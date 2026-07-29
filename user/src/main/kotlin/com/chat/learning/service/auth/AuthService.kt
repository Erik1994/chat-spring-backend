package com.chat.learning.service.auth

import com.chat.learning.domain.exception.InvalidCredentialsException
import com.chat.learning.domain.exception.InvalidTokenException
import com.chat.learning.domain.exception.UserAlreadyExistsException
import com.chat.learning.domain.exception.UserNotFoundException
import com.chat.learning.domain.model.AuthenticatedUser
import com.chat.learning.domain.model.User
import com.chat.learning.domain.model.UserId
import com.chat.learning.infra.database.entities.RefreshTokenEntity
import com.chat.learning.infra.database.entities.UserEntity
import com.chat.learning.infra.database.repositories.RefreshTokenRepository
import com.chat.learning.infra.database.repositories.UserRepository
import com.chat.learning.infra.mappers.toUser
import com.chat.learning.infra.security.PasswordEncoder
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.security.MessageDigest
import java.time.Instant
import java.util.Base64

@Service
class AuthService(
    private val userRepository: UserRepository,
    private val passwordEncoder: PasswordEncoder,
    private val jwtService: JwtService,
    private val refreshTokenRepository: RefreshTokenRepository,
) {
    fun register(email: String, username: String, password: String): User {
        val user = userRepository.findByEmailOrUsername(
            email = email.trim(),
            username = username.trim(),
        )
        if (user != null) {
            throw UserAlreadyExistsException()
        }

        val savedUser = userRepository.save(
            UserEntity(
                email = email.trim(),
                username = username.trim(),
                hashedPassword = passwordEncoder.encode(password),
            )
        ).toUser()
        return savedUser
    }

    fun login(email: String, password: String): AuthenticatedUser {
        val user = userRepository.findByEmail(
            email = email.trim()
        ) ?: throw InvalidCredentialsException()

        if (passwordEncoder.matches(password, user.hashedPassword).not()) {
            throw InvalidCredentialsException()
        }

        if (user.hasEmailVerified.not()) {
            //TODO: Check if the user has verified their email
        }

        return user.id?.let {
            val accessToken = jwtService.generateAccessToken(it)
            val refreshToken = jwtService.generateRefreshToken(it)

            storeRefreshToken(it, refreshToken)

            AuthenticatedUser(
                accessToken = accessToken,
                refreshToken = refreshToken,
                user = user.toUser(),
            )
        } ?: throw UserNotFoundException()
    }

    @Transactional
    fun refreshToken(refreshToken: String): AuthenticatedUser {
        if (jwtService.validateRefreshToken(refreshToken).not()) {
            throw InvalidTokenException(message = "Invalid refresh token")
        }

        val userId = jwtService.getUserIdFomToken(refreshToken)
        val user = userRepository.findByIdOrNull(userId) ?: throw UserNotFoundException()

        val hashed = hashToken(refreshToken)
        return user.id?.let {
            refreshTokenRepository.findByUserIdAndHashedToken(it, hashed)
                ?: throw InvalidTokenException(message = "Invalid refresh token")
            refreshTokenRepository.deleteByUserIdAndHashedToken(
                userId = it,
                hashedToken = hashed,
            )

            val newAccessToken = jwtService.generateAccessToken(it)
            val newRefreshToken = jwtService.generateRefreshToken(it)
            storeRefreshToken(it, newRefreshToken)

            AuthenticatedUser(
                accessToken = newAccessToken,
                refreshToken = newRefreshToken,
                user = user.toUser(),
            )
        } ?: throw UserNotFoundException()
    }

    @Transactional
    fun logout(refreshToken: String) {
        val userId = jwtService.getUserIdFomToken(refreshToken)
        val hashed = hashToken(refreshToken)
        refreshTokenRepository.deleteByUserIdAndHashedToken(userId, hashed)
    }

    private fun storeRefreshToken(userId: UserId, refreshToken: String) {
        val hashed = hashToken(refreshToken)
        val expiryMs = jwtService.refreshTokenValidityMs
        val expiresAt = Instant.now().plusMillis(expiryMs)

        refreshTokenRepository.save(
            RefreshTokenEntity(
                userId = userId,
                expiresAt = expiresAt,
                hashedToken = hashed,
            )
        )
    }

    private fun hashToken(token: String): String {
        val digest = MessageDigest.getInstance("SHA-256")
        val hashBytes = digest.digest(token.encodeToByteArray())
        return Base64.getEncoder().encodeToString(hashBytes)
    }
}
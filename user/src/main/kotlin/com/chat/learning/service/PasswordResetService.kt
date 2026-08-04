package com.chat.learning.service

import com.chat.learning.domain.exception.InvalidCredentialsException
import com.chat.learning.domain.exception.InvalidTokenException
import com.chat.learning.domain.exception.SamePasswordException
import com.chat.learning.domain.exception.UserNotFoundException
import com.chat.learning.domain.model.UserId
import com.chat.learning.infra.database.entities.PasswordResetTokenEntity
import com.chat.learning.infra.database.repositories.PasswordResetTokenRepository
import com.chat.learning.infra.database.repositories.RefreshTokenRepository
import com.chat.learning.infra.database.repositories.UserRepository
import com.chat.learning.infra.security.PasswordEncoder
import jakarta.transaction.Transactional
import org.springframework.beans.factory.annotation.Value
import org.springframework.data.repository.findByIdOrNull
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import java.time.Instant
import java.time.temporal.ChronoUnit

@Service
class PasswordResetService(
    private val passwordResetTokenRepository: PasswordResetTokenRepository,
    private val userRepository: UserRepository,
    private val passwordEncoder: PasswordEncoder,
    private val refreshTokenRepository: RefreshTokenRepository,
    @param:Value("\${chat.reset-password.expiry-minutes}")
    private val expiryMinutes: Long,
) {
    @Transactional
    fun requestPasswordReset(email: String) {
        val user = userRepository.findByEmail(email) ?: return

        passwordResetTokenRepository.invalidateActiveTokensForUser(user)

        val token = PasswordResetTokenEntity(
            expiresAt = Instant.now().plus(expiryMinutes, ChronoUnit.MINUTES),
            user = user,
        )

        passwordResetTokenRepository.save(token)

        // TODO: Inform notification service about the password reset trigger to send an email to the user
    }

    @Transactional
    fun resetPassword(token: String, password: String) {
        val resetToken = passwordResetTokenRepository.findByToken(token)
            ?: throw InvalidTokenException("Invalid password reset token")

        if (resetToken.isUsed) throw InvalidTokenException("Email verification token is already used")
        if (resetToken.isExpired) throw InvalidTokenException("Email verification token is expired")

        val user = resetToken.user
        if (passwordEncoder.matches(password, user.hashedPassword)) {
            throw SamePasswordException()
        }

        val hashedNewPassword = passwordEncoder.encode(password)
        userRepository.save(
            user.apply {
                hashedPassword = hashedNewPassword
            }
        )

        passwordResetTokenRepository.save(
            resetToken.apply {
                usedAt = Instant.now()
            }
        )

        refreshTokenRepository.deleteByUserId(user.id!!)
    }

    @Transactional
    fun changePassword(
        userId: UserId,
        oldPassword: String,
        newPassword: String,
    ) {
        val user = userRepository.findByIdOrNull(userId)
            ?: throw UserNotFoundException()

        if (passwordEncoder.matches(oldPassword, user.hashedPassword).not()) {
            throw InvalidCredentialsException()
        }

        if (oldPassword == newPassword) {
            throw SamePasswordException()
        }

        refreshTokenRepository.deleteByUserId(user.id!!)
        val newHashPassword = passwordEncoder.encode(newPassword)
        userRepository.save(
            user.apply {
                hashedPassword = newHashPassword
            }
        )
    }

    @Scheduled(cron = "0 0 3 * * *")
    fun cleanUpExpiredTokens() {
        passwordResetTokenRepository.deleteByExpiresAtLessThan(Instant.now())
    }
}
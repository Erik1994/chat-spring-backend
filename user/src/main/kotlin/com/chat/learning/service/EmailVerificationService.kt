package com.chat.learning.service.auth

import com.chat.learning.domain.exception.InvalidTokenException
import com.chat.learning.domain.exception.UserNotFoundException
import com.chat.learning.domain.model.EmailVerificationToken
import com.chat.learning.infra.database.entities.EmailVerificationTokenEntity
import com.chat.learning.infra.database.repositories.EmailVerificationTokenRepository
import com.chat.learning.infra.database.repositories.UserRepository
import com.chat.learning.infra.mappers.toEmailVerificationToken
import org.springframework.beans.factory.annotation.Value
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Instant
import java.time.temporal.ChronoUnit

@Service
class EmailVerificationService(
    private val emailVerificationTokenRepository: EmailVerificationTokenRepository,
    private val userRepository: UserRepository,
    @param:Value("\${chat.email.verification.expiry-hours}") private val expiryHours: Long
) {
    @Transactional
    fun createVerificationToken(email: String): EmailVerificationToken {
        val userEntity = userRepository.findByEmail(email)
            ?: throw UserNotFoundException()

        emailVerificationTokenRepository.invalidateActiveTokensForUser(userEntity)

        val token = EmailVerificationTokenEntity(
            expiresAt = Instant.now().plus(expiryHours, ChronoUnit.HOURS),
            user = userEntity,
        )

        return emailVerificationTokenRepository.save(token).toEmailVerificationToken()
    }

    @Transactional
    fun verifyEmail(token: String) {
        val verificationToken = emailVerificationTokenRepository.findByToken(token)
            ?: throw InvalidTokenException("Email verification token is invalid")

        if (verificationToken.isUsed) throw InvalidTokenException("Email verification token is already used")
        if (verificationToken.isExpired) throw InvalidTokenException("Email verification token is expired")

        emailVerificationTokenRepository.save(
            verificationToken.apply {
                usedAt = Instant.now()
            }
        )

        userRepository.save(
            verificationToken.user.apply {
                hasEmailVerified = true
            }
        )
    }

    @Scheduled(cron = "0 0 3 * * *") // Run every day at 3:00 AM
    fun cleanUpExpiredTokens() {
        emailVerificationTokenRepository.deleteByExpiresAtLessThan(Instant.now())
    }
}
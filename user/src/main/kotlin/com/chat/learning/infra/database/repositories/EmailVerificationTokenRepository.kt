package com.chat.learning.infra.database.repositories

import com.chat.learning.infra.database.entities.EmailVerificationTokenEntity
import com.chat.learning.infra.database.entities.UserEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import java.time.Instant

interface EmailVerificationTokenRepository: JpaRepository<EmailVerificationTokenEntity, Long> {
    fun findByToken(token: String): EmailVerificationTokenEntity?
    fun deleteByExpiresAtLessThan(now: Instant)

    //Instead of using findByUserAndUsedAtIsNull, we can use the following query
    //which is more performant, as it does the update in a single query
    //fun findByUserAndUsedAtIsNull(user: UserEntity): List<EmailVerificationTokenEntity>

    @Modifying
    @Query(
        """
            UPDATE EmailVerificationTokenEntity e
            SET e.usedAt = CURRENT_TIMESTAMP
            WHERE e.user = :user
        """
    )
    fun invalidateActiveTokensForUser(user: UserEntity)
}
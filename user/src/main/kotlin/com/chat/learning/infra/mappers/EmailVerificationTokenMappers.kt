package com.chat.learning.infra.mappers

import com.chat.learning.domain.model.EmailVerificationToken
import com.chat.learning.infra.database.entities.EmailVerificationTokenEntity

fun EmailVerificationTokenEntity.toEmailVerificationToken(): EmailVerificationToken = EmailVerificationToken(
    id = id,
    token = token,
    user = user.toUser(),
)
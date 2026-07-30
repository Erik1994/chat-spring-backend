package com.chat.learning.domain.model

data class EmailVerificationToken(
    val id: Long,
    val token: String,
    val user: User
)

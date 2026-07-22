package com.chat.learning.api.dto

import com.chat.learning.domain.model.UserId

data class UserDto (
    val id: UserId,
    val email: String,
    val username: String,
    val hasEmailVerified: Boolean,
)
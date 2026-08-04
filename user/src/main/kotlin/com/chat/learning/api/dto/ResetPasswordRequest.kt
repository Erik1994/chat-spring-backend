package com.chat.learning.api.dto

import com.chat.learning.api.util.Password
import jakarta.validation.constraints.NotBlank

data class ResetPasswordRequest(
    @field:NotBlank
    val token: String,
    @field:Password
    val newPassword: String,
)

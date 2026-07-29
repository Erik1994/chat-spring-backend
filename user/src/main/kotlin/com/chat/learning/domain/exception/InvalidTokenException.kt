package com.chat.learning.domain.exception

class InvalidTokenException(
    override val message: String? = null
): RuntimeException(message ?: "Invalid token") {
}
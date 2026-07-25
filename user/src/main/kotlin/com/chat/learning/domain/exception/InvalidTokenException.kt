package com.chat.learning.domain.exception

class InvalidTokenException(
    override val message: String?
): RuntimeException(message ?: "Invalid token") {
}
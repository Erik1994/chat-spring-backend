package com.chat.learning.domain.exception

class InvalidCredentialsException: RuntimeException(
    "The entered credentials are invalid"
) {
}
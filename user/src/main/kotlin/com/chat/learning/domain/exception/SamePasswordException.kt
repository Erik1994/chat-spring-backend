package com.chat.learning.domain.exception

class SamePasswordException: RuntimeException(
    "New password must be different from the old password"
) {
}
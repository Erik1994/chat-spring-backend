package com.chat.learning.infra.security

import java.security.SecureRandom
import java.util.Base64

object TokenGenerator {

    fun generateSecureToken(): String {
        val bytes = ByteArray(32)
        val secureRandom = SecureRandom()
        secureRandom.nextBytes(bytes)
        return Base64
            .getUrlEncoder()
            .withoutPadding()
            .encodeToString(bytes)
    }

}
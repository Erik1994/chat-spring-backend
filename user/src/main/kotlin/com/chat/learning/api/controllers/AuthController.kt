package com.chat.learning.api.controllers

import com.chat.learning.api.dto.RegisterRequest
import com.chat.learning.api.dto.UserDto
import com.chat.learning.api.mappers.toUserDto
import com.chat.learning.service.AuthService
import jakarta.validation.Valid
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/auth")
class AuthController (
    private val authService: AuthService,
) {

    @PostMapping("/register")
    fun register(
        @Valid @RequestBody body: RegisterRequest,
    ): UserDto = authService.register(
        email = body.email,
        username = body.username,
        password = body.password,
    ).toUserDto()
}
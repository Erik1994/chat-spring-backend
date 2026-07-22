package com.chat.learning.api.mappers

import com.chat.learning.api.dto.AuthenticatedUserDto
import com.chat.learning.api.dto.UserDto
import com.chat.learning.domain.model.AuthenticatedUser
import com.chat.learning.domain.model.User

fun AuthenticatedUser.toAuthenticatedUserDto(): AuthenticatedUserDto = AuthenticatedUserDto(
    user = user.toUserDto(),
    accessToken = accessToken,
    refreshToken = refreshToken,
)

fun User.toUserDto(): UserDto = UserDto(
    id = id,
    email = email,
    username = username,
    hasEmailVerified = hasEmailVerified,
)
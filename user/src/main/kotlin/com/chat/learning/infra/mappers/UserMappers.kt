package com.chat.learning.infra.mappers

import com.chat.learning.domain.model.User
import com.chat.learning.infra.database.entities.UserEntity

fun UserEntity.toUser(): User = User(
    id = id!!,
    email = email,
    username = username,
    hasEmailVerified = hasEmailVerified,
)
package com.chat.learning.domain.exception

class UserAlreadyExistsException: RuntimeException("A user with this email or username already exists.")
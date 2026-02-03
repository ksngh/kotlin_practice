package com.kotlin_practice.dto

data class SignUpRequest(
    val email: String,
    val password: String,
    val name: String,
)

data class LoginRequest(
    val email: String,
    val password: String,
)

data class AuthResponse(
    val token: String,
)

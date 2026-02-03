package com.kotlin_practice.dto

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

data class SignUpRequest(
    @field:Email
    @field:NotBlank
    @field:Schema(example = "user@example.com")
    val email: String,

    @field:NotBlank
    @field:Size(min = 8, max = 64)
    @field:Schema(example = "P@ssw0rd1234")
    val password: String,

    @field:NotBlank
    @field:Size(min = 2, max = 50)
    @field:Schema(example = "홍길동")
    val name: String,
)

data class LoginRequest(
    @field:Email
    @field:NotBlank
    @field:Schema(example = "user@example.com")
    val email: String,

    @field:NotBlank
    @field:Schema(example = "P@ssw0rd1234")
    val password: String,
)

data class AuthResponse(
    @field:Schema(example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...")
    val token: String,
)

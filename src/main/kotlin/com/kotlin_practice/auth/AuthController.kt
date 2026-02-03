package com.kotlin_practice.auth

import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController

@RestController
class AuthController(
    private val authService: AuthService,
) {
    @PostMapping("/signup")
    @ResponseStatus(HttpStatus.CREATED)
    fun signUp(@RequestBody request: SignUpRequest): AuthResponse {
        return authService.signUp(request)
    }

    @PostMapping("/login")
    fun login(@RequestBody request: LoginRequest): AuthResponse {
        return authService.login(request)
    }
}


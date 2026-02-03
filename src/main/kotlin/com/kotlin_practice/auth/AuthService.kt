package com.kotlin_practice.auth

import com.kotlin_practice.user.UserEntity
import com.kotlin_practice.user.UserRepository
import com.kotlin_practice.user.UserRole
import org.springframework.http.HttpStatus
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.web.server.ResponseStatusException

@Service
class AuthService(
    private val userRepository: UserRepository,
    private val passwordEncoder: PasswordEncoder,
    private val jwtProvider: JwtProvider,
) {
    fun signUp(request: SignUpRequest): AuthResponse {
        if (userRepository.existsByEmail(request.email)) {
            throw ResponseStatusException(HttpStatus.CONFLICT, "Email already exists")
        }

        val user = UserEntity(
            email = request.email,
            password = passwordEncoder.encode(request.password),
            name = request.name,
            role = UserRole.MEMBER,
        )
        userRepository.save(user)
        val token = jwtProvider.generateToken(user.email)
        return AuthResponse(token)
    }

    fun login(request: LoginRequest): AuthResponse {
        val user = userRepository.findByEmail(request.email)
            ?: throw ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid credentials")

        if (!passwordEncoder.matches(request.password, user.password)) {
            throw ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid credentials")
        }

        val token = jwtProvider.generateToken(user.email)
        return AuthResponse(token)
    }
}


package com.kotlin_practice.controller

import com.kotlin_practice.dto.AuthResponse
import com.kotlin_practice.dto.LoginRequest
import com.kotlin_practice.dto.SignUpRequest
import com.kotlin_practice.service.AuthService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import jakarta.validation.Valid
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
    @Operation(summary = "회원 가입", description = "이메일/패스워드/이름으로 회원 가입 후 JWT를 발급합니다.")
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "201", description = "가입 성공",
                content = [Content(schema = Schema(implementation = AuthResponse::class))]),
            ApiResponse(responseCode = "400", description = "검증 실패"),
            ApiResponse(responseCode = "409", description = "이메일 중복"),
        ],
    )
    fun signUp(@Valid @RequestBody request: SignUpRequest): AuthResponse {
        return authService.signUp(request)
    }

    @PostMapping("/login")
    @Operation(summary = "로그인", description = "이메일/패스워드로 로그인 후 JWT를 발급합니다.")
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "로그인 성공",
                content = [Content(schema = Schema(implementation = AuthResponse::class))]),
            ApiResponse(responseCode = "400", description = "검증 실패"),
            ApiResponse(responseCode = "401", description = "인증 실패"),
        ],
    )
    fun login(@Valid @RequestBody request: LoginRequest): AuthResponse {
        return authService.login(request)
    }
}

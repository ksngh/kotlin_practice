# Auth 기능 정리

Base URL: `http://localhost:8080`

## 기능 요약

- 회원 가입: 이메일/패스워드/이름으로 회원 생성 후 JWT 발급
- 로그인: 이메일/패스워드로 인증 후 JWT 발급
- 인증: 회원가입/로그인을 제외한 모든 요청은 JWT 필요

## 회원 가입

POST `/signup`

Request JSON
```json
{
  "email": "user@example.com",
  "password": "P@ssw0rd1234",
  "name": "홍길동"
}
```

Response 201
```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
}
```

Validation rules
- email: 필수, 이메일 형식
- password: 필수, 8~64자
- name: 필수, 2~50자

Possible errors
- 400: 요청 검증 실패
- 409: 이메일 중복

## 로그인

POST `/login`

Request JSON
```json
{
  "email": "user@example.com",
  "password": "P@ssw0rd1234"
}
```

Response 200
```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
}
```

Possible errors
- 400: 요청 검증 실패
- 401: 인증 실패

## 인증이 필요한 요청

Header
```
Authorization: Bearer <JWT>
```

## Swagger/OpenAPI

- OpenAPI JSON: `/v3/api-docs`
- Swagger UI: `/swagger-ui/index.html`

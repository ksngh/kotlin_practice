# Feedback Documentation

Base URL: `http://localhost:8080`

## 기능 요약

- 피드백 생성: 특정 대화에 대한 긍/부정 피드백 생성
- 피드백 목록 조회: 본인 피드백(관리자는 전체) + 페이지/정렬/필터
- 피드백 상태 변경: 관리자만 가능

## 피드백 생성

POST `/feedbacks`

Request JSON
```json
{
  "chatId": "10",
  "isPositive": true
}
```

Response 201
```json
{
  "feedbackId": 1,
  "userId": 2,
  "chatId": 10,
  "isPositive": true,
  "status": "PENDING",
  "createdAt": "2026-02-03T15:00:00+09:00"
}
```

Possible errors
- 400: 검증 실패/잘못된 chatId
- 401: 인증 실패
- 403: 권한 없음
- 409: 이미 피드백 존재

## 피드백 목록 조회

GET `/feedbacks?page=0&size=20&sort=desc&isPositive=true`

Response 200
```json
{
  "feedbacks": [
    {
      "feedbackId": 1,
      "userId": 2,
      "chatId": 10,
      "isPositive": true,
      "status": "PENDING",
      "createdAt": "2026-02-03T15:00:00+09:00"
    }
  ],
  "page": 0,
  "size": 20,
  "totalElements": 1,
  "totalPages": 1
}
```

## 피드백 상태 변경 (관리자)

PATCH `/feedbacks/{feedbackId}/status`

Request JSON
```json
{
  "status": "RESOLVED"
}
```

Response 200
```json
{
  "feedbackId": 1,
  "userId": 2,
  "chatId": 10,
  "isPositive": true,
  "status": "RESOLVED",
  "createdAt": "2026-02-03T15:00:00+09:00"
}
```

## 인증

Header
```
Authorization: Bearer <JWT>
```

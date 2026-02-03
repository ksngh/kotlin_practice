# Analytics & Reports Documentation

Base URL: `http://localhost:8080`

## 기능 요약

- 사용자 활동 기록: 지난 24시간 회원가입/로그인/대화 생성 수
- 보고서 생성: 지난 24시간 대화 내역을 CSV로 생성
- 관리자만 접근 가능

## 사용자 활동 기록

GET `/admin/activity`

Response 200
```json
{
  "from": "2026-02-02T15:00:00+09:00",
  "to": "2026-02-03T15:00:00+09:00",
  "signUpCount": 12,
  "loginCount": 34,
  "chatCount": 56
}
```

## CSV 보고서

GET `/admin/reports/chats.csv`

Response
- Content-Type: `text/csv`
- 파일 다운로드

CSV header
```
chat_id,user_id,user_email,question,answer,created_at
```

## 인증

Header
```
Authorization: Bearer <JWT>
```

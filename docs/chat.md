# Chat & Thread Documentation

Base URL: `http://localhost:8080`

## 기능 요약

- 대화 생성: 질문을 입력받아 답변 생성 (옵션: isStreaming, model)
- 대화 목록 조회: 스레드 단위로 그룹화
- 스레드 삭제: 본인 스레드만 삭제 가능 (관리자는 전체 가능)

## 대화 생성 (일반)

POST `/chats`

Request JSON
```json
{
  "question": "질문을 입력하세요.",
  "isStreaming": false,
  "model": "gpt-4o-mini"
}
```

Response 200
```json
{
  "threadId": 1,
  "chatId": 10,
  "question": "질문을 입력하세요.",
  "answer": "모델 응답 내용",
  "createdAt": "2026-02-03T14:30:00+09:00"
}
```

## 대화 생성 (Streaming SSE)

POST `/chats` with `isStreaming=true`

Response headers
```
Content-Type: text/event-stream
```

SSE events
```
event: meta
data: {"threadId":1}

event: delta
data: "...partial..."

event: done
data: {"chatId":10,"threadId":1}
```

## 대화 목록 조회 (스레드 단위)

GET `/threads?page=0&size=20&sort=desc`

Response 200
```json
{
  "threads": [
    {
      "threadId": 1,
      "createdAt": "2026-02-03T14:00:00+09:00",
      "lastMessageAt": "2026-02-03T14:30:00+09:00",
      "chats": [
        {
          "chatId": 10,
          "question": "질문을 입력하세요.",
          "answer": "모델 응답 내용",
          "createdAt": "2026-02-03T14:30:00+09:00"
        }
      ]
    }
  ],
  "page": 0,
  "size": 20,
  "totalElements": 1,
  "totalPages": 1
}
```

## 스레드 삭제

DELETE `/threads/{threadId}`

Response 204

## 인증

Header
```
Authorization: Bearer <JWT>
```

## OpenAI 설정

`src/main/resources/application.yml`
```
openai:
  api:
    key: ${OPENAI_API_KEY:}
    base-url: https://api.openai.com/v1
    model: gpt-4o-mini
```

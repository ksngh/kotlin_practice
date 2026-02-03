# kotlin_practice

## 기술 스택
- Kotlin 1.9.x
- Spring Boot 3.x.x
- PostgreSQL 15.8

## 요구사항

### 기능 요구사항

- 인증(Auth)
    - 이메일/비밀번호 기반 회원가입 및 로그인
    - 로그인 성공 시 JWT 발급

- JWT 인증
    - 회원가입/로그인을 제외한 모든 API는 유효한 JWT 필요

- 채팅 / 스레드
    - 채팅 생성 (일반 응답 또는 SSE 스트리밍)
    - 사용자별 스레드 단위로 대화 그룹화
    - 스레드 생성 규칙
        - 첫 질문 시 신규 스레드 생성
        - 마지막 메시지 이후 30분 초과 시 신규 스레드 생성
        - 30분 이내 재요청 시 기존 스레드 재사용

- 피드백
    - 사용자는 본인이 생성한 채팅에만 피드백 생성 가능
    - 관리자는 모든 채팅에 대해 피드백 생성 가능
    - 사용자당 채팅 1건에 대해 피드백 1개만 생성 가능
    - 사용자는 자신의 피드백만 조회 가능, 관리자는 전체 조회 가능
    - 관리자는 피드백 상태(`PENDING`, `RESOLVED`) 변경 가능

- 분석 / 리포트 (관리자 전용)
    - 최근 24시간 기준 활동 통계 제공 (회원가입, 로그인, 채팅 수)
    - 최근 24시간 채팅 내역 CSV 리포트 생성

## 구현 기능

- Spring Security 기반 JWT 인증/인가
- 사용자, 스레드, 채팅, 피드백, 로그인 이벤트에 대한 JPA 엔티티 및 Repository 구성
- OpenAI Responses API 연동 (스트리밍 및 일반 응답 지원)
- Swagger/OpenAPI 문서 자동 생성
- Docker Compose 기반 PostgreSQL 15.8 실행 환경 제공

## 실행 방법

### 1) 데이터베이스 실행
```bash
# Windows PowerShell
# 프로젝트 루트 경로에서 실행
docker compose up -d
```

### 2) OpenAI API Key 설정
```powershell
$env:OPENAI_API_KEY="sk-..."
```

### 3) 애플리케이션 실행
```bash
./gradlew bootRun
```

기본 주소: `http://localhost:8080`

## Swagger / OpenAPI
- OpenAPI 명세: `/v3/api-docs`
- Swagger UI: `/swagger-ui/index.html`

## 참고 사항
- 기본 DB 설정 위치: `src/main/resources/application.yml`
    - database: `kotlin_practice`
    - user: `postgres`
    - password: `postgres`
- 보호된 API는 반드시 `Authorization: Bearer <JWT>` 헤더 필요
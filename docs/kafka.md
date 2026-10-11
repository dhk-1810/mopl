### Kafka 사용처

- 1:N CQRS 데이터 동기화 (Pub/Sub)
  - 유저 프로필 변경 (`user.profile.events`)
    - `mopl-user`에서 프로필 생성/수정 시 단일 토픽으로 발행 (파티션 키: `userId`).
    - `mopl-content`, `mopl-playlist`, `mopl-dm`, `mopl-notification` 4개 서비스가 각각 독립된 컨슈머 그룹으로 구독하여 로컬 CQRS 뷰 실시간 동기화.
- CDC 기반 분산 트랜잭션 (Saga 이벤트 스트리밍)
  - 컨텐츠 삭제 Saga 시작 (`content.saga.delete.start`)
    - `mopl-content`의 Outbox 테이블 INSERT를 Debezium CDC가 감지하여 Kafka 토픽으로 자동 라우팅.
    - `mopl-playlist`, `mopl-live-chat`이 비동기 컨슘하여 연관 리소스 정리 수행 (멱등성 보장).

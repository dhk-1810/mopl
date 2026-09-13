## Transactional Outbox/Inbox Pattern 적용하기

### 1. 쓰는 이유
- 자체 비즈니스 로직 처리, 이벤트 발행이 원자성을 가져야 하나, 일부만 성공할 수 있음.
- Outbox 테이블에 이벤트를 INSERT 하고 스케줄러(폴링)나 CDC로 이벤트를 발송하게끔 해 정합성을 확보. 
- Inbox 테이블은 네트워크 장애 후 재시도 등 상황에서 참여 서버의 같은 이벤트 중복 수신을 막기 위해 사용. (멱등성 보장)

### 2. Transactional Outbox Pattern (아웃박스 테이블 & 폴링 릴레이)
- **적용**: `mopl-content` 모듈의 `OutboxEvent`, `OutboxService`, `OutboxPublisherScheduler`, `OutboxCleanupScheduler`.
- **동작**:
    - **동일 트랜잭션 내 Outbox 영속화**
      - 도메인 상태 변경(컨텐츠 수정/삭제, Saga 시작 등) 시, 메시지 브로커 전송 전 Spring 트랜잭션 커밋 직전(`BEFORE_COMMIT` 또는 서비스 트랜잭션 내부)에 
      - `outbox_events` 테이블에 이벤트 페이로드(JSON) 및 메타데이터를 `PENDING` 상태로 저장.
    - **장애 안전성 (At-Least-Once Delivery)**
      - 트랜잭션 커밋 직후 네트워크 오류나 RabbitMQ 브로커 일시 장애로 즉시 발행이 실패하더라도, DB에 안전하게 보관됨.
    - **Poller Scheduler 기반 비동기 릴레이**
      - `OutboxPublisherScheduler`가 3초 주기로 `PENDING` 상태의 아웃박스 이벤트를 조회하여 RabbitMQ로 재발행 및 ACK 수신 후 
      - `PUBLISHED` 상태로 갱신 (최대 5회 재시도 및 실패 시 `FAILED` 마킹).
    - **데이터 정리 스케줄러**
      - `OutboxCleanupScheduler`를 통해 7일 이상 경과한 `PUBLISHED` 이벤트를 매일 주기적으로 자동 정리하여 테이블 용량 최적화.

### 3. Transactional Inbox Pattern (인박스 테이블 및 멱등성 보장)
- **적용**:
    - `mopl-content`: `InboxEvent`, `InboxService`, `ContentSagaEventListener`, `InboxCleanupScheduler`
    - `mopl-playlist`: `InboxEvent`, `InboxService`, `ContentEventListener`, `InboxCleanupScheduler`
    - `mopl-watching-session`: `WatchingSessionEventListener` (Redis 기반 `setIfAbsent` Inbox 키 관리)
- **동작**:
    - **Saga 참가자(Playlist/WatchingSession)의 중복 수신 방지**
      - RabbitMQ의 At-Least-Once 전달 특성상 네트워크 재시도로 동일한 `ContentDeletionSagaEvent (START)`가 재유입되더라도, 
      - `inbox_events` 테이블(또는 Redis)의 유니크 `messageId`(`saga-start-${sagaId}`) 조회를 통해 중복 작업을 즉시 차단하고 기존 성공 응답을 재전송 후 ACK.
    - **Saga 오케스트레이터(Content)의 응답 멱등성 보장**
      - 참가자 서비스들로부터 전달되는 `ContentDeletionSagaEvent (RESPONSE)`에 대해 `saga-resp-${sagaId}-${participant}` 키로 
      - 중복 처리를 방지하여 보상 트랜잭션(`restoreActive`) 및 완료 확정(`markAsDeleted`)의 다중 실행을 원천 차단.
    - **데이터 정리 스케줄러**
      - 7일 이상 경과한 Inbox 이벤트를 주기적으로 자동 정리하여 DB 저장 용량 최적화.

## 3. 추가 개선 가능성
- **Dead Letter Queue (DLQ) 및 재시도 백오프(Retry Backoff) 세분화**
  - 일시적 장애와 비즈니스 오류를 구분하여 지연 큐(TTL 기반 Retry) 및 최종 DLQ 라우팅 파이프라인 구축.

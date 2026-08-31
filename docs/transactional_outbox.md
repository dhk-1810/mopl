# RabbitMQ 기반 메시징 정합성 및 신뢰성 확보 전략

## 1. 도입 배경

### 1) Dual Write 및 상태 고립 (Zombie State)
- 컨텐츠 삭제 시 `mopl-content` DB는 `DELETING` 상태로 변경되었으나, 네트워크 지연/RabbitMQ 일시 장애로 인해 Saga 시작 이벤트 발행이 실패하면 해당 컨텐츠는 영원히 `DELETING` 상태로 고립됨.

### 2) Consumer 서버 장애 시 메시지 유실 위험
- RabbitMQ에서 기본 모드로 메시지를 읽어오면, 컨슈머의 비즈니스 로직(DB 갱신 등) 도중 서버가 다운(Crash)되거나 예외가 발생했을 때 메시지가 이미 큐에서 소비 처리되어 영구 유실될 위험 존재.

---

## 2. 조치

### 1) Saga 데드락/고립 방지 자동 보상 스케줄러 (Timeout Mechanism)
- **적용**: `mopl-content` 모듈의 `ContentSagaTimeoutScheduler`.
- **동작**:
    - `Content` 엔티티의 `updatedAt` 필드를 기준으로 `DELETING` 상태가 3분 이상 지속된 고립 데이터를 1분마다 주기적으로 감지.
    - 브로커 장애 또는 네트워크 단절로 참여자 서비스의 응답이 누락된 경우, 자동으로 `restoreActive()`를 실행하여 상태를 원상 복구(보상 트랜잭션)시킴으로써 데드락 해소.

### 2) Consumer Manual ACK (비즈니스 처리 완료 후 명시적 ACK)
- **설정**: `spring.rabbitmq.listener.simple.acknowledge-mode: manual` 적용 (`content`, `playlist`, `watching-session`).
- **동작**:
    - 컨슈머가 메시지를 수신하여 로컬 DB 트랜잭션 및 응답 이벤트 전송까지 **완전히 완료한 직후에만 `channel.basicAck(deliveryTag, false)` 전송**.
    - 비즈니스 예외 발생 또는 처리 실패 시 `basicReject(deliveryTag, false)` 및 Saga FAILED 응답 전송.
    - 컨슈머 서버가 작업 도중 다운되면 Ack를 받지 못한 RabbitMQ 브로커가 메시지를 큐에 보존했다가 다른 컨슈머에게 안전하게 재전파(Redelivery).

### 3) Publisher Confirm 및 Returns 활성화 (발행 확인 및 디스크 영속성 검증)
- **설정**: `spring.rabbitmq.publisher-confirm-type: correlated`, `publisher-returns: true` 적용.
- **동작**:
  - RabbitTemplate에 `ConfirmCallback`과 `ReturnsCallback` 등록.
  - Durable 큐 및 Persistent 메시지 조건 하에서, 브로커가 메시지를 디스크/큐에 안전하게 적재(Flush)한 후 반환하는 `ACK` / `NACK` 결과를 Correlation ID와 함께 로깅하여 발행 신뢰성 확보.

---

### 4) Transactional Outbox Pattern (아웃박스 테이블 & 폴링 릴레이)
- **적용**: `mopl-content` 모듈의 `OutboxEvent`, `OutboxService`, `OutboxPublisherScheduler`, `OutboxCleanupScheduler`.
- **동작**:
    - **동일 트랜잭션 내 Outbox 영속화**: 도메인 상태 변경(컨텐츠 수정/삭제, Saga 시작 등) 시, 메시지 브로커 전송 전 Spring 트랜잭션 커밋 직전(`BEFORE_COMMIT` 또는 서비스 트랜잭션 내부)에 `outbox_events` 테이블에 이벤트 페이로드(JSON) 및 메타데이터를 `PENDING` 상태로 저장.
    - **장애 안전성 (At-Least-Once Delivery)**: 트랜잭션 커밋 직후 네트워크 오류나 RabbitMQ 브로커 일시 장애로 즉시 발행이 실패하더라도, DB에 안전하게 보관됨.
    - **Poller Scheduler 기반 비동기 릴레이**: `OutboxPublisherScheduler`가 3초 주기로 `PENDING` 상태의 아웃박스 이벤트를 조회하여 RabbitMQ로 재발행 및 ACK 수신 후 `PUBLISHED` 상태로 갱신 (최대 5회 재시도 및 실패 시 `FAILED` 마킹).
    - **데이터 정리 스케줄러**: `OutboxCleanupScheduler`를 통해 7일 이상 경과한 `PUBLISHED` 이벤트를 매일 주기적으로 자동 정리하여 테이블 용량 최적화.

### 5) Transactional Inbox Pattern (인박스 테이블 및 멱등성 보장)
- **적용**:
    - `mopl-content`: `InboxEvent`, `InboxService`, `ContentSagaEventListener`, `InboxCleanupScheduler`
    - `mopl-playlist`: `InboxEvent`, `InboxService`, `ContentEventListener`, `InboxCleanupScheduler`
    - `mopl-watching-session`: `WatchingSessionEventListener` (Redis 기반 `setIfAbsent` Inbox 키 관리)
- **동작**:
    - **Saga 참가자(Playlist/WatchingSession)의 중복 수신 방지**: RabbitMQ의 At-Least-Once 전달 특성상 네트워크 재시도로 동일한 `ContentDeletionSagaEvent (START)`가 재유입되더라도, `inbox_events` 테이블(또는 Redis)의 유니크 `messageId`(`saga-start-${sagaId}`) 조회를 통해 중복 작업을 즉시 차단하고 기존 성공 응답을 재전송 후 ACK.
    - **Saga 오케스트레이터(Content)의 응답 멱등성 보장**: 참가자 서비스들로부터 전달되는 `ContentDeletionSagaEvent (RESPONSE)`에 대해 `saga-resp-${sagaId}-${participant}` 키로 중복 처리를 방지하여 보상 트랜잭션(`restoreActive`) 및 완료 확정(`markAsDeleted`)의 다중 실행을 원천 차단.
    - **데이터 정리 스케줄러**: 7일 이상 경과한 Inbox 이벤트를 주기적으로 자동 정리하여 DB 저장 용량 최적화.

---

## 3. 추후 개선 가능성

1. **Debezium 기반 Change Data Capture (CDC) 엔진 연동**
   - 현재 Polling 기반 스케줄러에서 DB 부하를 더욱 최소화하고 준실시간 스트리밍 처리가 필요한 경우 Debezium 등 CDC 파이프라인으로 전환 가능.
2. **Dead Letter Queue (DLQ) 및 재시도 백오프(Retry Backoff) 세분화**
   - 일시적 장애와 비즈니스 오류를 구분하여 지연 큐(TTL 기반 Retry) 및 최종 DLQ 라우팅 파이프라인 구축.

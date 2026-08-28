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

## 3. 추후 개선 가능성

1. **Transactional Outbox Table & Debezium (CDC) 도입**
   - 현재의 Timeout 기반 롤백 방식에서 더 나아가, "브로커 장애 시에도 메시지 발행 자체를 100% 무손실 보장"해야 하는 요구사항이 강화될 경우 로컬 Outbox 테이블 + CDC(또는 Poller) 구조로 점진적 고도화.
2. **컨슈머 멱등성(Idempotency) 전용 저장소 도입**
   - At-least-once 전달 특성으로 인한 중복 메시지 유입에 대비하여, Redis 또는 DB 기반의 `Message-ID` 중복 소비 방지 필터 도입.
3. **Dead Letter Queue (DLQ) 및 재시도 백오프(Retry Backoff) 세분화**
   - 일시적 장애와 비즈니스 오류를 구분하여 지연 큐(TTL 기반 Retry) 및 최종 DLQ 라우팅 파이프라인 구축.

## Saga 구현해보기

### 1. 필요성
- MSA 도입으로 서버 간 트랜잭션 ACID 보장 불가
- 컨텐츠 삭제 시, 플레이리스트 담음 정보(Curation), 시청세션도 원자적으로 삭제되어야 하나. `@Transactional`로 이들을 묶을 수 없음.

### 2. 기본 구현
- 컨텐츠 삭제 시 상태를 `DELETING`으로 변경, RabbitMQ로 연관 서비스로 이벤트 전파.
- 연관 서비스의 삭제 실패 시 보상 트랜잭션을 트리거해, 컨텐츠 상태를 ACTIVE로 롤백 처리.
  - 둘 모두 성공 응답 이벤트를 받았을 때 `DELETED`로 상태 변경, 하나라도 실패하면 `ACTIVE`로 롤백.

### 3. 상태 고립(Zombie State) 방지
- 컨텐츠 삭제 시 `mopl-content` DB는 `DELETING` 상태로 변경되었으나, 
- 네트워크 지연/RabbitMQ 일시 장애로 인해 Saga 시작 이벤트 발행이 실패하면 해당 컨텐츠는 영원히 `DELETING` 상태로 고립될 수 있음.

- Timeout 스케줄러 `ContentSagaTimeoutScheduler` 도입, 
- `Content` 엔티티의 `updatedAt` 필드를 기준으로 `DELETING` 상태가 3분 이상 지속된 고립 데이터를 1분마다 주기적으로 감지.
- 브로커 장애/네트워크 단절로 참여 서버의 응답이 누락되면 자동으로 `restoreActive()`를 실행 -> 상태를 원상 복구

### 4. Consumer 서버 장애 시 메시지 유실 위험 차단
- RabbitMQ의 Consumer는 메시지를 읽어갈 때 ACK을 보내고 큐에서 영구 삭제함. (기본 설정)
- 컨슈머의 비즈니스 로직(DB 갱신 등) 도중 서버 다운(Crash), 예외 발생 시 메시지가 이미 큐에서 소비 처리되어 영구 유실될 수 있음.

- 컨슈머가 로컬 DB 트랜잭션 및 응답 이벤트 전송까지 완전히 완료한 후 `channel.basicAck(deliveryTag, false)` 전송.
- 비즈니스 예외 발생 또는 처리 실패 시 `basicReject(deliveryTag, false)` 및 Saga FAILED 응답 전송.
- 컨슈머 서버가 작업 도중 다운되면 Ack를 받지 못한 RabbitMQ 브로커가 메시지를 큐에 보존했다가 다른 컨슈머에게 안전하게 재전파(Redelivery).

### 5. RabbitMQ 자체 장애 대처
- RabbitMQ 서버 다운 시 큐의 메시지는 전부 증발함.

- Durable 큐 / Persistent 메시지 사용, 메시지를 디스크에 저장.
- Publisher Confirm 및 Returns 활성화 (발행 확인, 디스크 영속성 보증)
  - Publisher Confirm: 브로커가 디스크에 저장을 끝마쳤음을 발행자에게 확인해 주는 알림(ACK)
  - Publisher Returns: 라우팅 키 오타 등으로 어떤 큐에도 들어가지 못한 메시지를 발행자에게 반송해 주는 알림(NACK)
  - 결과(ACK/NACK)는 Correlation ID와 함께 로깅.
- `spring.rabbitmq.publisher-confirm-type: correlated`, `publisher-returns: true` 적용.
- RabbitTemplate에 `ConfirmCallback`과 `ReturnsCallback` 등록.
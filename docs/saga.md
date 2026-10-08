## Saga 보상 트랜잭션 구현 (gRPC 동기 오케스트레이션)

### 1. 배경 및 필요성
- MSA 환경에서 컨텐츠 삭제 시, 연관된 플레이리스트 담음 정보(Curation) 및 실시간 시청 세션(WatchingSession)이 원자적으로 정리되어야 함.
- 단일 `@Transactional`로 다른 서비스들의 DB/Redis를 묶을 수 없으므로 분산 트랜잭션 및 보상 트랜잭션(Saga) 메커니즘 도입.


### 2. 아키텍처 전환 이유 (Message Broker vs gRPC)
* **기존 비동기 브로커(RabbitMQ / Kafka CDC)의 한계**:
  * 비동기 이벤트 분기(Fork) 후 결과 병합(Join) 과정에서 메시지 도착 순서 역전(Race Condition)으로 인한 상태 덮어쓰기 위험 존재.
  * 3분 타임아웃 동안 컨텐츠가 좀비 상태로 고립되어 사용자 경험 저하.
  * 복잡한 4중 인프라(Outbox + Debezium CDC + Kafka + RabbitMQ + Inbox)로 인한 운영 복잡도 과다.
* **gRPC + Deadline 타임아웃 기반 순차 오케스트레이션 채택**:
  * 컨텐츠 삭제 연관 서비스가 2개(`Playlist`, `LiveChat`)로 명확하고 빠른 삭제 작업이므로, **직접 gRPC 호출**을 통해 극적으로 단순화.
  * 서비스 간 결합도(Coupling)를 방지하기 위해 `mopl-content`의 `ContentCompositeService`가 조율자가 되어 **순차 호출(Sequential Orchestration)** 수행.
  * 2~3초 Deadline 타임아웃을 적용하여 장애 발생 시 즉각적인 감지 및 보상(Rollback) 수행.


### 3. 트랜잭션 흐름 및 순차 체이닝

```text
[정상 시나리오]
ContentCompositeService.delete(contentId)
  │
  ├─ 1. gRPC DeleteCurations (Deadline 3s) ──> mopl-playlist (Curation & View 삭제 성공)
  │
  ├─ 2. gRPC DeleteWatchingSessions (Deadline 3s) ──> mopl-live-chat (세션 정리 성공)
  │
  └─ 3. 외부 작업 완료 후 로컬 DB markAsDeleted 커밋 (0.001초 짧은 트랜잭션) ──> 204 No Content

[Step 1 (Playlist) 실패 시]
ContentCompositeService.delete(contentId) ──> Playlist 실패
  │
  └─ 로컬 DB는 수정하지 않고 즉시 500 예외 반환 (LiveChat은 미호출 상태이므로 안전)

[Step 2 (LiveChat) 실패 시 - 보상 트랜잭션 발동]
ContentCompositeService.delete(contentId)
  ├─ Playlist 성공
  ├─ LiveChat 실패 / DeadlineExceeded
  │
  └─ [보상 트랜잭션 즉시 실행]
       ├─ gRPC RestoreCurations ──> mopl-playlist (백업된 Curation & View 복구)
       └─ 로컬 DB는 변경 없이 500 실패 예외 반환
```


### 4. 주요 특징 및 예외 대응

1. **DB 커넥션 풀(HikariCP) 보호 & 트랜잭션 분리**:
   * 외부 네트워크 gRPC 통신 중에는 DB 커넥션을 점유하지 않음.
   * 모든 외부 통신이 성공한 시점에만 `contentCommandService.delete()`를 통해 0.001초 만에 로컬 DB 커밋.
2. **단순화된 상태 머신 (Soft Lock `DELETING` 불필요)**:
   * 동기 통신으로 1초 이내에 성공/실패가 판가름 나므로, 중간 상태인 `DELETING`을 두지 않고 `ACTIVE → DELETED`로 직관적 전이.
   * 로컬 DB를 맨 마지막에 갱신하므로 중간 실패 시 로컬 DB 원복(`restoreActive`) 자체가 불필요.
3. **동기식 즉각 보상(Immediate Compensation)**:
   * 3분 스케줄러 대기 없이, Step 2 실패 즉시 `RestoreCurations`를 호출하여 Playlist 원상 복구.
   * `mopl-playlist`는 삭제 직전 데이터를 캐시에 백업해 두어 보상 호출 시 완벽 복원.
4. **네트워크 장애 및 지연 대응 (Deadline)**:
   * 각 gRPC 호출에 3초 Deadline을 설정하여 상대 서버 장애 시 무한 대기(Hang) 방지.
5. **인프라 극적 단순화**:
   * 불필요해진 Debezium CDC, Kafka 브로커, Outbox 및 Inbox 테이블, 스케줄러 전면 제거.
6. **ContentStatus.DELETING 제거**
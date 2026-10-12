### 1. 토픽 및 파티션 현황

- **`user.profile.events`**
    - 기본 파티션 수: 1 (기본값)
    - 복제본 수: 1
    - 파티션 키: `userId`
    - 주요 역할: 1:N CQRS 데이터 동기화 (Pub/Sub), 사용자 프로필 생성/수정 이벤트 순서 보장 전송
- **`content.saga.delete.start`**
    - 기본 파티션 수: 1 (기본값)
    - 복제본 수: 1
    - 파티션 키: `sagaId`
    - 주요 역할: Debezium CDC를 통한 Saga 트랜잭션 시작 이벤트
- **`content.saga.delete.response`**
    - 기본 파티션 수: 1 (기본값)
    - 복제본 수: 1
    - 파티션 키: `sagaId`
    - 주요 역할: 하위 서비스들의 Saga 처리 응답 이벤트

- **파티셔닝 설계 현황**:
    - 프로듀서에서 `userId` 및 `sagaId`를 메시지 키로 전송하여 **동일 엔티티 기준 순서 보장(Ordering Guarantee)**의 기반은 갖추어져 있음.
    - 현재는 토픽 파티션 수가 1개로 운영되고 있어, 추후 처리량 확장 및 병렬 소비 실습을 위해 파티션 증설(예: 3개) 및 리스너 `concurrency` 조정 대상임.

### 2. 컨슈머 및 컨슈머 그룹 현황

각 마이크로서비스는 목적에 따라 고유한 **Consumer Group ID**를 부여받아 독립적으로 이벤트를 소비(Pub/Sub 및 분산 트랜잭션)하고 있음.

- **`user.profile.events`**
  - `content-profile-group` (`mopl-content` / `ProfileEventListener`): 로컬 CQRS 유저 뷰 동기화
  - `playlist-profile-group` (`mopl-playlist` / `UserProfileEventListener`): 플레이리스트 작성자 정보 동기화
  - `dm-profile-group` (`mopl-dm` / `UserProfileEventListener`): DM 상대방 프로필 정보 동기화
  - `notification-profile-group` (`mopl-notification` / `UserEventConsumer`): 알림 수신자 프로필 정보 동기화
- **`content.saga.delete.start`**
  - `playlist-service-group` (`mopl-playlist` / `ContentEventListener`): 콘텐츠 삭제 시 재생목록 내 콘텐츠 정리
  - `live-chat-group` (`mopl-live-chat` / `WatchingSessionEventListener`): 콘텐츠 삭제 시 시청 세션 및 채팅방 정리
- **`content.saga.delete.response`**
  - `content-service-group` (`mopl-content` / `ContentSagaEventListener`): 하위 서비스 결과 수신 및 Saga 완료 처리

- **특징**
  - 동일 토픽(`user.profile.events`)을 4개 그룹이 각각 구독하여 1:N Pub/Sub(브로드캐스팅) 보장.
  - 모든 리스너의 동시성(`concurrency`)은 현재 기본값(1)으로 설정되어 단일 스레드로 동작.

### 3. 브로커 및 클러스터 인프라 현황

`docker-compose.yml` 기준

- **클러스터 모드**: **KRaft (Kafka Raft)** (ZooKeeper 없이 실행되는 최신 카프카 아키텍처 `cp-kafka:7.6.0`)
- **브로커 수**: **1대 (단일 노드)** (Node ID: 1, Broker와 Controller 역할을 단일 프로세스에서 겸임)
- **클러스터 ID**: `MkU3OEVBNTcwNTJENDM2Qk` (고유 클러스터 메타데이터 식별자)
- **리스너 (포트)**:
  - 내부: `kafka:9092`
  - 외부: `localhost:29092` (컨테이너 내부 및 로컬 호스트 통신 분리)
- **연계 인프라**: `mopl-kafka-connect` (Port 8089/8083, Debezium CDC Connector 탑재)
- **데이터 볼륨**: `kafka-data:/var/lib/kafka/data` (브로커 메시지 세그먼트 파일 영속화)
- **Replication Factor**: 1 (단일 브로커 구성에 맞춘 단일 복제본)
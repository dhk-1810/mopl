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
- **`image.presigned-url.events`**
    - 기본 파티션 수: 1 (기본값)
    - 복제본 수: 1
    - 파티션 키: `imageKey`
    - 주요 역할: S3 Presigned URL 생성 완료 브로드캐스팅 (`mopl-image` -> 타 서비스들의 CQRS `ExternalImageView` 갱신)
- **`content.metadata.events`**
    - 기본 파티션 수: 1 (기본값)
    - 복제본 수: 1
    - 파티션 키: `contentId`
    - 주요 역할: 콘텐츠 메타데이터 수정 이벤트 동기화 (`mopl-content` -> `mopl-playlist`의 `ExternalContentView` 갱신)
- **`user.follow.events`**
    - 기본 파티션 수: **3**
    - 복제본 수: 1
    - 파티션 키: `followeeId`
    - 주요 역할: 사용자 팔로우/언팔로우 이벤트 동기화 (`mopl-user` -> `mopl-playlist`의 `ExternalFollowView` 추가/삭제 반영)

- **파티셔닝 설계 현황**:
    - 프로듀서에서 `userId`, `sagaId`, `imageKey`, `contentId`, `followeeId`를 메시지 키로 전송하여 **동일 엔티티 기준 순서 보장(Ordering Guarantee)**의 기반은 갖추어져 있음.
    - **`user.follow.events` 파티셔닝 적용 완료**: 파티션 수를 3개로 확장하고 컨슈머 리스너의 동시성(`concurrency = 3`)을 적용하여 3개의 워커 스레드가 파티션을 1:1로 병렬 소비하도록 구성 완료.
    - 나머지 토픽들은 단일 파티션(1개)으로 운영 중이며 트래픽에 따라 확장 가능.

### 2. 컨슈머 및 컨슈머 그룹 현황

각 마이크로서비스는 목적에 따라 고유한 **Consumer Group ID**를 부여받아 독립적으로 이벤트를 소비(Pub/Sub 및 분산 트랜잭션)하고 있음.

- **`user.profile.events`**
  - `content-profile-group` (`mopl-content` / `UserEventListener`): 로컬 CQRS 유저 뷰 동기화
  - `playlist-profile-group` (`mopl-playlist` / `UserEventListener`): 플레이리스트 작성자 정보 동기화
  - `dm-profile-group` (`mopl-dm` / `UserEventListener`): DM 상대방 프로필 정보 동기화
  - `notification-profile-group` (`mopl-notification` / `UserEventConsumer`): 알림 수신자 프로필 정보 동기화


- **`content.saga.delete.start`**
  - `playlist-service-group` (`mopl-playlist` / `ContentEventListener`): 콘텐츠 삭제 시 재생목록 내 콘텐츠 정리
  - `live-chat-group` (`mopl-live-chat` / `WatchingSessionEventListener`): 콘텐츠 삭제 시 시청 세션 및 채팅방 정리


- **`content.saga.delete.response`**
  - `content-service-group` (`mopl-content` / `ContentSagaEventListener`): 하위 서비스 결과 수신 및 Saga 완료 처리


- **`image.presigned-url.events`**
  - `user-image-group` (`mopl-user` / `ExternalImageEventListener`): 유저 서비스 이미지 캐시 뷰 동기화
  - `content-image-group` (`mopl-content` / `ExternalImageEventListener`): 콘텐츠 서비스 이미지 캐시 뷰 동기화
  - `dm-image-group` (`mopl-dm` / `ExternalImageEventListener`): DM 서비스 이미지 캐시 뷰 동기화


- **`content.metadata.events`**
  - `playlist-content-group` (`mopl-playlist` / `ContentEventListener`): 플레이리스트 서비스 콘텐츠 메타데이터 복제 뷰 동기화


- **`user.follow.events`**
  - `playlist-follow-group` (`mopl-playlist` / `FollowEventListener`): 플레이리스트 서비스 팔로우 관계 복제 뷰 동기화 (`concurrency = 3` 적용)


- **특징**
  - 동일 토픽(`user.profile.events`, `image.presigned-url.events`)을 다수 그룹이 각각 구독하여 1:N Pub/Sub(브로드캐스팅) 보장.
  - `user.follow.events` 리스너는 `concurrency = 3`으로 설정되어 3개의 워커 스레드가 3개 파티션을 1:1로 매핑하여 병렬 처리함. (그 외 리스너는 기본 동시성 1로 동작)

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
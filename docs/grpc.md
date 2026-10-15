# gRPC 통신 아키텍처 및 현황

## 1. 서비스별 gRPC 포트 및 역할 매핑

| 서비스 모듈 | HTTP 포트 | gRPC 포트 | gRPC 역할 | 호출 대상 서비스 (Client) |
|---|---|---|---|---|
| **mopl-user** | 8081 | 8181 | 서버 + 클라이언트 | `mopl-image` |
| **mopl-content** | 8082 | 8182 | 서버 + 클라이언트 | `mopl-user`, `mopl-playlist`, `mopl-live-chat`, `mopl-image` |
| **mopl-live-chat** | 8083 | 8183 | 서버 + 클라이언트 | `mopl-user`, `mopl-content`, `mopl-image` |
| **mopl-playlist** | 8084 | 8184 | 서버 + 클라이언트 | `mopl-user`, `mopl-content`, `mopl-image` |
| **mopl-dm** | 8085 | - | 클라이언트 | `mopl-user`, `mopl-image` |
| **mopl-notification** | 8086 | - | 클라이언트 | `mopl-user` |
| **mopl-image** | 8087 | 8185 | 서버 | - |
| **mopl-batch** | 8088 | - | 클라이언트 | `mopl-content` |


## 2. Proto 명세 및 RPC 서비스

### 1) UserService (`user.proto`)
- **패키지**: `org.codeit.sb06.team03.mopl.grpc.user`
- **제공 서비스**: `mopl-user` (포트: 8181)
- **RPC 메서드**:
  - `GetUser(GetUserRequest) returns (UserResponse)`: 사용자 ID 기반 상세 프로필(식별자, 이메일, 이름, 프로필 이미지 키, 권한, 잠금 상태, 생성일시) 단건 동기 조회

### 2) ImageService (`image.proto`)
- **패키지**: `org.codeit.sb06.team03.mopl.grpc.image`
- **제공 서비스**: `mopl-image` (포트: 8185)
- **RPC 메서드**:
  - `GetPresignedUrl(GetPresignedUrlRequest) returns (GetPresignedUrlResponse)`: 이미지 스토리지 키 기반 Presigned URL 및 만료 시각(`exp`) 조회 및 Lazy 발급

### 3) ContentService (`content.proto`)
- **패키지**: `org.codeit.sb06.team03.mopl.grpc.content`
- **제공 서비스**: `mopl-content` (포트: 8182)
- **RPC 메서드**:
  - `GetContent(GetContentRequest) returns (ContentResponse)`: 컨텐츠 ID 기반 단건 메타데이터(유형, 제목, 설명, 썸네일 URL, 태그 목록, 평점, 리뷰 수, 시청자 수) 조회
  - `CheckContentExists(CheckContentExistsRequest) returns (CheckContentExistsResponse)`: 컨텐츠 제목 및 타입 기준 존재 여부 실시간 확인
  - `BatchCreateContents(BatchCreateContentsRequest) returns (BatchCreateContentsResponse)`: 외부 수집 컨텐츠 대량 일괄 등록 및 생성 건수 반환

### 4) PlaylistService (`playlist.proto`)
- **패키지**: `org.codeit.sb06.team03.mopl.grpc.playlist`
- **제공 서비스**: `mopl-playlist` (포트: 8184)
- **RPC 메서드**:
  - `DeleteCurations(DeleteCurationsRequest) returns (DeleteCurationsResponse)`: 특정 컨텐츠와 연관된 플레이리스트 담음 정보(Curation) 및 로컬 복제 뷰(`ExternalContentView`) Soft Delete
  - `RestoreCurations(RestoreCurationsRequest) returns (RestoreCurationsResponse)`: 분산 트랜잭션 실패 시 보상 트랜잭션으로 백업된 큐레이션 및 로컬 복제 뷰 복원

### 5) WatchingSessionService (`watching_session.proto`)
- **패키지**: `org.codeit.sb06.team03.mopl.grpc.watchingsession`
- **제공 서비스**: `mopl-live-chat` (포트: 8183)
- **RPC 메서드**:
  - `DeleteWatchingSessions(DeleteWatchingSessionsRequest) returns (DeleteWatchingSessionsResponse)`: 삭제 대상 컨텐츠와 연동된 실시간 라이브 시청 세션 정리


## 3. 핵심 사용 사례

### 1) CQRS 로컬 복제 뷰 동기 Fallback & 이미지 Lazy 서빙
- **사용자 정보 Fallback (`UserService.GetUser`)**:
  - 타 서비스(`mopl-content`, `mopl-playlist`, `mopl-dm`, `mopl-notification`, `mopl-live-chat`)는 Kafka 이벤트 기반 비동기 복제 테이블(`ExternalUserView`, `ExternalProfileView`)을 유지함.
  - 이벤트 유실이나 지연으로 인해 로컬 복제 테이블에 사용자가 존재하지 않는 경우(Cache-Miss), `UserService.GetUser` gRPC를 동기 호출하여 즉시 데이터를 조회하고 로컬 DB에 자동 적재.
- **컨텐츠 메타데이터 Fallback (`ContentService.GetContent`)**:
  - `mopl-playlist`의 `ExternalContentView`에 수록 컨텐츠 정보가 없는 경우, `ContentService.GetContent` gRPC를 통해 최신 메타데이터를 가져와 로컬 캐시를 보충.
- **이미지 Presigned URL 실시간 획득 및 갱신 (`ImageService.GetPresignedUrl`)**:
  - 각 서비스의 `ExternalImageView` 레코드가 없거나 URL이 만료(`isExpired()`)된 경우, `mopl-image`에 gRPC 호출.
  - `mopl-image`는 캐시 확인 후 만료 시 S3 API를 호출하여 신규 Presigned URL을 즉시 생성/저장하고, 타 서비스 동기화를 위해 Kafka(`image-presigned-url-events`) 브로드캐스트 수행.

### 2) Saga 분산 트랜잭션 동기 오케스트레이션 (컨텐츠 삭제)
- **오케스트레이터**: `mopl-content` (`GrpcContentDeletionSagaStrategy`)
- **실행 흐름 (Deadline: 3초 타임아웃 적용)**:
  1. `PlaylistService.DeleteCurations` 호출 ──> `mopl-playlist` (Curation 및 복제 뷰 Soft Delete)
  2. `WatchingSessionService.DeleteWatchingSessions` 호출 ──> `mopl-live-chat` (시청 세션 정리)
  3. 모든 원격 서비스 정리 완료 후 `mopl-content` 로컬 DB 상태 변경 (`contentCommandService.delete`)
- **즉각 보상 트랜잭션 (Immediate Compensation)**:
  - Step 2 호출 실패 혹은 타임아웃 발생 시, 즉시 `PlaylistService.RestoreCurations`를 호출하여 Step 1에서 삭제된 큐레이션을 완벽 복구.
- **커넥션 풀(HikariCP) 보호**:
  - 외부 gRPC 네트워크 호출 중에는 로컬 DB 트랜잭션을 잡지 않으며, 모든 외부 조율이 성공한 마지막 순간에만 짧은 트랜잭션(0.001초)으로 로컬 커밋 수행.

### 3) 대용량 배치 데이터 중복 확인 및 일괄 적재
- `mopl-batch`에서 외부 API(TMDB, TheSportsDB)로부터 콘텐츠를 수집할 때:
  - `ContentService.CheckContentExists`를 호출하여 중복 수집 방지.
  - 신규 수집 데이터를 청크 단위로 묶어 `ContentService.BatchCreateContents`로 일괄 전송함으로써 네트워크 오버헤드 최소화.
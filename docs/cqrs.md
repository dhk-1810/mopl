## 서비스별 CQRS 복제 테이블 현황
### 사용 목적
1. 가용성 향상
2. 응답 속도 향상

### 주의사항
- 생성, 수정, 삭제 발생 시 다른 서버에도 일관성 보장되어야 함.
- 즉시/지연 반영 중 상황에 맞게 적절한 방식 선택 필요.
- 찾는 레코드가 없는 경우 내부통신으로 가져와야 함. (gRPC)
- 변경 반영까지 시차 존재.

### 1. mopl-user (사용자)
* **timeout_images** (`ExternalImageView`): `mopl-image`의 Presigned URL 캐시 동기화

### 2. mopl-content (컨텐츠)
* **external_user_views** (`ExternalUserView`): `mopl-user`의 사용자 프로필(이름, 프사키) 복제
* **timeout_images** (`ExternalImageView`): `mopl-image`의 이미지 Presigned URL 동기화

### 3. mopl-playlist (플레이리스트)
* **external_user_views** (`ExternalUserView`): `mopl-user`의 플레이리스트 소유자 프로필 복제
* **external_follow_views** (`ExternalFollowView`): `mopl-user`의 팔로우 관계 복제
* **external_content_views** (`ExternalContentView`): `mopl-content`의 수록 컨텐츠 메타데이터 복제
* **timeout_images** (`ExternalImageView`): `mopl-image`의 이미지 Presigned URL 동기화

### 4. mopl-dm (DM)
* **external_user_views** (`ExternalUserView`): `mopl-user`의 대화 상대방 및 본인 프로필 복제
* **timeout_images** (`ExternalImageView`): `mopl-image`의 프로필 이미지 Presigned URL 동기화

### 5. mopl-notification (알림)
* **external_user_views** (`ExternalUserView`): `mopl-user`의 알림 대상/행위자 프로필 복제

### 6. mopl-watching-session (시청 세션)
* **external_profile_views** (`ExternalProfileView`): `mopl-user`의 시청자 프로필 정보 복제
* **timeout_images** (`ExternalImageView`): `mopl-image`의 프로필 이미지 Presigned URL 동기화

### gRPC 사용처

- mopl-user                  | 8081       | 서버                               | 8181
- mopl-content               | 8082       | 서버 + 클라이언트 (User 호출)          | 8182
- mopl-live-chat             | 8083       | 서버 + 클라이언트 (User 호출)          | 8103
- mopl-playlist              | 8084       | 서버 + 클라이언트 (User, Content 호출) | 8184
- mopl-dm                    | 8085       | 클라이언트 (User 호출)                | -
- mopl-notification          | 8086       | 클라이언트 (User 호출)                | -
- mopl-image                 | 8087       | -                                 | -
- mopl-batch                 | 8088       | 클라이언트 (Content 호출)


- **배치 작업 결과 및 중복 확인** (`ContentService.CheckContentExists`, `ContentService.BatchCreateContents`)
  - `mopl-batch`에서 수집한 데이터의 중복 여부를 `CheckContentExists` gRPC로 실시간 확인.
  - 중복되지 않은 신규 콘텐츠를 청크 단위로 `BatchCreateContents` gRPC를 통해 `mopl-content`로 일괄 전송 및 DB 저장.


- **Saga 분산 트랜잭션 오케스트레이션 및 즉각 보상 트랜잭션**
  - 컨텐츠 삭제 조율 (`PlaylistGrpcService`, `WatchingSessionGrpcService`)
    - `mopl-content`가 오케스트레이터가 되어 `mopl-playlist`(Curation 삭제)와 `mopl-live-chat`(세션 정리)을 순차 동기 호출.
    - Step 2 실패 시 즉시 `restoreCurations`를 호출하여 캐시 백업 데이터 기반 롤백 수행.
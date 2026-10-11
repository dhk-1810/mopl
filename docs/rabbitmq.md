### RabbitMQ 사용처

- 실시간 알림(Notification) 이벤트 전파
  - 팔로우, 권한 변경, DM 수신, 플레이리스트 구독/생성 알림을 mopl-notification으로 전달.
- 백그라운드 작업 큐 (Task Worker Queue)
    - 이미지 업로드 및 변환 (mopl.image.upload)
      - 각 서비스에서 이미지를 올리면 mopl-image 서비스가 큐에서 소비하여 후처리.
    - 워칭세션 비동기 기록 (watching-session.create / delete)
      - 웹소켓 연결/해제 시 mopl-live-chat 내부에서 Redis/DB 기록 작업을 큐로 비동기 분리.
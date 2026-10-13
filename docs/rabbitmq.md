### RabbitMQ 사용처

- 실시간 알림 이벤트 전파 (1:1)
  - 팔로우, 권한 변경, DM 수신, 플레이리스트 구독/생성 알림을 mopl-notification으로 전달.
- 백그라운드 작업 큐 (Task Worker Queue)
  - 이미지 업로드 및 변환 (mopl.image.upload)
    - 각 서비스에서 이미지를 올리면 mopl-image 서비스가 큐에서 소비하여 후처리.
  - 로그아웃/토큰 무효화 시 시청세션 정리 (watching-session.delete)
    - mopl-user에서 로그아웃 발생 시 mopl-live-chat으로 전달하여 잔여 시청 세션 삭제 및 퇴장 처리.
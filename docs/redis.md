### 1. Redis 사용처

- mopl-user : JWT Refresh Token 저장, 이메일 인증번호/임시 비밀번호 캐싱
- mopl-watching-session : Watching Session 상태 관리, Inbox 멱등성 키(inbox:saga-start:*) 관리
- mopl-notification : SSE 연결 클라이언트 관리 및 알림 캐싱

### 2. Redis + RabbitMQ로 분산 환경에서의 SSE 알림 전파 구현하기

- Redis Pub/Sub만으로 SSE 알림 전파를 구현하면, 모든 인스턴스가 알림 발생 시 자기 메모리의 `SseEmitter`를 뒤져야 함.
- 알림이 잦아질수록 성능 저하 가능성 커짐.
- RabbitMQ의 정교한 라우팅 기능으로 이 오버헤드를 없앨 수 있음. 

**동작 방식**
1. 각 서버 인스턴스가 뜰 때 자신만의 고유한 큐(Queue)를 선언.
   (예: `queue.server-A`, `queue.server-B`).

2. 사용자가 특정 서버에 SSE 연결을 맺으면, **Redis에 라우팅 정보를 저장**.
   `userId:100 -> server-A`

3. 알림 발송 시
- Redis에서 해당 유저가 어느 서버에 붙어있는지 확인 (`server-A`)
- RabbitMQ에 라우팅 키로 `server-A`를 지정하여 발행
- **`server-A` 인스턴스 딱 1대만 해당 메시지를 수신**하여 SSE로 알림 발송
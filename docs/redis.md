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

### 3. 샤딩 (Redis Cluster)

- **클러스터 구조**: Master 노드 3대 (`7001`, `7002`, `7003`)로 16,384개 해시 슬롯을 균등 분할.
- **키 설계 및 Hash Tag 샤딩 원리**:
  - 세션 키 포맷: `notification:session:{userId}` (Set)
  - Redis Cluster는 중괄호 `{...}` 내부의 문자열을 기반으로 CRC16 해시를 계산하여 슬롯(0~16383)을 결정.
  - 특정 유저(`userId`)의 인스턴스 세션 목록(`Set<instanceId>`)은 해당 유저가 할당된 특정 Redis 샤드 노드에만 저장/조회됨.
  - 다중 서버가 동시에 서로 다른 유저에 대한 세션을 읽고 쓰더라도, 요청이 각 Redis 노드로 고르게 분산되어 단일 Redis의 메모리 및 I/O 병목을 해소.
- **Cross-Slot 배제 설계**:
  - 클러스터 환경에서 멀티 키 연산(`MGET`, Cross-slot 트랜잭션 등) 시 발생하는 
  - `CROSSSLOT Keys in request don't hash to the same slot` 에러를 원천 방지하기 위해, 
  - 모든 세션 접근은 단일 유저 키 단위(`notification:session:{userId}`)로만 수행됨.

  - 실행 및 샤딩 테스트 방법
    1. 도커 컨테이너 기동:
       docker compose up -d notification-redis-1 notification-redis-2 notification-redis-3 notification-redis-cluster-init rabbitmq

    2. 클러스터 상태 확인:
       docker exec -it mopl-notification-redis-1 redis-cli -p 7001 -a moplnotificationpwd123 cluster nodes
       (3개 노드에 슬롯 0-5460, 5461-10922, 10923-16383이 분할 할당됨)
    
    3. Notification 서비스 실행 및 다중 인스턴스 SSE 교차 전송 확인:
       - 1번 인스턴스: ./gradlew :mopl-notification:bootRun --args='--server.port=8084'
       - 2번 인스턴스: ./gradlew :mopl-notification:bootRun --args='--server.port=8085'
       - 유저들이 SSE 연결을 맺으면 각 유저의 notification:session:{userId} 키가 
       - 7001, 7002, 7003 노드로 고르게 분산 저장되고, 알림 발생 시 타겟 인스턴스로 정확히 라우팅됩니다.
## CDC 구현해보기
### 0. CDC란?
- Change Data Capture.
- 아웃박스 테이블에 새 이벤트가 생성되었는지 폴링으로 확인하는 대신, 
- 실시간으로 DB 엔진의 트랜잭션 로그(binlog, WAL 등)를 감지해 메시지 큐 등으로 전달. 

### **1. 인프라 구성 (`docker-compose.yml`)** 

**Kafka KRaft 추가 (Broker + Controller 통합)**
  - Connect가 밀어 넣은 이벤트들을 안전하게 보관하고, 다른 마이크로서비스들이 소비할 수 있게 분배해주는 허브.
  - 토픽별 파티션에 메시지 디스크 영속화
  - 순서 보장 (Ordering) 및 고성능 분산 큐잉
  - Connect 자체의 설정/상태/오프셋 메타데이터 저장

**Kafka Connect 추가**
  - PostgreSQL WAL을 실시간으로 긁어와서 입맛대로 가공(SMT)해 카프카로 밀어 넣어주는 엔진.
  (Postgres 드라이버와 Debezium 엔진 내장)
  - DB 연결 및 WAL 파싱
  - Outbox Event Router (메시지 변환/토픽명 결정)
  - 어디까지 읽었는지(Offset) 관리

  - Kafka Connect(Debezium)는 커넥터 설정과 CDC 진행 위치(오프셋)를 Kafka 내부 토픽에 저장함.
    - docker compose down으로 컨테이너 삭제 시 재등록 필요.
    - 볼륨 마운트로 영속화 가능.

**DB(PostgreSQL/MySQL) 설정**
  - PostgreSQL인 경우: `wal_level = logical` 설정
  - MySQL인 경우: `binlog_format = ROW` 설정

```
[mopl-content 서비스]
       │
       │  (1. 트랜잭션 내 outbox_events 테이블에 INSERT)
       ▼ 
[PostgreSQL: content-db (wal_level=logical)]
       │
       │  (2. PostgreSQL WAL 변경 실시간 감지)
       ▼ 
[Kafka Connect: debezium/connect:2.6]  <--- "일꾼 (Connector & SMT)"
       │  (3. Kafka 토픽으로 프로듀싱)
       │ ──> Outbox Event Router SMT
       │       1. routing_key 컬럼값 -> Kafka Topic 이름으로 라우팅
       │       2. payload 컬럼의 JSON -> Kafka 메시지 본문(Value)으로 언래핑
       │       3. aggregate_id 컬럼 -> Kafka 파티션 Key로 설정
       │       4. event_type 컬럼 -> Kafka Header(eventType)로 주입
       ▼
[Kafka Broker: confluentinc/cp-kafka:7.6.0]  <--- "저장소 및 허브 (Broker)"
       │  (4. 이벤트 컨슘)
       ├── Topic: `content.event.updated`
       ├── Topic: `content.event.deleted`
       └── Topic: `content.saga.start`
```
### 2. 설정 파일 생성

- `docker/debezium/register-connectors.sh`
  - Kafka Connect 서버(localhost:8083)가 정상 기동할 때까지 대기(Health Check) 후,
        content-outbox-connector.json 설정을 REST API로 등록/갱신하는 쉘 스크립트

- `docker/debezium/content-outbox-connector.json`
  - 어떤 DB의 어떤 테이블을 어떻게 가공해서 어떤 Kafka 토픽으로 쏠지 지정하는 작업 지시서.
  - outbox_events 테이블의 INSERT 변경 사항만 감지
  - Debezium의 Outbox Event Router SMT를 적용하여
  - routing_key 컬럼값(예: content.event.updated, content.saga.start)을 Kafka의 목적지 Topic 이름으로 자동 지정
  - payload 컬럼의 JSON 내용을 Kafka 메시지의 순수 Body(Value)로 언래핑
  - event_type, aggregate_type 등을 Kafka Header로 주입

### 3. 폴러 클래스 비활성화, 보상 트랜잭션의 RabbitMQ 이벤트 발행을 Kafka로 대체.

### 4. 사용 방법

**1) 인프라 기동**

`docker compose up -d content-db kafka kafka-connect`

**2) 커넥터 등록**

Kafka Connect 서버가 기동된 후 아래 스크립트를 실행.

`./docker/debezium/register-connectors.sh`

**3) 커넥터 상태 확인**

```bash
# 등록된 커넥터 목록 조회
curl http://localhost:8083/connectors

# 커넥터 실행 상태(RUNNING 여부) 상세 조회
curl http://localhost:8083/connectors/content-outbox-connector/status
```

**4) Kafka 토픽 수신 확인 (CLI 테스트)**

`outbox_events` 테이블에 데이터가 들어갔을 때 정상적으로 토픽에 수신되는지 확인하는 명령어:

```bash
docker exec -it mopl-kafka kafka-console-consumer \  --bootstrap-server localhost:9092 \  --topic content.event.updated \  --from-beginning \  --property print.key=true \  --property print.headers=true
```

**5) 커넥터 삭제 및 재등록 (설정 변경 시)**

```bash
curl -X DELETE http://localhost:8083/connectors/content-outbox-connector./docker/debezium/register-connectors.sh
```


## DB Master-Slave 분리 로컬 구현하기

- DB 읽기 트래픽이 많을 때 slave(읽기 DB)를 여러개 구성해 트래픽을 분산.
- Master에 쓰기 발생 시 WAL을 복제해 slave들에 반영.
- `mopl-user`에 실험 차 구현 완료. (master 1, slave 1) 

### 1. 설정 변경 : `docker-compose.yml에` slave 컨테이너 추가

### 2. 단일 DataSource 설정을 master와 slave로 분리

- 예시 구조
    ```yaml
    spring:
      datasource:
        user:
          master:
            driver-class-name: org.postgresql.Driver
            jdbc-url: jdbc:postgresql://${DB_MASTER_HOST:localhost}:${DB_MASTER_PORT:5432}/${POSTGRES_DB}
            username: ${POSTGRES_USER}
            password: ${POSTGRES_PASSWORD}
          slave:
            driver-class-name: org.postgresql.Driver
            jdbc-url: jdbc:postgresql://${DB_SLAVE_HOST:localhost}:${DB_SLAVE_PORT:5433}/${POSTGRES_DB}
            username: ${POSTGRES_USER}
            password: ${POSTGRES_PASSWORD}
    ```


### 3. Spring Routing DataSource 구현

Spring Transaction의 `readOnly` 속성에 따라 동적으로 DataSource를 선택하는 컴포넌트 작성.

1. DataSource 타입 구분용 Enum 정의 (`MASTER`, `SLAVE`)
2. `AbstractRoutingDataSource` 구현체 작성
   - `determineCurrentLookupKey()`에서 `TransactionSynchronizationManager.isCurrentTransactionReadOnly()`가 `true`면 slave, `false`면 master를 반환.
     - `@Transactional(readOnly = true)` → isReadOnly = true → SLAVE (5431)
     - `@Transactional` → isReadOnly = false → MASTER (5432)
     - `@Transactional`이 없는 경우 → isReadOnly = false (기본값) → MASTER (5432)

3. `LazyConnectionDataSourceProxy` 래핑 (중요)
   - Spring은 트랜잭션 진입 시점에 기본 커넥션을 먼저 가져오려고 하므로,
   - 쿼리가 실행되는 시점까지 실제 커넥션 획득을 지연시켜
   - `@Transactional(readOnly = ...)` 속성이 적용된 후 라우팅되도록 설정.


### 4. UserDbConfig.java 수정
- 단일 DataSource로 구성된 빈 설정을 복수 DataSource 및 라우팅 설정으로 변경.
- masterDataSource 및 slaveDataSource 빈 생성 (HikariDataSource)
- routingDataSource에 TargetDataSources 매핑 등록 (master, slave) 및 DefaultTargetDataSource 지정
- lazyConnectionDataSource 빈을 생성하여 userEntityManagerFactory의 dataSource로 주입

### 5. Master → Slave WAL 복제(동기화) 구현

- Master DB 초기화 스크립트 작성
  - `init-master.sh`
    - 복제 전용 사용자(replicator) 생성 및 pg_hba.conf에 복제 접속 허용
- Slave DB 엔트리포인트 스크립트 작성 
  - `entrypoint-slave.sh`
    - Master가 뜰 때까지 대기 후 pg_basebackup으로 초기 데이터를 동기화하고 대기 모드(standby)로 기동
- `docker-compose.yml` 수정
  - Master DB: wal_level=replica, max_wal_senders=10,
    - hot_standby=on 파라미터 적용
  - Slave DB: Master 헬스체크 후 엔트리포인트 스크립트 실행

### 6. 점검, 주의사항

- Service 계층 `@Transactional(readOnly = true)` 점검
    - 조회 쿼리 수행 메서드/서비스에 누락 없는지 확인. `readOnly = true` 없으면 기본값인 master로 라우팅됨.
- 쿼리 실행 시 어떤 DataSource의 커넥션이 획득되는지 확인하는 통합 테스트 또는 로깅 추가
  - CUD 작업 시 Master DataSource 연결 확인
  - R 작업 시 Slave DataSource 연결 확인
    - 주의) Replication Lag
      - Master에 쓰기 직후 즉시 같은 요청 내에서 조회하거나, 복제 지연이 발생할 수 있는 경우 예외 처리

### 7. 확장 시 (Slave 추가)

1. `docker-compose.yml`
    - user-slave-db-2 서비스 추가 (포트 5430:5432)
    
2. `application-dev.yml`
   - slave를 단일 객체 대신 리스트(slaves) 또는 slave-1, slave-2로 분리:

    ```yaml
    spring:
      datasource:
        user:
          master:
            url: jdbc:postgresql://localhost:5432/mopl_user
          slave-1:
            url: jdbc:postgresql://localhost:5431/mopl_user
          slave-2:
            url: jdbc:postgresql://localhost:5430/mopl_user
    ```

3. Java 코드 (RoutingDataSource.java 및 UserDbConfig.java)
    
   - RoutingDataSource에 Round-Robin(순환 분산) 로직 추가:
        
   ```java
    public class RoutingDataSource extends AbstractRoutingDataSource {
        		
        private final AtomicInteger counter = new AtomicInteger(0);
        private final List<DataSourceType> slaveKeys = List.of(DataSourceType.SLAVE_1, DataSourceType.SLAVE_2);
            
        @Override
        protected Object determineCurrentLookupKey() {
            boolean isReadOnly = TransactionSynchronizationManager.isCurrentTransactionReadOnly();
            if (!isReadOnly) {
                return DataSourceType.MASTER;
            }
        
            // Slave 간에 번갈아가며 균등 분산 (Round-Robin) 분산 (Round-Robin)
            int index = Math.abs(counter.getAndIncrement() % slaveKeys.size());
            return slaveKeys.get(index);
        }
    }
    ```
   

### 8. 어디로 보낼지 어떻게 판별하는가? (심화)

- TransactionSynchronizationManager의 원리
  - Spring에서 메서드에 @Transactional 어노테이션이 선언되면 다음과 같은 일이 일어남.
    1. AOP 인터셉트
       - Spring의 TransactionInterceptor가 메서드 호출을 가로챔.
  
    2. 트랜잭션 정보 저장
       - 어노테이션에 지정된 속성(readOnly = true 여부 등)을 현재 실행 중인 스레드의 ThreadLocal(TransactionSynchronizationManager)에 저장.
  
    3. 판별
       - RoutingDataSource.java의 determineCurrentLookupKey()에서 
       - TransactionSynchronizationManager.isCurrentTransactionReadOnly()를 호출하면, 
       - ThreadLocal에 저장됐던 readOnly 플래그를 읽어옴.

       ```java
       @Override
          protected Object determineCurrentLookupKey() {
             // 현재 스레드의 트랜잭션이 readOnly인지 확인
             boolean isReadOnly = TransactionSynchronizationManager.isCurrentTransactionReadOnly();
      
             return isReadOnly ? DataSourceType.SLAVE : DataSourceType.MASTER;
          }
        ```

### 9. LazyConnectionDataSourceProxy

Spring 트랜잭션과 JPA의 기본 동작 순서 (LazyConnectionDataSourceProxy가 없을 때)
1. @Transactional 진입
2. 트랜잭션 시작과 동시에 DB 커넥션을 획득하려고 시도 (dataSource.getConnection())
3. 이 시점에는 아직 트랜잭션의 readOnly 속성이 ThreadLocal에 완전히 바인딩되기 전이거나 기본값 상태.
4. 결과: readOnly = true로 설정했음에도 무조건 Master DB 커넥션을 가져옴.

LazyConnectionDataSourceProxy 적용 시
- 트랜잭션이 시작될 때는 진짜 DB 커넥션 대신 가짜(프록시) 커넥션만 넘김.
- 실제 비즈니스 로직에서 첫 쿼리(예: repository.findById(...))가 실행되는 순간 getConnection()을 호출.
- 이 시점엔 TransactionSynchronizationManager에 readOnly=true가 세팅되어, RoutingDataSource.java가 정상적으로 Slave DB를 선택함.
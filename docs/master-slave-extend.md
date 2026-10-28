## DB Master-Slave 다중 확장(Multi-Slave) 가이드

- 읽기 트래픽 증가에 대응하기 위해 Slave(읽기 전용 DB)를 2대 이상으로 확장하는 가이드입니다.
- Docker Compose 환경에서 복수 Slave 컨테이너를 구동하고, Spring Boot 애플리케이션 레벨에서 라운드 로빈(Round-Robin) 로드 밸런싱을 통해 읽기 트래픽을 분산합니다.
- `docs/master-slave.md`의 단일 Slave 구조를 기반으로 확장됩니다.

### 1. 인프라 설정: `docker-compose.yml`에 복수 Slave 추가

- 기존 `user-slave-db`(또는 `user-slave-db-1`) 외에 `user-slave-db-2` 서비스를 추가합니다.
- 각 Slave 컨테이너는 동일한 `entrypoint-slave.sh`를 진입점으로 사용하며, 서로 다른 포트와 데이터 볼륨을 할당받습니다.

```yaml
services:
  # Master DB
  user-db:
    image: postgres:18-alpine
    container_name: mopl-user-db
    ports:
      - "5432:5432"
    command: >
      postgres
      -c wal_level=replica
      -c max_wal_senders=10
      -c max_replication_slots=10
      -c hot_standby=on
    env_file:
      - mopl-user/.env.dev
    volumes:
      - user-db-data:/var/lib/postgresql
      - ./mopl-user/docker/postgres/init-master.sh:/docker-entrypoint-initdb.d/init-master.sh:ro
    restart: always
    healthcheck:
      test: ["CMD-SHELL", "pg_isready -d $$POSTGRES_DB -U $$POSTGRES_USER"]
      interval: 5s
      timeout: 5s
      retries: 5

  # Slave DB 1
  user-slave-db-1:
    image: postgres:18-alpine
    container_name: mopl-user-slave-db-1
    ports:
      - "5431:5432"
    env_file:
      - mopl-user/.env.dev
    environment:
      - MASTER_HOST=user-db
    volumes:
      - user-slave-1-db-data:/var/lib/postgresql
      - ./mopl-user/docker/postgres/entrypoint-slave.sh:/entrypoint-slave.sh:ro
    entrypoint: ["/bin/sh", "/entrypoint-slave.sh"]
    depends_on:
      user-db:
        condition: service_healthy
    restart: always

  # Slave DB 2 (신규 추가)
  user-slave-db-2:
    image: postgres:18-alpine
    container_name: mopl-user-slave-db-2
    ports:
      - "5430:5432"
    env_file:
      - mopl-user/.env.dev
    environment:
      - MASTER_HOST=user-db
    volumes:
      - user-slave-2-db-data:/var/lib/postgresql
      - ./mopl-user/docker/postgres/entrypoint-slave.sh:/entrypoint-slave.sh:ro
    entrypoint: ["/bin/sh", "/entrypoint-slave.sh"]
    depends_on:
      user-db:
        condition: service_healthy
    restart: always

volumes:
  user-db-data:
  user-slave-1-db-data:
  user-slave-2-db-data:
```

### 2. 애플리케이션 설정: `application.yml` 단일 Slave를 복수 Slave로 분리

- 단일 `slave` 설정을 `slave-1`, `slave-2`로 분리하여 각 인스턴스별 접속 정보 및 풀 크기를 개별 설정합니다.

```yaml
spring:
  datasource:
    user:
      master:
        driver-class-name: org.postgresql.Driver
        url: jdbc:postgresql://${DB_HOST:localhost}:${DB_PORT:5432}/${POSTGRES_DB}
        username: ${POSTGRES_USER}
        password: ${POSTGRES_PASSWORD}
        hikari:
          pool-name: HikariPool-User-Master
          maximum-pool-size: 5
          minimum-idle: 5
      slave-1:
        driver-class-name: org.postgresql.Driver
        url: jdbc:postgresql://${DB_SLAVE_1_HOST:${DB_HOST:localhost}}:${DB_SLAVE_1_PORT:5431}/${POSTGRES_DB}
        username: ${POSTGRES_USER}
        password: ${POSTGRES_PASSWORD}
        hikari:
          pool-name: HikariPool-User-Slave-1
          maximum-pool-size: 5
          minimum-idle: 5
      slave-2:
        driver-class-name: org.postgresql.Driver
        url: jdbc:postgresql://${DB_SLAVE_2_HOST:${DB_HOST:localhost}}:${DB_SLAVE_2_PORT:5430}/${POSTGRES_DB}
        username: ${POSTGRES_USER}
        password: ${POSTGRES_PASSWORD}
        hikari:
          pool-name: HikariPool-User-Slave-2
          maximum-pool-size: 5
          minimum-idle: 5
```

### 3. DataSource 타입 구분 Enum 확장 (`DataSourceType.java`)

- 라우팅 대상 키를 구분하기 위해 Enum 상수를 추가합니다.

```java
package org.codeit.sb06.team03.mopl.config.datasource;

public enum DataSourceType {
    MASTER,
    SLAVE_1,
    SLAVE_2
}
```

### 4. `UserDbConfig.java` 수정: 복수 DataSource 빈 등록 및 매핑

- 각 Slave에 대한 DataSource 빈을 개별 선언하고, `routingDataSource` 빈의 `targetDataSources` 맵에 등록합니다.

```java
package org.codeit.sb06.team03.mopl.config;

// ... 기존 import 생략 ...

@Configuration
@EnableTransactionManagement
@EnableQuerydslRepositories(
        basePackages = {"org.codeit.sb06.team03.mopl"},
        entityManagerFactoryRef = "userEntityManagerFactory",
        transactionManagerRef = "userTransactionManager"
)
public class UserDbConfig {

    @Value("${spring.jpa.user.hibernate.ddl-auto:update}")
    private String ddlAuto;

    // Master DataSource 설정
    @Bean
    @ConfigurationProperties("spring.datasource.user.master")
    public DataSourceProperties masterDataSourceProperties() {
        return new DataSourceProperties();
    }

    @Bean(name = "masterDataSource")
    @ConfigurationProperties(prefix = "spring.datasource.user.master.hikari")
    public DataSource masterDataSource() {
        return masterDataSourceProperties()
                .initializeDataSourceBuilder()
                .type(HikariDataSource.class)
                .build();
    }

    // Slave 1 DataSource 설정
    @Bean
    @ConfigurationProperties("spring.datasource.user.slave-1")
    public DataSourceProperties slave1DataSourceProperties() {
        return new DataSourceProperties();
    }

    @Bean(name = "slave1DataSource")
    @ConfigurationProperties(prefix = "spring.datasource.user.slave-1.hikari")
    public DataSource slave1DataSource() {
        return slave1DataSourceProperties()
                .initializeDataSourceBuilder()
                .type(HikariDataSource.class)
                .build();
    }

    // Slave 2 DataSource 설정
    @Bean
    @ConfigurationProperties("spring.datasource.user.slave-2")
    public DataSourceProperties slave2DataSourceProperties() {
        return new DataSourceProperties();
    }

    @Bean(name = "slave2DataSource")
    @ConfigurationProperties(prefix = "spring.datasource.user.slave-2.hikari")
    public DataSource slave2DataSource() {
        return slave2DataSourceProperties()
                .initializeDataSourceBuilder()
                .type(HikariDataSource.class)
                .build();
    }

    // RoutingDataSource 빈 등록 (Target 매핑)
    @Bean(name = "routingDataSource")
    public DataSource routingDataSource(
            @Qualifier("masterDataSource") DataSource masterDataSource,
            @Qualifier("slave1DataSource") DataSource slave1DataSource,
            @Qualifier("slave2DataSource") DataSource slave2DataSource) {
        RoutingDataSource routingDataSource = new RoutingDataSource();

        Map<Object, Object> targetDataSources = new HashMap<>();
        targetDataSources.put(DataSourceType.MASTER, masterDataSource);
        targetDataSources.put(DataSourceType.SLAVE_1, slave1DataSource);
        targetDataSources.put(DataSourceType.SLAVE_2, slave2DataSource);

        routingDataSource.setTargetDataSources(targetDataSources);
        routingDataSource.setDefaultTargetDataSource(masterDataSource);
        routingDataSource.afterPropertiesSet();

        return routingDataSource;
    }

    // LazyConnectionDataSourceProxy로 실제 쿼리 실행 시점까지 커넥션 획득 지연
    @Primary
    @Bean(name = "userDataSource")
    public DataSource userDataSource(@Qualifier("routingDataSource") DataSource routingDataSource) {
        return new LazyConnectionDataSourceProxy(routingDataSource);
    }

    // entityManagerFactory 및 transactionManager 설정은 기존과 동일
    // ...
}
```

### 5. `RoutingDataSource.java` 수정: Round-Robin 분산 알고리즘

- `readOnly = true` 트랜잭션 요청을 수신했을 때 `AtomicInteger`를 활용하여 등록된 Slave 목록을 순환(Round-Robin)하며 고르게 분산합니다.

```java
package org.codeit.sb06.team03.mopl.config.datasource;

import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
public class RoutingDataSource extends AbstractRoutingDataSource {

    private final AtomicInteger counter = new AtomicInteger(0);
    private final List<DataSourceType> slaveKeys = List.of(
            DataSourceType.SLAVE_1,
            DataSourceType.SLAVE_2
    );

    @Override
    protected Object determineCurrentLookupKey() {
        boolean isReadOnly = TransactionSynchronizationManager.isCurrentTransactionReadOnly();

        if (!isReadOnly) {
            log.debug("Current transaction is write operation -> Routing to MASTER");
            return DataSourceType.MASTER;
        }

        // Round-Robin 분산 (음수 방지를 위해 Math.abs 적용)
        int index = Math.abs(counter.getAndIncrement() % slaveKeys.size());
        DataSourceType selectedSlave = slaveKeys.get(index);

        log.debug("Current transaction is readOnly -> Routing to {}", selectedSlave);
        return selectedSlave;
    }
}
```

### 6. 점검 및 주의사항

1. **커넥션 풀(HikariCP) 자원 배분**
   - 단일 Slave 대비 Slave 인스턴스가 늘어남에 따라 애플리케이션에서 유지하는 전체 커넥션 수(Master + Slave 1 + Slave 2)가 증가합니다.
   - DB 서버의 리소스와 컨테이너 메모리 한도에 맞춰 `maximum-pool-size`를 적절히 조절해야 합니다.

2. **Master의 복제 부하 관리 (`max_wal_senders`)**
   - Master 컨테이너의 PostgreSQL 설정 중 `max_wal_senders` 및 `max_replication_slots` 값은 연결될 Slave 개수 이상이어야 합니다.
   - 현재 설정(`max_wal_senders=10`)은 Slave 2대 이상을 수용하기에 충분합니다.

3. **Replication Lag (복제 지연)**
   - 비동기 WAL 스트리밍 복제 특성상 Master에 쓰기 완료 후 Slave에 반영되기까지 극히 짧은 지연이 발생할 수 있습니다.
   - CUD 직후 반드시 최신 상태를 즉시 조회해야 하는 비즈니스 로직의 경우, `@Transactional(readOnly = false)`로 두어 Master에서 직접 조회하도록 처리하거나 트랜잭션 전파 옵션을 점검해야 합니다.

4. **로컬 Docker 환경 vs 클라우드(AWS RDS / Aurora) 환경**
   - **로컬 Docker 환경**: DNS 레벨의 로드 밸런서가 없으므로 애플리케이션 코드(`RoutingDataSource`)에서 직접 각 Slave의 IP/Port를 알고 라운드 로빈 로직을 수행해야 합니다.
   - **클라우드 환경 (Aurora / RDS Read Replica)**: 클라우드 프로바이더가 단일 Reader Endpoint(도메인)를 제공하며 자체 DNS/L4 수준에서 여러 읽기 복제본으로 로드 밸런싱을 수행하므로, 애플리케이션 코드는 슬레이브를 여러 개 관리할 필요 없이 단일 `reader` 엔드포인트만 바라보도록 구성하는 것이 일반적입니다.

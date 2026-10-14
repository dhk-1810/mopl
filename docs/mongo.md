#### 1. 사용처, 도입 이유

- **사용처**
  - `mopl-dm` 모듈의 1:1 다이렉트 메시지([`dm_messages`](file:///Users/dhk/codeit/sb06-mopl-team03/mopl-dm/src/main/java/org/codeit/sb06/team03/mopl/entity/DMMessage.java) 컬렉션) 영속화 저장소

- **도입 이유 (학습 목적)**
  1. **높은 쓰기 처리량 및 대용량 확장성 (High Write Throughput & Scalability)**
     - 실시간 채팅 메시지는 읽기뿐 아니라 쓰기(Insert) 빈도가 매우 높으며, 시간이 지남에 따라 데이터가 선형적·기하급수적으로 누적되는 특성을 가집니다.
     - RDB의 엄격한 트랜잭션 오버헤드(Undo/Redo 로그, 테이블 락, 인덱스 갱신 부하) 대신 빠른 쓰기 성능을 보장하며, 데이터 증가 시 샤딩(Sharding)을 통한 수평 확장에 유리합니다.
  2. **조회 패턴의 단순성 (No JOINs)**
     - 채팅 메시지는 복잡한 다자간 JOIN 연산이 필요하지 않으며, 특정 채팅방(`dmChatRoomId`)을 기준으로 시간순 정렬 및 커서 페이징 조회만 주로 발생합니다.
     - 따라서 관계형 모델보다 독립적인 도큐먼트(Document) 단위 조회가 훨씬 효율적입니다.
  3. **스키마 유연성 (Schema Flexibility)**
     - 단순 텍스트 메시지 외에도 향후 이미지/파일 첨부, 답장/인용(Thread/Reply), 이모지 리액션, 링크 프리뷰 메타데이터 등 메시지 포맷이 확장될 때 DDL 스키마 변경 마이그레이션 부담 없이 유연하게 수용할 수 있습니다.

---

#### 2. 인덱스

- **설정된 복합 인덱스 (Compound Index)**
  ```java
  @CompoundIndexes({
      @CompoundIndex(name = "idx_chatroom_created_id", def = "{'dmChatRoomId': 1, 'deletedAt': 1, 'createdAt': -1, '_id': -1}")
  })
  @Document(collection = "dm_messages")
  public class DMMessage { ... }
  ```

- **인덱스 설계 및 컬럼 선정 근거 (ESR 원칙 적용)**
  1. **Equality (`dmChatRoomId: 1`, `deletedAt: 1`)**
     - 특정 채팅방의 활성(미삭제 `deletedAt: null`) 메시지만 필터링하므로 가장 선두에 배치하여 검색 대상 범위를 최소화합니다.
  2. **Sort (`createdAt: -1`, `_id: -1`)**
     - 최신 메시지 조회 및 무한 스크롤 페이징을 위해 역순 정렬을 인덱스 트리에 일치시켰습니다.
     - 같은 밀리초 단위에 생성된 메시지의 순서 보장(Tie-breaker)을 위해 고유값인 `_id`를 2차 정렬 컬럼으로 포함했습니다.
     - 이를 통해 MongoDB 내부의 **인메모리 정렬(In-Memory Sort) 메모리 초과 에러 방지 및 정렬 비용 0**을 달성합니다.
  3. **Range (`createdAt`)**
     - 커서 기반 페이징(Cursor-based Pagination) 시 이전/이후 시간 범위를 부등호(`$lt`, `$gt`) 조건으로 빠르게 스캔합니다.

- **운영 설정**
  - [`mopl-dm/src/main/resources/application.yml`](file:///Users/dhk/codeit/sb06-mopl-team03/mopl-dm/src/main/resources/application.yml)에 `spring.data.mongodb.auto-index-creation: true`를 구성하여 애플리케이션 구동 시 인덱스가 자동으로 안전하게 생성되도록 설정했습니다.

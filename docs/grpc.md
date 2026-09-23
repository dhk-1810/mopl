### 현재 모듈별 포트 및 gRPC 사용 현황

- mopl-user                  | 8081       | 서버 (User gRPC 제공)                         | 8181
- mopl-content               | 8082       | 서버 (Content gRPC 제공) + 클라이언트 (User 호출) | 8182
- mopl-watching-session      | 8083       | 클라이언트 (User 호출)                          | -
- mopl-playlist              | 8084       | 클라이언트 (User, Content 호출)                 | -
- mopl-dm                    | 8085       | 클라이언트 (User 호출)                          | -
- mopl-notification          | 8086       | 클라이언트 (User 호출)                          | -
- mopl-image                 | 8087       | -                                           | -
- mopl-batch                 | 8088       | -                                           | -
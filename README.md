# 3팀
- 팀 협업 문서 ([GitHub Wiki 링크](https://github.com/Codeit-Sprint-SB-06-03-Advanced-Project/sb06-mopl-team03/wiki))

# 팀원 구성
- 김승빈 ([GitHub 링크](https://github.com/mainlib990))
- 김도현 ([GitHub 링크](https://github.com/dhk-1810))
- 이진우 ([GitHub 링크](https://github.com/jionu102))
- 이치호 ([GitHub 링크](https://github.com/chiho0270))

---

# 프로젝트 소개
- 영화, 드라마, 스포츠 등 다양한 콘텐츠를 큐레이팅하고 공유하며, 실시간 같이 보기 기능까지 제공하는 소셜 서비스.
- 프로젝트 기간: 2026.1.22 ~ 2026.2.27

---

# 기술 스택
| 구분 | 기술 스택 |
| :--- | :--- |
| **언어 / 기본** | **Java SE 21** |
| **백엔드** | **Spring Boot 3** |
| **데이터** | **PostgreSQL 17, JPA, Querydsl** |
| **보안** | **Spring Security** |
| **실시간 / 알림** | **Web Socket, SSE** |
| **인프라** | **AWS (ECS, ECR, S3, RDS)** |
| **데브옵스** | **Docker, GitHub Actions** |
| **버전 관리** | **Git, GitHub** |

---

# 팀원별 구현 기능 상세

| 이름 | 주요 역할 및 담당 업무 |
| :--- | :--- |
| **김승빈 (팀장)** | - 이벤트 스토밍 제안 및 주도<br>- DDD 및 헥사고날 아키텍처 설계·제안<br>- SAGA(Orchestration), CQRS, Event Sourcing 설계 및 적용 |
| **김도현** | - 사용자 비밀번호 변경, 플레이리스트 도메인 구현<br>- SSE API 구현 및 AWS 인프라/CI/CD 워크플로우 구성 |
| **이진우** | - 계정 권한 변경 및 잠금 상태 수정<br>- 사용자 인증 및 실시간 채팅 기능 구현 |
| **이치호** | - 비밀번호 초기화 및 이메일 전송 기능 구현<br>- 개인 메시지(DM) 기능 구현 |

---

# 파일 구조

```text
.
├── build.gradle
├── gradle
│   └── wrapper
│       ├── gradle-wrapper.jar
│       └── gradle-wrapper.properties
├── gradlew
├── gradlew.bat
├── README.md
├── settings.gradle
└── src
    ├── main
    │   ├── java
    │   │   └── org
    │   │       └── codeit
    │   │           └── sb06
    │   │               └── team03
    │   │                   └── mopl
    │   │                       ├── account
    │   │                       │   ├── AccountProperties.java
    │   │                       │   ├── application
    │   │                       │   │   ├── AccountCommandService.java
    │   │                       │   │   ├── AccountQueryService.java
    │   │                       │   │   ├── in
    │   │                       │   │   │   ├── AssignRoleCommand.java
    │   │                       │   │   │   ├── AssignRoleUseCase.java
    │   │                       │   │   │   ├── GetAccountUseCase.java
    │   │                       │   │   │   ├── package-info.java
    │   │                       │   │   │   ├── RegisterAccountCommand.java
    │   │                       │   │   │   ├── RegisterAccountUseCase.java
    │   │                       │   │   │   ├── ResetPasswordCommand.java
    │   │                       │   │   │   ├── ResetPasswordUseCase.java
    │   │                       │   │   │   ├── UpdateLockStatusCommand.java
    │   │                       │   │   │   ├── UpdateLockStatusUseCase.java
    │   │                       │   │   │   ├── UpdatePasswordCommand.java
    │   │                       │   │   │   └── UpdatePasswordUseCase.java
    │   │                       │   │   ├── out
    │   │                       │   │   │   ├── CreateFollowPort.java
    │   │                       │   │   │   ├── CreateProfilePort.java
    │   │                       │   │   │   ├── DeletePasswordResetPort.java
    │   │                       │   │   │   ├── LoadAccountPort.java
    │   │                       │   │   │   ├── LoadPasswordResetPort.java
    │   │                       │   │   │   ├── package-info.java
    │   │                       │   │   │   └── SaveAccountPort.java
    │   │                       │   │   └── package-info.java
    │   │                       │   ├── domain
    │   │                       │   │   ├── Account.java
    │   │                       │   │   ├── AccountService.java
    │   │                       │   │   ├── entity
    │   │                       │   │   │   ├── package-info.java
    │   │                       │   │   │   └── PasswordReset.java
    │   │                       │   │   ├── event
    │   │                       │   │   │   ├── AccountEvent.java
    │   │                       │   │   │   └── package-info.java
    │   │                       │   │   ├── exception
    │   │                       │   │   │   ├── AccountException.java
    │   │                       │   │   │   ├── AccountNotFoundException.java
    │   │                       │   │   │   ├── AccountRegistrationFailedException.java
    │   │                       │   │   │   ├── EmailAddressAlreadyExistsException.java
    │   │                       │   │   │   ├── EmailAddressNotFoundException.java
    │   │                       │   │   │   ├── InvalidAccountIdFormatException.java
    │   │                       │   │   │   ├── InvalidEmailAddressException.java
    │   │                       │   │   │   ├── InvalidIdentifierException.java
    │   │                       │   │   │   ├── InvalidPasswordException.java
    │   │                       │   │   │   ├── InvalidRoleException.java
    │   │                       │   │   │   ├── package-info.java
    │   │                       │   │   │   └── PasswordResetNotFound.java
    │   │                       │   │   ├── package-info.java
    │   │                       │   │   ├── PasswordResetService.java
    │   │                       │   │   ├── policy
    │   │                       │   │   │   ├── BasicTempPasswordResetTimeoutPolicy.java
    │   │                       │   │   │   ├── package-info.java
    │   │                       │   │   │   ├── PasswordEncryptionPolicy.java
    │   │                       │   │   │   ├── PasswordNoEncryptionPolicy.java
    │   │                       │   │   │   ├── TempPasswordGenerationPolicy.java
    │   │                       │   │   │   ├── TempPasswordRandomGenerationPolicy.java
    │   │                       │   │   │   └── TempPasswordResetTimeoutPolicy.java
    │   │                       │   │   └── vo
    │   │                       │   │       ├── EmailAddress.java
    │   │                       │   │       ├── package-info.java
    │   │                       │   │       ├── Password.java
    │   │                       │   │       └── Role.java
    │   │                       │   ├── infra
    │   │                       │   │   ├── in
    │   │                       │   │   │   └── AccountEventListener.java
    │   │                       │   │   ├── out
    │   │                       │   │   │   ├── AccountRepository.java
    │   │                       │   │   │   ├── CreateFollowAdapter.java
    │   │                       │   │   │   ├── CreateProfileAdapter.java
    │   │                       │   │   │   ├── DeletePasswordResetAdapter.java
    │   │                       │   │   │   ├── LoadAccountAdapter.java
    │   │                       │   │   │   ├── LoadPasswordResetAdapter.java
    │   │                       │   │   │   ├── package-info.java
    │   │                       │   │   │   ├── PasswordResetRepository.java
    │   │                       │   │   │   └── SaveAccountAdapter.java
    │   │                       │   │   └── package-info.java
    │   │                       │   └── package-info.java
    │   │                       ├── auth
    │   │                       │   ├── infra
    │   │                       │   │   ├── in
    │   │                       │   │   │   ├── AuthApi.java
    │   │                       │   │   │   ├── AuthController.java
    │   │                       │   │   │   ├── AuthControllerAdvice.java
    │   │                       │   │   │   ├── AuthMapper.java
    │   │                       │   │   │   ├── package-info.java
    │   │                       │   │   │   └── ResetPasswordRequest.java
    │   │                       │   │   └── package-info.java
    │   │                       │   └── package-info.java
    │   │                       ├── bff
    │   │                       │   ├── BasicBffAuthService.java
    │   │                       │   ├── BasicBffDMService.java
    │   │                       │   ├── BasicBffNotificationService.java
    │   │                       │   ├── BasicBffPlaylistService.java
    │   │                       │   ├── BasicBffUserService.java
    │   │                       │   ├── BffAuthService.java
    │   │                       │   ├── BffDMService.java
    │   │                       │   ├── BffNotificationService.java
    │   │                       │   ├── BffPlaylistService.java
    │   │                       │   ├── BffUserService.java
    │   │                       │   └── package-info.java
    │   │                       ├── common
    │   │                       │   ├── config
    │   │                       │   │   ├── AppConfig.java
    │   │                       │   │   ├── AsyncConfig.java
    │   │                       │   │   ├── JpaConfig.java
    │   │                       │   │   ├── package-info.java
    │   │                       │   │   ├── PasswordEncoderConfig.java
    │   │                       │   │   ├── QuerydslConfig.java
    │   │                       │   │   ├── SecurityConfig.java
    │   │                       │   │   ├── SwaggerConfig.java
    │   │                       │   │   └── WebSocketConfig.java
    │   │                       │   ├── ContentMapper.java
    │   │                       │   ├── ContentResult.java
    │   │                       │   ├── enums
    │   │                       │   │   └── SortDirection.java
    │   │                       │   ├── error
    │   │                       │   │   ├── ErrorResponse.java
    │   │                       │   │   ├── GlobalControllerAdvice.java
    │   │                       │   │   └── package-info.java
    │   │                       │   ├── package-info.java
    │   │                       │   ├── security
    │   │                       │   │   ├── jwt
    │   │                       │   │   │   ├── exception
    │   │                       │   │   │   │   ├── InvalidTokenException.java
    │   │                       │   │   │   │   ├── JwtException.java
    │   │                       │   │   │   │   ├── package-info.java
    │   │                       │   │   │   │   └── TokenGenerationFailedException.java
    │   │                       │   │   │   ├── JwtAuthenticationFilter.java
    │   │                       │   │   │   ├── JwtClaimNames.java
    │   │                       │   │   │   ├── JwtClaims.java
    │   │                       │   │   │   ├── JwtDto.java
    │   │                       │   │   │   ├── JwtLoginSuccessHandler.java
    │   │                       │   │   │   ├── JwtLogoutHandler.java
    │   │                       │   │   │   ├── JwtTokenProvider.java
    │   │                       │   │   │   ├── JwtTokenType.java
    │   │                       │   │   │   ├── package-info.java
    │   │                       │   │   │   ├── RefreshTokenCookieProvider.java
    │   │                       │   │   │   ├── registry
    │   │                       │   │   │   │   ├── JwtRegistry.java
    │   │                       │   │   │   │   ├── package-info.java
    │   │                       │   │   │   │   └── PersistentJwtRegistry.java
    │   │                       │   │   │   ├── TokenPair.java
    │   │                       │   │   │   ├── TokenResult.java
    │   │                       │   │   │   ├── TokenSession.java
    │   │                       │   │   │   └── TokenSessionRepository.java
    │   │                       │   │   ├── LoginFailureHandler.java
    │   │                       │   │   ├── MoplAccessDeniedHandler.java
    │   │                       │   │   ├── MoplAuthenticationEntryPoint.java
    │   │                       │   │   ├── MoplUserDetails.java
    │   │                       │   │   ├── MoplUserDetailsService.java
    │   │                       │   │   ├── package-info.java
    │   │                       │   │   └── SpaCsrfTokenRequestHandler.java
    │   │                       │   ├── SessionDetails.java
    │   │                       │   ├── StompAuthInboundInterceptor.java
    │   │                       │   └── UserSummary.java
    │   │                       ├── content
    │   │                       │   ├── application
    │   │                       │   │   ├── ContentCommandService.java
    │   │                       │   │   ├── in
    │   │                       │   │   │   ├── CreateContentCommand.java
    │   │                       │   │   │   ├── CreateReviewCommand.java
    │   │                       │   │   │   ├── CursorResponseWatchingSessionDto.java
    │   │                       │   │   │   ├── DeleteContentCommand.java
    │   │                       │   │   │   ├── DeleteReviewCommand.java
    │   │                       │   │   │   ├── GetContentUseCase.java
    │   │                       │   │   │   ├── package-info.java
    │   │                       │   │   │   ├── UpdateContentCommand.java
    │   │                       │   │   │   ├── UpdateReviewCommand.java
    │   │                       │   │   │   └── WatchingSessionCursorCommand.java
    │   │                       │   │   ├── out
    │   │                       │   │   │   ├── LoadContentPort.java
    │   │                       │   │   │   ├── package-info.java
    │   │                       │   │   │   └── WatchingSessionCursorQuery.java
    │   │                       │   │   └── package-info.java
    │   │                       │   ├── Content.java
    │   │                       │   ├── domain
    │   │                       │   │   ├── entity
    │   │                       │   │   │   ├── package-info.java
    │   │                       │   │   │   ├── Review.java
    │   │                       │   │   │   ├── ReviewStats.java
    │   │                       │   │   │   └── Tag.java
    │   │                       │   │   ├── event
    │   │                       │   │   │   ├── ContentEvent.java
    │   │                       │   │   │   └── package-info.java
    │   │                       │   │   ├── exception
    │   │                       │   │   │   ├── ContentException.java
    │   │                       │   │   │   ├── ContentNotFoundException.java
    │   │                       │   │   │   ├── InvalidCursorFormatException.java
    │   │                       │   │   │   └── package-info.java
    │   │                       │   │   ├── package-info.java
    │   │                       │   │   ├── policy
    │   │                       │   │   │   └── package-info.java
    │   │                       │   │   └── vo
    │   │                       │   │       ├── package-info.java
    │   │                       │   │       └── Type.java
    │   │                       │   ├── infra
    │   │                       │   │   ├── in
    │   │                       │   │   │   ├── ContentApi.java
    │   │                       │   │   │   ├── ContentController.java
    │   │                       │   │   │   ├── ContentControllerAdvice.java
    │   │                       │   │   │   ├── mock
    │   │                       │   │   │   │   ├── ContentCursorRequest.java
    │   │                       │   │   │   │   ├── ContentCursorResponse.java
    │   │                       │   │   │   │   └── ContentMockController.java
    │   │                       │   │   │   ├── package-info.java
    │   │                       │   │   │   └── WatchingSessionCursorRequest.java
    │   │                       │   │   ├── out
    │   │                       │   │   │   ├── ContentQueryAdapter.java
    │   │                       │   │   │   ├── ContentRepository.java
    │   │                       │   │   │   ├── LoadContentAdapter.java
    │   │                       │   │   │   └── package-info.java
    │   │                       │   │   └── package-info.java
    │   │                       │   └── package-info.java
    │   │                       ├── dm
    │   │                       │   ├── common
    │   │                       │   │   └── infra
    │   │                       │   │       ├── CursorUtils.java
    │   │                       │   │       └── package-info.java
    │   │                       │   ├── conversation
    │   │                       │   │   ├── application
    │   │                       │   │   │   ├── ConversationCommandService.java
    │   │                       │   │   │   ├── ConversationQueryService.java
    │   │                       │   │   │   ├── DMUserQueryService.java
    │   │                       │   │   │   ├── in
    │   │                       │   │   │   │   ├── CreateConversationCommand.java
    │   │                       │   │   │   │   ├── CreateConversationUseCase.java
    │   │                       │   │   │   │   ├── GetConversationUseCase.java
    │   │                       │   │   │   │   ├── GetDMUserUseCase.java
    │   │                       │   │   │   │   ├── LiveMessageJoinCommand.java
    │   │                       │   │   │   │   ├── LiveMessageJoinUseCase.java
    │   │                       │   │   │   │   ├── LiveMessageLeaveCommand.java
    │   │                       │   │   │   │   ├── LiveMessageLeaveUseCase.java
    │   │                       │   │   │   │   ├── MessageReadCommand.java
    │   │                       │   │   │   │   ├── MessageReadUseCase.java
    │   │                       │   │   │   │   └── package-info.java
    │   │                       │   │   │   ├── out
    │   │                       │   │   │   │   ├── LoadConversationPort.java
    │   │                       │   │   │   │   ├── LoadDMUserPort.java
    │   │                       │   │   │   │   ├── LoadLiveMessageStatPort.java
    │   │                       │   │   │   │   ├── package-info.java
    │   │                       │   │   │   │   └── SaveConversationPort.java
    │   │                       │   │   │   └── package-info.java
    │   │                       │   │   ├── domain
    │   │                       │   │   │   ├── Conversation.java
    │   │                       │   │   │   ├── ConversationService.java
    │   │                       │   │   │   ├── entity
    │   │                       │   │   │   │   ├── LiveMessageStat.java
    │   │                       │   │   │   │   └── package-info.java
    │   │                       │   │   │   ├── event
    │   │                       │   │   │   │   ├── ConversationEvent.java
    │   │                       │   │   │   │   └── package-info.java
    │   │                       │   │   │   ├── exception
    │   │                       │   │   │   │   ├── ConversationAlreadyExistsException.java
    │   │                       │   │   │   │   ├── ConversationCannotCreateWithSelfException.java
    │   │                       │   │   │   │   ├── ConversationNotFoundException.java
    │   │                       │   │   │   │   ├── DMException.java
    │   │                       │   │   │   │   ├── LiveMessageNotFoundException.java
    │   │                       │   │   │   │   └── package-info.java
    │   │                       │   │   │   ├── package-info.java
    │   │                       │   │   │   └── vo
    │   │                       │   │   │       ├── DMUser.java
    │   │                       │   │   │       └── package-info.java
    │   │                       │   │   ├── infra
    │   │                       │   │   │   ├── in
    │   │                       │   │   │   │   ├── ConversationDto.java
    │   │                       │   │   │   │   ├── CursorRequestConversationDto.java
    │   │                       │   │   │   │   ├── CursorRequestDirectMessageDto.java
    │   │                       │   │   │   │   ├── CursorResponseConversationDto.java
    │   │                       │   │   │   │   ├── CursorResponseDirectMessageDto.java
    │   │                       │   │   │   │   ├── DirectMessageDto.java
    │   │                       │   │   │   │   ├── DMApi.java
    │   │                       │   │   │   │   ├── DMController.java
    │   │                       │   │   │   │   ├── DMControllerAdvice.java
    │   │                       │   │   │   │   ├── DMMapper.java
    │   │                       │   │   │   │   ├── DMUserDto.java
    │   │                       │   │   │   │   ├── package-info.java
    │   │                       │   │   │   │   ├── request
    │   │                       │   │   │   │   │   ├── ConversationCreateRequest.java
    │   │                       │   │   │   │   │   └── package-info.java
    │   │                       │   │   │   │   └── SortOrder.java
    │   │                       │   │   │   ├── out
    │   │                       │   │   │   │   ├── ConversationRepository.java
    │   │                       │   │   │   │   ├── LiveMessageStatRepository.java
    │   │                       │   │   │   │   ├── LoadConversationAdapter.java
    │   │                       │   │   │   │   ├── LoadDMUserAdapter.java
    │   │                       │   │   │   │   ├── LoadLiveMessageStatAdapter.java
    │   │                       │   │   │   │   ├── package-info.java
    │   │                       │   │   │   │   └── SaveConversationAdapter.java
    │   │                       │   │   │   └── package-info.java
    │   │                       │   │   └── package-info.java
    │   │                       │   └── livemessage
    │   │                       │       ├── application
    │   │                       │       │   ├── in
    │   │                       │       │   │   ├── GetDirectMessageUseCase.java
    │   │                       │       │   │   ├── MessagePassUseCase.java
    │   │                       │       │   │   ├── MessageReceiveCommand.java
    │   │                       │       │   │   ├── MessageSendCommand.java
    │   │                       │       │   │   ├── MessageSendUseCase.java
    │   │                       │       │   │   └── package-info.java
    │   │                       │       │   ├── LiveMessageCommandService.java
    │   │                       │       │   ├── LiveMessagePassService.java
    │   │                       │       │   ├── LiveMessageQueryService.java
    │   │                       │       │   ├── out
    │   │                       │       │   │   ├── LoadLiveDMUserPort.java
    │   │                       │       │   │   ├── LoadLiveMessagePort.java
    │   │                       │       │   │   ├── LoadReceiverActivityPort.java
    │   │                       │       │   │   ├── MarkUnreadPort.java
    │   │                       │       │   │   ├── MessagePassPort.java
    │   │                       │       │   │   ├── package-info.java
    │   │                       │       │   │   └── SaveLiveMessagePort.java
    │   │                       │       │   └── package-info.java
    │   │                       │       ├── domain
    │   │                       │       │   ├── event
    │   │                       │       │   │   ├── LiveMessageEvent.java
    │   │                       │       │   │   └── package-info.java
    │   │                       │       │   ├── LiveMessage.java
    │   │                       │       │   ├── LiveMessageService.java
    │   │                       │       │   └── package-info.java
    │   │                       │       ├── infra
    │   │                       │       │   ├── in
    │   │                       │       │   │   ├── DMWebSocketController.java
    │   │                       │       │   │   ├── DMWebSocketEventListener.java
    │   │                       │       │   │   ├── LiveMessageEventListener.java
    │   │                       │       │   │   ├── package-info.java
    │   │                       │       │   │   └── request
    │   │                       │       │   │       ├── MessageSendRequest.java
    │   │                       │       │   │       └── package-info.java
    │   │                       │       │   ├── out
    │   │                       │       │   │   ├── LiveMessageRepository.java
    │   │                       │       │   │   ├── LoadLiveDMUserAdapter.java
    │   │                       │       │   │   ├── LoadLiveMessageAdapter.java
    │   │                       │       │   │   ├── LoadReceiverActivityAdapter.java
    │   │                       │       │   │   ├── MarkUnreadAdapter.java
    │   │                       │       │   │   ├── MessagePassAdapter.java
    │   │                       │       │   │   ├── package-info.java
    │   │                       │       │   │   └── SaveLiveMessageAdapter.java
    │   │                       │       │   └── package-info.java
    │   │                       │       └── package-info.java
    │   │                       ├── email
    │   │                       │   ├── application
    │   │                       │   │   ├── EmailAppService.java
    │   │                       │   │   ├── in
    │   │                       │   │   │   ├── package-info.java
    │   │                       │   │   │   ├── SendEmailCommand.java
    │   │                       │   │   │   └── SendEmailUseCase.java
    │   │                       │   │   ├── out
    │   │                       │   │   │   └── package-info.java
    │   │                       │   │   └── package-info.java
    │   │                       │   ├── domain
    │   │                       │   │   ├── Email.java
    │   │                       │   │   ├── EmailService.java
    │   │                       │   │   ├── event
    │   │                       │   │   │   ├── EmailEvent.java
    │   │                       │   │   │   └── package-info.java
    │   │                       │   │   ├── package-info.java
    │   │                       │   │   ├── PasswordUpdateRequest.java
    │   │                       │   │   ├── policy
    │   │                       │   │   │   ├── EmailSenderPolicy.java
    │   │                       │   │   │   └── package-info.java
    │   │                       │   │   └── vo
    │   │                       │   │       ├── EmailVO.java
    │   │                       │   │       └── package-info.java
    │   │                       │   └── infra
    │   │                       │       ├── in
    │   │                       │       │   ├── EmailEventListener.java
    │   │                       │       │   ├── package-info.java
    │   │                       │       │   └── PasswordResetMapper.java
    │   │                       │       └── out
    │   │                       │           ├── package-info.java
    │   │                       │           └── SpringEmailSenderPolicy.java
    │   │                       ├── follow
    │   │                       │   ├── application
    │   │                       │   │   ├── FollowCommandService.java
    │   │                       │   │   ├── in
    │   │                       │   │   │   ├── CreateFollowCommand.java
    │   │                       │   │   │   ├── CreateFollowUseCase.java
    │   │                       │   │   │   ├── FollowCommand.java
    │   │                       │   │   │   ├── FolloweeQueryService.java
    │   │                       │   │   │   ├── FollowQuery.java
    │   │                       │   │   │   ├── GetFolloweeUseCase.java
    │   │                       │   │   │   ├── GetFollowUseCase.java
    │   │                       │   │   │   ├── ToggleFollowUseCase.java
    │   │                       │   │   │   └── UnfollowCommand.java
    │   │                       │   │   ├── out
    │   │                       │   │   │   ├── LoadFolloweePort.java
    │   │                       │   │   │   └── SaveFolloweePort.java
    │   │                       │   │   └── package-info.java
    │   │                       │   ├── domain
    │   │                       │   │   ├── entity
    │   │                       │   │   │   ├── Follower.java
    │   │                       │   │   │   ├── FollowerId.java
    │   │                       │   │   │   └── package-info.java
    │   │                       │   │   ├── event
    │   │                       │   │   │   ├── FollowEvent.java
    │   │                       │   │   │   └── package-info.java
    │   │                       │   │   ├── exception
    │   │                       │   │   │   ├── FolloweeNotFoundException.java
    │   │                       │   │   │   └── FollowException.java
    │   │                       │   │   ├── Followee.java
    │   │                       │   │   ├── FollowService.java
    │   │                       │   │   └── package-info.java
    │   │                       │   ├── infra
    │   │                       │   │   ├── FollowMapper.java
    │   │                       │   │   ├── in
    │   │                       │   │   │   ├── FollowApi.java
    │   │                       │   │   │   ├── FollowController.java
    │   │                       │   │   │   ├── FollowControllerAdvice.java
    │   │                       │   │   │   ├── FollowDto.java
    │   │                       │   │   │   ├── FollowEventListener.java
    │   │                       │   │   │   ├── FollowRequest.java
    │   │                       │   │   │   └── package-info.java
    │   │                       │   │   ├── out
    │   │                       │   │   │   ├── JpaFollowRepository.java
    │   │                       │   │   │   ├── LoadFolloweeAdapter.java
    │   │                       │   │   │   ├── package-info.java
    │   │                       │   │   │   └── SaveFolloweeAdapter.java
    │   │                       │   │   └── package-info.java
    │   │                       │   └── package-info.java
    │   │                       ├── liveChat
    │   │                       │   ├── application
    │   │                       │   │   ├── in
    │   │                       │   │   │   ├── command
    │   │                       │   │   │   │   ├── package-info.java
    │   │                       │   │   │   │   ├── SendLiveChatMessageCommand.java
    │   │                       │   │   │   │   └── SendPresenceMessageCommand.java
    │   │                       │   │   │   ├── CreateLiveChatUseCase.java
    │   │                       │   │   │   ├── DeleteLiveChatUseCase.java
    │   │                       │   │   │   ├── package-info.java
    │   │                       │   │   │   ├── SendLiveChatMessageUseCase.java
    │   │                       │   │   │   └── SendPresenceMessageUseCase.java
    │   │                       │   │   ├── LiveChatCommandService.java
    │   │                       │   │   ├── out
    │   │                       │   │   │   ├── DeleteLiveChatPort.java
    │   │                       │   │   │   ├── LiveChatContentQueryPort.java
    │   │                       │   │   │   ├── LiveChatWatchingSessionQueryPort.java
    │   │                       │   │   │   ├── LoadLiveChatPort.java
    │   │                       │   │   │   ├── package-info.java
    │   │                       │   │   │   ├── query
    │   │                       │   │   │   │   ├── package-info.java
    │   │                       │   │   │   │   ├── SendLiveChatMessageQuery.java
    │   │                       │   │   │   │   └── SendPresenceMessageQuery.java
    │   │                       │   │   │   ├── SaveLiveChatPort.java
    │   │                       │   │   │   └── SendMessagePort.java
    │   │                       │   │   └── package-info.java
    │   │                       │   ├── domain
    │   │                       │   │   ├── exception
    │   │                       │   │   │   ├── LiveChatDuplicateException.java
    │   │                       │   │   │   ├── LiveChatException.java
    │   │                       │   │   │   ├── LiveChatNotFoundException.java
    │   │                       │   │   │   └── package-info.java
    │   │                       │   │   ├── LiveChat.java
    │   │                       │   │   └── package-info.java
    │   │                       │   ├── infra
    │   │                       │   │   ├── in
    │   │                       │   │   │   ├── LiveChatControllerAdvice.java
    │   │                       │   │   │   ├── package-info.java
    │   │                       │   │   │   └── web
    │   │                       │   │   │       ├── DestinationUtils.java
    │   │                       │   │   │       ├── LiveChatApi.java
    │   │                       │   │   │       ├── LiveChatSendRequest.java
    │   │                       │   │   │       ├── LiveChatWebController.java
    │   │                       │   │   │       ├── LiveChatWebEventListener.java
    │   │                       │   │   │       ├── package-info.java
    │   │                       │   │   │       ├── StompContentInboundInterceptor.java
    │   │                       │   │   │       └── WatchType.java
    │   │                       │   │   ├── out
    │   │                       │   │   │   ├── DeleteLiveChatAdapter.java
    │   │                       │   │   │   ├── LiveChatMessageResponse.java
    │   │                       │   │   │   ├── LiveChatPresenceResponse.java
    │   │                       │   │   │   ├── LiveChatRepository.java
    │   │                       │   │   │   ├── LoadLiveChatAdapter.java
    │   │                       │   │   │   ├── package-info.java
    │   │                       │   │   │   ├── SaveLiveChatAdapter.java
    │   │                       │   │   │   └── SendMessageAdapter.java
    │   │                       │   │   └── package-info.java
    │   │                       │   └── package-info.java
    │   │                       ├── MoplApplication.java
    │   │                       ├── notification
    │   │                       │   ├── application
    │   │                       │   │   ├── in
    │   │                       │   │   │   ├── CreateNotificationUseCase.java
    │   │                       │   │   │   ├── DeleteNotificationUseCase.java
    │   │                       │   │   │   └── GetNotificationsUseCase.java
    │   │                       │   │   ├── NotificationCommandService.java
    │   │                       │   │   ├── NotificationQueryService.java
    │   │                       │   │   └── out
    │   │                       │   │       ├── DeleteNotificationPort.java
    │   │                       │   │       ├── LoadNotificationsPort.java
    │   │                       │   │       ├── LoadSingleNotificationPort.java
    │   │                       │   │       └── SaveNotificationPort.java
    │   │                       │   ├── domain
    │   │                       │   │   ├── exception
    │   │                       │   │   │   ├── NotificationAccessDeniedException.java
    │   │                       │   │   │   ├── NotificationException.java
    │   │                       │   │   │   └── NotificationNotFoundException.java
    │   │                       │   │   ├── Notification.java
    │   │                       │   │   ├── NotificationLevel.java
    │   │                       │   │   └── NotificationService.java
    │   │                       │   └── infra
    │   │                       │       ├── in
    │   │                       │       │   ├── CursorRequestNotificationDto.java
    │   │                       │       │   ├── CursorResponseNotificationDto.java
    │   │                       │       │   ├── NotificationApi.java
    │   │                       │       │   ├── NotificationController.java
    │   │                       │       │   ├── NotificationControllerAdvice.java
    │   │                       │       │   └── NotificationDto.java
    │   │                       │       └── out
    │   │                       │           ├── CursorGetNotificationsCondition.java
    │   │                       │           ├── DeleteNotificationAdapter.java
    │   │                       │           ├── LoadNotificationsAdapter.java
    │   │                       │           ├── LoadSingleNotificationAdapter.java
    │   │                       │           ├── NotificationRepository.java
    │   │                       │           └── SaveNotificationAdapter.java
    │   │                       ├── playlist
    │   │                       │   ├── application
    │   │                       │   │   ├── in
    │   │                       │   │   │   ├── AddContentToCurationUseCase.java
    │   │                       │   │   │   ├── CreatePlaylistCommand.java
    │   │                       │   │   │   ├── CreatePlaylistUseCase.java
    │   │                       │   │   │   ├── DeleteContentFromCurationUseCase.java
    │   │                       │   │   │   ├── DeletePlaylistUseCase.java
    │   │                       │   │   │   ├── GetPlaylistsUseCase.java
    │   │                       │   │   │   ├── GetSinglePlaylistUseCase.java
    │   │                       │   │   │   ├── GetSubscriptionUseCase.java
    │   │                       │   │   │   ├── SubscribePlaylistUseCase.java
    │   │                       │   │   │   ├── UnsubscribePlaylistUseCase.java
    │   │                       │   │   │   ├── UpdatePlaylistCommand.java
    │   │                       │   │   │   └── UpdatePlaylistUseCase.java
    │   │                       │   │   ├── out
    │   │                       │   │   │   ├── LoadCurationPort.java
    │   │                       │   │   │   ├── LoadPlaylistsPort.java
    │   │                       │   │   │   ├── LoadSinglePlaylistPort.java
    │   │                       │   │   │   ├── LoadSubscriptionPort.java
    │   │                       │   │   │   ├── SaveCurationPort.java
    │   │                       │   │   │   ├── SavePlaylistPort.java
    │   │                       │   │   │   └── SaveSubscriptionPort.java
    │   │                       │   │   ├── PlaylistCommandService.java
    │   │                       │   │   └── PlaylistQueryService.java
    │   │                       │   ├── domain
    │   │                       │   │   ├── CurationService.java
    │   │                       │   │   ├── entity
    │   │                       │   │   │   ├── Curation.java
    │   │                       │   │   │   ├── CurationId.java
    │   │                       │   │   │   ├── Playlist.java
    │   │                       │   │   │   ├── Subscription.java
    │   │                       │   │   │   └── SubscriptionId.java
    │   │                       │   │   ├── event
    │   │                       │   │   │   └── PlaylistEvent.java
    │   │                       │   │   ├── exception
    │   │                       │   │   │   ├── ContentAlreadyBeenCuratedException.java
    │   │                       │   │   │   ├── CurationNotFoundException.java
    │   │                       │   │   │   ├── PlaylistAccessDeniedException.java
    │   │                       │   │   │   ├── PlaylistException.java
    │   │                       │   │   │   ├── PlaylistNotFoundException.java
    │   │                       │   │   │   ├── SelfSubscriptionNotAllowedException.java
    │   │                       │   │   │   ├── SubscriptionAlreadyExistsException.java
    │   │                       │   │   │   └── SubscriptionNotFoundException.java
    │   │                       │   │   ├── PlaylistService.java
    │   │                       │   │   └── SubscriptionService.java
    │   │                       │   ├── infra
    │   │                       │   │   ├── in
    │   │                       │   │   │   ├── PlaylistApi.java
    │   │                       │   │   │   ├── PlaylistController.java
    │   │                       │   │   │   ├── PlaylistControllerAdvice.java
    │   │                       │   │   │   ├── PlaylistEventListener.java
    │   │                       │   │   │   ├── PlaylistMapper.java
    │   │                       │   │   │   ├── request
    │   │                       │   │   │   │   ├── CursorRequestPlaylistDto.java
    │   │                       │   │   │   │   ├── PlaylistCreateRequest.java
    │   │                       │   │   │   │   └── PlaylistUpdateRequest.java
    │   │                       │   │   │   └── response
    │   │                       │   │   │       ├── CursorResponsePlaylistDto.java
    │   │                       │   │   │       ├── PlaylistDto.java
    │   │                       │   │   │       └── UserSummaryDto.java
    │   │                       │   │   └── out
    │   │                       │   │       ├── CurationRepository.java
    │   │                       │   │       ├── LoadCurationAdapter.java
    │   │                       │   │       ├── LoadPlaylistsAdapter.java
    │   │                       │   │       ├── LoadSinglePlaylistAdapter.java
    │   │                       │   │       ├── LoadSubscriptionAdapter.java
    │   │                       │   │       ├── PlaylistRepository.java
    │   │                       │   │       ├── SaveCurationAdapter.java
    │   │                       │   │       ├── SavePlaylistAdapter.java
    │   │                       │   │       ├── SaveSubscriptionAdapter.java
    │   │                       │   │       └── SubscriptionRepository.java
    │   │                       │   └── PlaylistReadModel.java
    │   │                       ├── s3
    │   │                       │   ├── S3Config.java
    │   │                       │   ├── S3Properties.java
    │   │                       │   └── S3Service.java
    │   │                       ├── sse
    │   │                       │   ├── application
    │   │                       │   │   ├── SseService.java
    │   │                       │   │   └── SseUseCase.java
    │   │                       │   ├── infra
    │   │                       │   │   ├── in
    │   │                       │   │   │   └── SseController.java
    │   │                       │   │   └── out
    │   │                       │   │       ├── SseEmitterAdapter.java
    │   │                       │   │       ├── SseEmitterPort.java
    │   │                       │   │       ├── SseMessageAdapter.java
    │   │                       │   │       ├── SseMessagePort.java
    │   │                       │   │       └── SseRepository.java
    │   │                       │   └── SseMessage.java
    │   │                       ├── user
    │   │                       │   ├── application
    │   │                       │   │   ├── in
    │   │                       │   │   │   ├── CreateProfileCommand.java
    │   │                       │   │   │   ├── CreateProfileUseCase.java
    │   │                       │   │   │   ├── GetProfileUseCase.java
    │   │                       │   │   │   ├── package-info.java
    │   │                       │   │   │   ├── UpdateProfileCommand.java
    │   │                       │   │   │   └── UpdateProfileUseCase.java
    │   │                       │   │   ├── out
    │   │                       │   │   │   ├── LoadProfileAdapter.java
    │   │                       │   │   │   ├── LoadProfilePort.java
    │   │                       │   │   │   ├── package-info.java
    │   │                       │   │   │   └── SaveProfilePort.java
    │   │                       │   │   ├── package-info.java
    │   │                       │   │   ├── ProfileCommandService.java
    │   │                       │   │   └── ProfileQueryService.java
    │   │                       │   ├── domain
    │   │                       │   │   ├── entity
    │   │                       │   │   │   └── package-info.java
    │   │                       │   │   ├── event
    │   │                       │   │   │   ├── package-info.java
    │   │                       │   │   │   └── UserEvent.java
    │   │                       │   │   ├── exception
    │   │                       │   │   │   ├── ImageRegistrationFailedException.java
    │   │                       │   │   │   ├── package-info.java
    │   │                       │   │   │   ├── ProfileNotFoundException.java
    │   │                       │   │   │   └── UserException.java
    │   │                       │   │   ├── package-info.java
    │   │                       │   │   ├── policy
    │   │                       │   │   │   ├── BasicPresignedUrlTimeoutPolicy.java
    │   │                       │   │   │   ├── BasicProfileImageKeyGenerationPolicy.java
    │   │                       │   │   │   ├── package-info.java
    │   │                       │   │   │   ├── PresignedUrlTimeoutPolicy.java
    │   │                       │   │   │   ├── ProfileImageKeyGenerationPolicy.java
    │   │                       │   │   │   └── ProfileImageRegistrationPolicy.java
    │   │                       │   │   ├── Profile.java
    │   │                       │   │   ├── ProfileService.java
    │   │                       │   │   └── vo
    │   │                       │   │       ├── package-info.java
    │   │                       │   │       └── TimeoutImage.java
    │   │                       │   ├── infra
    │   │                       │   │   ├── in
    │   │                       │   │   │   ├── AccountMapper.java
    │   │                       │   │   │   ├── CursorRequestUserDto.java
    │   │                       │   │   │   ├── CursorResponseUserDto.java
    │   │                       │   │   │   ├── package-info.java
    │   │                       │   │   │   ├── PasswordUpdateRequest.java
    │   │                       │   │   │   ├── UserApi.java
    │   │                       │   │   │   ├── UserController.java
    │   │                       │   │   │   ├── UserControllerAdvice.java
    │   │                       │   │   │   ├── UserCreateRequest.java
    │   │                       │   │   │   ├── UserDto.java
    │   │                       │   │   │   ├── UserLockUpdateRequest.java
    │   │                       │   │   │   ├── UserRoleUpdateRequest.java
    │   │                       │   │   │   └── UserUpdateRequest.java
    │   │                       │   │   ├── out
    │   │                       │   │   │   ├── package-info.java
    │   │                       │   │   │   ├── PresignedUrlUpdateListener.java
    │   │                       │   │   │   ├── ProfileRepository.java
    │   │                       │   │   │   ├── S3ProfileImageRegistrationPolicy.java
    │   │                       │   │   │   └── SaveProfileAdapter.java
    │   │                       │   │   ├── package-info.java
    │   │                       │   │   └── ProfileMapper.java
    │   │                       │   └── package-info.java
    │   │                       └── watchingSession
    │   │                           ├── application
    │   │                           │   ├── in
    │   │                           │   │   ├── CreateWatchingSessionCommand.java
    │   │                           │   │   ├── CreateWatchingSessionUseCase.java
    │   │                           │   │   ├── DeleteWatchingSessionUseCase.java
    │   │                           │   │   ├── GetWatchingSessionUseCase.java
    │   │                           │   │   └── package-info.java
    │   │                           │   ├── out
    │   │                           │   │   ├── DeleteWatchingSessionPort.java
    │   │                           │   │   ├── LoadWatchingSessionPort.java
    │   │                           │   │   ├── package-info.java
    │   │                           │   │   └── SaveWatchingSessionPort.java
    │   │                           │   ├── package-info.java
    │   │                           │   └── WatchingSessionCommandService.java
    │   │                           ├── domain
    │   │                           │   ├── event
    │   │                           │   │   ├── package-info.java
    │   │                           │   │   └── WatchingSessionEvent.java
    │   │                           │   ├── exception
    │   │                           │   │   ├── package-info.java
    │   │                           │   │   ├── WatchingSessionDuplicateException.java
    │   │                           │   │   ├── WatchingSessionException.java
    │   │                           │   │   └── WatchingSessionNotFoundException.java
    │   │                           │   ├── package-info.java
    │   │                           │   └── WatchingSession.java
    │   │                           └── infra
    │   │                               ├── in
    │   │                               │   ├── package-info.java
    │   │                               │   └── WatchingSessionControllerAdvice.java
    │   │                               ├── out
    │   │                               │   ├── DeleteWatchingSessionAdapter.java
    │   │                               │   ├── LoadWatchingSessionAdapter.java
    │   │                               │   ├── package-info.java
    │   │                               │   ├── SaveWatchingSessionAdapter.java
    │   │                               │   ├── WatchingSessionQueryAdapter.java
    │   │                               │   └── WatchingSessionRepository.java
    │   │                               └── package-info.java
    │   └── resources
    │       ├── application-dev.yml
    │       ├── application-prod.yml
    │       ├── application.yml
    │       └── static
    │           ├── assets
    │           │   ├── il_password-2BUOiZum.svg
    │           │   ├── index-C6XQRtMX.css
    │           │   ├── index-ChBRkLr8.js
    │           │   └── PretendardVariable-CJuje-Rk.woff2
    │           ├── favicon.svg
    │           └── index.html
    └── test
        └── java
            └── org
                └── codeit
                    └── sb06
                        └── team03
                            └── mopl
                                └── MoplApplicationTests.java
```

---

# 구현 홈페이지
[링크](http://43.200.74.119:8080/)

---

# 프로젝트 회고록
[링크](https://github.com/Codeit-Sprint-SB-06-03-Advanced-Project/sb06-mopl-team03/wiki/5-3.-스프린트)

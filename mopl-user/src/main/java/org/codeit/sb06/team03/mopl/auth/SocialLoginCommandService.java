package org.codeit.sb06.team03.mopl.auth;

import lombok.RequiredArgsConstructor;
import org.codeit.sb06.team03.mopl.account.application.in.RegisterAccountCommand;
import org.codeit.sb06.team03.mopl.account.application.in.RegisterAccountUseCase;
import org.codeit.sb06.team03.mopl.account.application.out.LoadAccountPort;
import org.codeit.sb06.team03.mopl.account.application.out.SaveAccountPort;
import org.codeit.sb06.team03.mopl.account.domain.Account;
import org.codeit.sb06.team03.mopl.account.domain.vo.EmailAddress;
import org.springframework.stereotype.Service;

import java.util.UUID;

@RequiredArgsConstructor
@Service
public class SocialLoginCommandService implements SocialLoginUseCase {

    private final LoadAccountPort loadAccountPort;
    private final SaveAccountPort saveAccountPort;
    private final RegisterAccountUseCase registerAccountUseCase;
//    private final TokenProviderPort tokenProviderPort; // 필요 시 토큰 발행 포트 호출

    @Override
    public Account loginOrRegister(SocialLoginCommand command) {

        EmailAddress emailAddress = new EmailAddress(command.email());
        return loadAccountPort.findByEmailAddress(emailAddress)
                .orElseGet(() -> {
                    // 2. 계정이 없다면 신규 소셜 회원 가입 진행
                    // 소셜 로그인 회원은 비밀번호 로그인을 하지 않으므로 UUID 등으로 무작위 임시 패스워드를 생성하여 가입시킵니다.
                    String tempPassword = UUID.randomUUID().toString();

                    RegisterAccountCommand registerCommand = new RegisterAccountCommand(
                            command.name(),
                            emailAddress,
                            tempPassword
                    );

                    return registerAccountUseCase.register(registerCommand);
                });
    }
}

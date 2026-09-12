package org.codeit.sb06.team03.mopl.service;

import lombok.RequiredArgsConstructor;
import org.codeit.sb06.team03.mopl.entity.Account;
import org.codeit.sb06.team03.mopl.entity.vo.EmailAddress;
import org.codeit.sb06.team03.mopl.dto.request.ResetPasswordRequest;
import org.codeit.sb06.team03.mopl.dto.response.UserDto;
import org.codeit.sb06.team03.mopl.service.ImageQueryService;
import org.springframework.stereotype.Service;

import java.util.UUID;

@RequiredArgsConstructor
@Service
public class AuthCompositeService {

    private final AccountCommandService accountCommandService;
    private final AccountQueryService accountQueryService;
    private final ImageQueryService imageQueryService;

    public void resetPassword(ResetPasswordRequest request) {
        accountCommandService.resetPassword(new EmailAddress(request.email()));
    }

    public UserDto getUserDto(UUID accountId) {
        Account account = accountQueryService.getById(accountId);
        String presignedUrl = imageQueryService.getPresignedUrl(account.getProfile().getImageKey());
        return UserDto.from(account, account.getProfile(), presignedUrl);
    }
}

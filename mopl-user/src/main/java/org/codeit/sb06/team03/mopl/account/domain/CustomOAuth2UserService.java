package org.codeit.sb06.team03.mopl.account.domain;

import lombok.RequiredArgsConstructor;
import org.codeit.sb06.team03.mopl.account.application.AccountCommandService;
import org.codeit.sb06.team03.mopl.account.application.out.LoadAccountPort;
import org.codeit.sb06.team03.mopl.account.domain.vo.EmailAddress;
import org.codeit.sb06.team03.mopl.account.application.in.RegisterAccountCommand;
import org.codeit.sb06.team03.mopl.common.security.MoplUserDetails;
import org.codeit.sb06.team03.mopl.common.security.OAuth2Attributes;
import org.codeit.sb06.team03.mopl.image.application.in.GetPresignedUrlUseCase;
import org.codeit.sb06.team03.mopl.profile.application.out.LoadProfilePort;
import org.codeit.sb06.team03.mopl.profile.domain.entity.Profile;
import org.codeit.sb06.team03.mopl.profile.infra.in.UserDto;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.util.UUID;

@RequiredArgsConstructor
@Service
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final LoadAccountPort loadAccountPort;
    private final LoadProfilePort loadProfilePort;
    private final AccountCommandService accountCommandService;
    private final GetPresignedUrlUseCase getPresignedUrlUseCase;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User = super.loadUser(userRequest);
        
        String registrationId = userRequest.getClientRegistration().getRegistrationId();
        String userNameAttributeName = userRequest.getClientRegistration()
                .getProviderDetails()
                .getUserInfoEndpoint()
                .getUserNameAttributeName();
        OAuth2Attributes attributes = OAuth2Attributes.of(registrationId, userNameAttributeName, oAuth2User.getAttributes());

        EmailAddress emailAddress = new EmailAddress(attributes.email());
        Account existingUser = loadAccountPort.findByEmailAddress(emailAddress).orElse(null);

        UserDto userDto;
        if (existingUser != null) {
            Profile profile = loadProfilePort.load(existingUser.getId()).orElse(null);
            String profileImageUrl = profile != null ? getPresignedUrlUseCase.getPresignedUrl(profile.getImageKey()) : null;
            userDto = UserDto.from(existingUser, profile, profileImageUrl);
        } else {
            Account account = accountCommandService.register(
                    new RegisterAccountCommand(
                            attributes.name(),
                            emailAddress,
                            UUID.randomUUID().toString()
                    )
            );
            Profile profile = loadProfilePort.load(account.getId()).orElse(null);
            String profileImageUrl = profile != null ? getPresignedUrlUseCase.getPresignedUrl(profile.getImageKey()) : null;
            userDto = UserDto.from(account, profile, profileImageUrl);
        }

        return new MoplUserDetails(userDto, oAuth2User.getAttributes());
    }
}

package org.codeit.sb06.team03.mopl.auth;

import lombok.RequiredArgsConstructor;
import org.codeit.sb06.team03.mopl.account.service.AccountCommandService;
import org.codeit.sb06.team03.mopl.account.service.AccountQueryService;
import org.codeit.sb06.team03.mopl.account.service.RegisterAccountCommand;
import org.codeit.sb06.team03.mopl.account.domain.Account;
import org.codeit.sb06.team03.mopl.account.domain.vo.EmailAddress;
import org.codeit.sb06.team03.mopl.image.service.ImageQueryService;
import org.codeit.sb06.team03.mopl.profile.domain.entity.Profile;
import org.codeit.sb06.team03.mopl.profile.controller.UserDto;
import org.codeit.sb06.team03.mopl.security.MoplUserDetails;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

@RequiredArgsConstructor
@Service
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final AccountQueryService accountQueryService;
    private final AccountCommandService accountCommandService;
    private final ImageQueryService imageQueryService;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User = super.loadUser(userRequest);
        
        String registrationId = userRequest.getClientRegistration().getRegistrationId();
        String userNameAttributeName = userRequest.getClientRegistration()
                .getProviderDetails()
                .getUserInfoEndpoint()
                .getUserNameAttributeName();
        OAuth2Attributes attributes = OAuth2Attributes.of(registrationId, userNameAttributeName, oAuth2User.getAttributes());

        Account existingUser = accountQueryService.getByEmail(attributes.email()).orElse(null);

        UserDto userDto;
        if (existingUser != null) {
            Profile profile = existingUser.getProfile();
            String profileImageUrl = imageQueryService.getPresignedUrl(profile.getImageKey());
            userDto = UserDto.from(existingUser, profile, profileImageUrl);
        } else {
            RegisterAccountCommand registerCommand = new RegisterAccountCommand(
                    attributes.name(),
                    new EmailAddress(attributes.email()),
                    UUID.randomUUID().toString()
            );
            Account account = accountCommandService.register(registerCommand);
            Profile profile = account.getProfile();
            String profileImageUrl = imageQueryService.getPresignedUrl(profile.getImageKey());
            userDto = UserDto.from(account, profile, profileImageUrl);
        }

        return new MoplUserDetails(userDto, oAuth2User.getAttributes());
    }
}

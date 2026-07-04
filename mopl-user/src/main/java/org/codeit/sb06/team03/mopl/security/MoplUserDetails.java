package org.codeit.sb06.team03.mopl.security;

import lombok.Getter;
import org.codeit.sb06.team03.mopl.profile.infra.in.UserDto;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Getter
public class MoplUserDetails implements UserDetails, OAuth2User {

    private final UserDto userDto;
    private final String password;
    private final Map<String, Object> attributes;

    // 일반 로그인용 생성자
    public MoplUserDetails(UserDto userDto, String password) {
        this.userDto = userDto;
        this.password = password;
        this.attributes = Map.of();
    }

    // 소셜 로그인용 생성자
    public MoplUserDetails(UserDto userDto, Map<String, Object> attributes) {
        this.userDto = userDto;
        this.password = ""; // 소셜 로그인은 패스워드 검증 절차가 없으므로 빈 값 설정
        this.attributes = attributes;
    }

    @Override
    public Map<String, Object> getAttributes() {
        return attributes;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + userDto.role()));
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return userDto.email();
    }

    @Override
    public String getName() {
        return userDto.email();
    }

    @Override
    public boolean isAccountNonLocked() {
        return !userDto.locked();
    }

    public UUID getId() {
        return userDto.id();
    }
}

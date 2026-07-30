package org.codeit.sb06.team03.mopl.config;

import lombok.RequiredArgsConstructor;
import org.codeit.sb06.team03.mopl.entity.vo.Role;
import org.codeit.sb06.team03.mopl.service.CustomOAuth2UserService;
import org.codeit.sb06.team03.mopl.security.OAuth2SuccessHandler;
import org.codeit.sb06.team03.mopl.security.LoginFailureHandler;
import org.codeit.sb06.team03.mopl.security.MoplAccessDeniedHandler;
import org.codeit.sb06.team03.mopl.security.MoplAuthenticationEntryPoint;
import org.codeit.sb06.team03.mopl.security.SpaCsrfTokenRequestHandler;
import org.codeit.sb06.team03.mopl.security.UserHeaderAuthenticationFilter;
import org.codeit.sb06.team03.mopl.security.jwt.JwtLoginSuccessHandler;
import org.codeit.sb06.team03.mopl.security.jwt.JwtLogoutHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.hierarchicalroles.RoleHierarchy;
import org.springframework.security.access.hierarchicalroles.RoleHierarchyImpl;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.authentication.logout.HttpStatusReturningLogoutSuccessHandler;

@RequiredArgsConstructor
@Configuration
@EnableWebSecurity
@EnableMethodSecurity(jsr250Enabled = true)
public class SecurityConfig {

    private final CustomOAuth2UserService customOAuth2UserService;
    private final OAuth2SuccessHandler oAuth2SuccessHandler;

    @Bean
    public SecurityFilterChain filterChain(
            HttpSecurity http,
            SpaCsrfTokenRequestHandler spaCsrfTokenRequestHandler,
            JwtLoginSuccessHandler loginSuccessHandler,
            LoginFailureHandler loginFailureHandler,
            UserHeaderAuthenticationFilter userHeaderAuthenticationFilter,
            MoplAuthenticationEntryPoint authenticationEntryPoint,
            MoplAccessDeniedHandler accessDeniedHandler,
            JwtLogoutHandler logoutHandler
    ) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .headers(headers -> headers
                        .frameOptions(HeadersConfigurer.FrameOptionsConfig::sameOrigin))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/v3/api-docs/**", "/swagger-ui/**", "/error").permitAll()
                        .requestMatchers("/", "/index.html", "/favicon.svg", "/assets/**").permitAll()
                        .requestMatchers("/sign-in", "/oauth-redirect", "/oauth2/**", "/login/oauth2/**").permitAll()
                        .requestMatchers("/ws/**").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/auth/sign-in").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/users").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/auth/csrf-token").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/auth/refresh").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/sse").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/reviews").permitAll()
                        .anyRequest().authenticated()
                )
                .formLogin(login -> login
                        .loginProcessingUrl("/api/auth/sign-in")
                        .successHandler(loginSuccessHandler)
                        .failureHandler(loginFailureHandler)
                )
                .oauth2Login(oauth2 -> oauth2
                        .userInfoEndpoint(userInfo -> userInfo
                                        .userService(customOAuth2UserService)
                        )
                        .successHandler(oAuth2SuccessHandler)
                )
                .logout(logout -> logout
                        .logoutUrl("/api/auth/sign-out")
                        .addLogoutHandler(logoutHandler)
                        .logoutSuccessHandler(
                                new HttpStatusReturningLogoutSuccessHandler(HttpStatus.NO_CONTENT)
                        )
                )
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .addFilterBefore(userHeaderAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint(authenticationEntryPoint)
                        .accessDeniedHandler(accessDeniedHandler)
                )
        ;

        return http.build();
    }

    @Bean
    public RoleHierarchy roleHierarchy() {
        return RoleHierarchyImpl.withDefaultRolePrefix()
                .role(Role.ADMIN.name())
                .implies(Role.USER.name())
                .build();
    }
}

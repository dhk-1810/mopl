package org.codeit.sb06.team03.mopl.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;

import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
public class KeycloakUserService {

    private final RestClient restClient;
    private final String serverUrl;
    private final String realm;
    private final String adminUsername;
    private final String adminPassword;

    public KeycloakUserService(
            @Value("${mopl.keycloak.server-url:http://mopl-keycloak:8090}") String serverUrl,
            @Value("${mopl.keycloak.realm:mopl}") String realm,
            @Value("${mopl.keycloak.admin-username:admin}") String adminUsername,
            @Value("${mopl.keycloak.admin-password:admin}") String adminPassword
    ) {
        this.restClient = RestClient.builder().build();
        this.serverUrl = serverUrl;
        this.realm = realm;
        this.adminUsername = adminUsername;
        this.adminPassword = adminPassword;
    }

    private String getAdminToken() {
        try {
            MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
            formData.add("client_id", "admin-cli");
            formData.add("username", adminUsername);
            formData.add("password", adminPassword);
            formData.add("grant_type", "password");

            @SuppressWarnings("unchecked")
            Map<String, Object> response = restClient.post()
                    .uri(serverUrl + "/realms/master/protocol/openid-connect/token")
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .body(formData)
                    .retrieve()
                    .body(Map.class);

            return response != null ? (String) response.get("access_token") : null;
        } catch (Exception e) {
            log.error("Failed to obtain Keycloak admin token from {}: {}", serverUrl, e.getMessage());
            return null;
        }
    }

    public void createUser(UUID userId, String email, String name, String password) {
        try {
            String adminToken = getAdminToken();
            if (adminToken == null) {
                log.error("Cannot create user in Keycloak: Admin token unavailable");
                return;
            }

            // Keycloak 26 User Profile 필수 필드(firstName, lastName) 처리
            String firstName = name != null && !name.isBlank() ? name : email;
            String lastName = name != null && !name.isBlank() ? name : "User";

            Map<String, Object> userPayload = Map.of(
                    "username", email,
                    "email", email,
                    "firstName", firstName,
                    "lastName", lastName,
                    "enabled", true,
                    "emailVerified", true
            );

            var responseEntity = restClient.post()
                    .uri(serverUrl + "/admin/realms/" + realm + "/users")
                    .header("Authorization", "Bearer " + adminToken)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(userPayload)
                    .retrieve()
                    .toBodilessEntity();

            URI location = responseEntity.getHeaders().getLocation();
            String keycloakUserId = null;
            if (location != null) {
                String path = location.getPath();
                keycloakUserId = path.substring(path.lastIndexOf('/') + 1);
            }

            if (keycloakUserId != null) {
                // 비밀번호 설정 (temporary: false)
                Map<String, Object> credentialPayload = Map.of(
                        "type", "password",
                        "value", password,
                        "temporary", false
                );

                restClient.put()
                        .uri(serverUrl + "/admin/realms/" + realm + "/users/" + keycloakUserId + "/reset-password")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(credentialPayload)
                        .retrieve()
                        .toBodilessEntity();

                log.info("Successfully registered user in Keycloak: {} (ID: {})", email, keycloakUserId);
            }
        } catch (Exception e) {
            log.error("Failed to register user {} in Keycloak: {}", email, e.getMessage(), e);
        }
    }

    public void updatePassword(UUID userId, String newPassword) {
        try {
            String adminToken = getAdminToken();
            if (adminToken == null) return;

            Map<String, Object> credentialPayload = Map.of(
                    "type", "password",
                    "value", newPassword,
                    "temporary", false
            );

            restClient.put()
                    .uri(serverUrl + "/admin/realms/" + realm + "/users/" + userId + "/reset-password")
                    .header("Authorization", "Bearer " + adminToken)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(credentialPayload)
                    .retrieve()
                    .toBodilessEntity();

            log.info("Successfully updated password in Keycloak for user {}", userId);
        } catch (Exception e) {
            log.error("Failed to update password in Keycloak for user {}: {}", userId, e.getMessage(), e);
        }
    }
}

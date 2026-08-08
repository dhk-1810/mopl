package org.codeit.sb06.team03.mopl.auth.infra.in;

import jakarta.validation.constraints.NotBlank;

public record ResetPasswordRequest(
        @NotBlank(message = "이메일은 필수 입력 항목입니다.")
        String email
) {
}

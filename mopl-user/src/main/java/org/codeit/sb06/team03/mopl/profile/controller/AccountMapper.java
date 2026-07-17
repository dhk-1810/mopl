package org.codeit.sb06.team03.mopl.profile.controller;

import org.codeit.sb06.team03.mopl.account.service.AssignRoleCommand;
import org.codeit.sb06.team03.mopl.account.service.RegisterAccountCommand;
import org.codeit.sb06.team03.mopl.account.service.UpdateLockStatusCommand;
import org.codeit.sb06.team03.mopl.account.service.UpdatePasswordCommand;
import org.codeit.sb06.team03.mopl.account.domain.vo.EmailAddress;
import org.springframework.stereotype.Component;

@Component
public class AccountMapper {

    public RegisterAccountCommand toCommand(UserCreateRequest request) {
        final String name = request.name();
        final EmailAddress emailAddress = new EmailAddress(request.email());
        final String rawPassword = request.password();
        return new RegisterAccountCommand(name, emailAddress, rawPassword);
    }

    public UpdatePasswordCommand toCommand(PasswordUpdateRequest request){
        final String newPassword = request.newPassword();
        return new UpdatePasswordCommand(newPassword);
    }

    public AssignRoleCommand toCommand(UserRoleUpdateRequest request) {
        final String role = request.role();
        return new AssignRoleCommand(role);
    }

    public UpdateLockStatusCommand toCommand(UserLockUpdateRequest request) {
        final boolean locked = request.locked();
        return new UpdateLockStatusCommand(locked);
    }
}

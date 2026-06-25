package org.codeit.sb06.team03.mopl.account.infra.out;

import org.codeit.sb06.team03.mopl.account.application.out.CreateProfilePort;
import org.codeit.sb06.team03.mopl.profile.application.in.CreateProfileCommand;
import org.codeit.sb06.team03.mopl.profile.application.in.CreateProfileUseCase;
import org.codeit.sb06.team03.mopl.profile.domain.entity.Profile;
import org.springframework.stereotype.Component;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@Component
public class CreateProfileAdapter implements CreateProfilePort {

    private final CreateProfileUseCase createProfileUseCase;

    public CreateProfileAdapter(CreateProfileUseCase createProfileUseCase) {
        this.createProfileUseCase = createProfileUseCase;
    }

    @Override
    public CompletableFuture<Profile> create(UUID accountId, String name) {
        return CompletableFuture.supplyAsync(() -> {
            var command = new CreateProfileCommand(accountId, name);
            return createProfileUseCase.create(command);
        });
    }
}

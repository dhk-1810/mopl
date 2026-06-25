package org.codeit.sb06.team03.mopl.profile.application.in;

import org.codeit.sb06.team03.mopl.profile.domain.entity.Profile;

public interface UpdateProfileUseCase {

    Profile update(UpdateProfileCommand command);
}

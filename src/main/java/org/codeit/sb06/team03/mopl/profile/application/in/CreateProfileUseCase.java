package org.codeit.sb06.team03.mopl.profile.application.in;

import org.codeit.sb06.team03.mopl.profile.domain.Profile;

public interface CreateProfileUseCase {

    Profile create(CreateProfileCommand command);
}

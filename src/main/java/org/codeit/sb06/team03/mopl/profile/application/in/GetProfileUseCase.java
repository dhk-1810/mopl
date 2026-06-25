package org.codeit.sb06.team03.mopl.profile.application.in;

import org.codeit.sb06.team03.mopl.profile.ProfileReadModel;
import org.codeit.sb06.team03.mopl.profile.domain.entity.Profile;

import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * QueryDSL 메서드로 DTO 형태로 한번에 가져올 경우 ProfileNotFoundException 발생
 * -> Profile 객체 자체를 가져오고 애플리케이션 계층에서 가공
 */

public interface GetProfileUseCase {

    Profile load(UUID accountId);

    List<Profile> load(List<UUID> accountIds);

    ProfileReadModel getProfileReadModel(UUID id);

    Map<UUID, ProfileReadModel> getProfileReadModels(List<UUID> ids);

    List<Profile> loadByNameContaining(String name);

}

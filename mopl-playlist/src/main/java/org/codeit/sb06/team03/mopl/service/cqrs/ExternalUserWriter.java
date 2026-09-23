package org.codeit.sb06.team03.mopl.service.cqrs;

import lombok.RequiredArgsConstructor;
import org.codeit.sb06.team03.mopl.client.UserGrpcClient;
import org.codeit.sb06.team03.mopl.entity.cqrs.ExternalUserView;
import org.codeit.sb06.team03.mopl.repository.cqrs.ExternalUserViewRepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Component
public class ExternalUserWriter {

    private final ExternalUserViewRepository externalUserViewRepository;

    @Transactional(value = "playlistTransactionManager", propagation = Propagation.REQUIRES_NEW)
    public ExternalUserView saveFromDto(UserGrpcClient.UserDto userDto) {
        if (userDto == null) {
            return null;
        }
        ExternalUserView view = ExternalUserView.create(userDto.id(), userDto.name(), userDto.profileImageUrl());
        return externalUserViewRepository.save(view);
    }
}

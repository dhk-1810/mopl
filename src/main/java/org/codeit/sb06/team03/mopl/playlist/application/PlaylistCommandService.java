package org.codeit.sb06.team03.mopl.playlist.application;

import lombok.RequiredArgsConstructor;
import org.codeit.sb06.team03.mopl.account.domain.exception.InvalidIdentifierException;
import org.codeit.sb06.team03.mopl.playlist.application.in.*;
import org.codeit.sb06.team03.mopl.playlist.application.out.*;
import org.codeit.sb06.team03.mopl.playlist.domain.CurationService;
import org.codeit.sb06.team03.mopl.playlist.domain.SubscriptionService;
import org.codeit.sb06.team03.mopl.playlist.domain.entity.*;
import org.codeit.sb06.team03.mopl.playlist.domain.PlaylistService;
import org.codeit.sb06.team03.mopl.playlist.domain.event.PlaylistEvent;
import org.codeit.sb06.team03.mopl.playlist.domain.exception.*;
import org.codeit.sb06.team03.mopl.playlist.infra.in.PlaylistDto;
import org.codeit.sb06.team03.mopl.playlist.infra.in.UserSummaryDto;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Collections;
import java.util.UUID;

@RequiredArgsConstructor
@Service
@Transactional(readOnly = true)
public class PlaylistCommandService implements CreatePlaylistUseCase, UpdatePlaylistUseCase, DeletePlaylistUseCase, AddContentToCurationUseCase, DeleteContentFromCurationUseCase,SubscribePlaylistUseCase, UnsubscribePlaylistUseCase {

    private final SavePlaylistPort savePlaylistPort;
    private final SaveSubscriptionPort saveSubscriptionPort;
    private final SaveCurationPort saveCurationPort;
    private final LoadSinglePlaylistPort loadPlaylistPort;
    private final LoadCurationPort loadCurationPort;
    private final LoadSubscriptionPort loadSubscriptionPort;

    private final PlaylistService playlistService;
    private final CurationService curationService;
    private final SubscriptionService subscriptionService;

    private final ApplicationEventPublisher eventPublisher;

    @Override
    @Transactional
    public PlaylistDto create(CreatePlaylistCommand command, UUID ownerId) {

        final String title = command.title();
        final String description = command.description();

        Playlist playlist = playlistService.create(title, description, ownerId);
        savePlaylistPort.save(playlist);

        // TODO
//      UserDto owner = getUserDto(playlist.ownerId);

        eventPublisher.publishEvent(new PlaylistEvent.PlaylistCreatedEvent(ownerId)); // TODO 저장?
        return PlaylistDto.toDto(playlist, null, false/*, Collections.emptyList()*/);
    }

    @Override
    // TODO 소유자 검증
    public PlaylistDto update(String playlistId, UpdatePlaylistCommand command, UUID ownerId) {

        UUID playlistUUID = parseUUID(playlistId);
        final String title = command.title();
        final String description = command.description();

        Playlist playlist = loadPlaylistPort.findById(playlistUUID)
                        .orElseThrow(() -> new PlaylistNotFoundException(playlistUUID));
        playlist = playlistService.update(playlist, title, description);
        savePlaylistPort.save(playlist);

        // TODO
//      UserDto owner = getUserDto(playlist.ownerId);
//      List<ContentDto> contents = getContents(playlistUUID);
        return PlaylistDto.toDto(playlist, null, false/*, contents*/);
    }

    @Override
    // TODO 소유자 검증
    public void delete(String playlistId, UUID ownerId) {
        UUID playlistUUID = parseUUID(playlistId);
        loadPlaylistPort.findById(playlistUUID)
                .orElseThrow(() -> new PlaylistNotFoundException(playlistUUID));
        savePlaylistPort.delete(playlistUUID);

        saveSubscriptionPort.deleteAllByPlaylistId(playlistUUID); // TODO 이벤트,비동기?
        saveCurationPort.deleteAllByPlaylistId(playlistUUID);
    }

    @Override
    // TODO 소유자 검증
    public void addContentToPlaylist(String playlistId, String contentId, UUID ownerId) {

        UUID playlistUUID = parseUUID(playlistId);
        UUID contentUUID = parseUUID(contentId);

        Playlist playlist = loadPlaylistPort.findById(playlistUUID)
                .orElseThrow(() -> new PlaylistNotFoundException(playlistUUID));
        // TODO loadContentPort.existsById()

        if (loadCurationPort.existsById(new CurationId(playlistUUID, contentUUID))) {
            throw new ContentAlreadyBeenCuratedException(playlistUUID, contentUUID);
        }

        Curation curation = curationService.create(playlistUUID, contentUUID);
        saveCurationPort.save(curation);

        playlist.increaseContentCount();
        savePlaylistPort.save(playlist);

        eventPublisher.publishEvent(new PlaylistEvent.CurationAddedEvent(playlistUUID));
    }

    @Override
    // TODO 소유자 검증
    public void deleteContentFromPlaylist(String playlistId, String contentId, UUID ownerId) {

        UUID playlistUUID = parseUUID(playlistId);
        UUID contentUUID = parseUUID(contentId);

        loadPlaylistPort.findById(playlistUUID)
                .orElseThrow(() -> new PlaylistNotFoundException(playlistUUID));
        Playlist playlist = loadPlaylistPort.findById(playlistUUID)
                        .orElseThrow(() -> new PlaylistNotFoundException(playlistUUID));

        CurationId id = new CurationId(playlistUUID, contentUUID);
        loadCurationPort.findById(id)
                .orElseThrow(() -> new CurationNotFoundException(playlistUUID, contentUUID));

        saveCurationPort.delete(id);

        playlist.decreaseContentCount();
        savePlaylistPort.delete(playlistUUID);
    }

    @Override
    public void subscribe(String playlistId, UUID userId) {

        UUID playlistUUID = parseUUID(playlistId);
        Playlist playlist = loadPlaylistPort.findById(playlistUUID)
                .orElseThrow(() -> new PlaylistNotFoundException(playlistUUID));

        SubscriptionId id = new SubscriptionId(playlistUUID, userId);
        if (loadSubscriptionPort.existsById(id)){
            throw new SubscriptionAlreadyExistsException(playlistUUID, userId);
        }
        if (playlist.getOwnerId().equals(userId)){
            throw new SelfSubscriptionNotAllowedException(playlistUUID, userId);
        }

        Subscription subscription = subscriptionService.create(playlistUUID, userId);
        saveSubscriptionPort.save(subscription);

        playlist.increaseSubscriberCount();
        savePlaylistPort.save(playlist);

        eventPublisher.publishEvent(new PlaylistEvent.SubscriptionCreatedEvent(
                playlistUUID,
                userId,
                playlist.getOwnerId()
        ));
    }

    @Override
    public void unsubscribe(String playlistId, UUID userId) {

        UUID playlistUUID = parseUUID(playlistId);
        Playlist playlist = loadPlaylistPort.findById(playlistUUID)
                .orElseThrow(() -> new PlaylistNotFoundException(playlistUUID));

        SubscriptionId id = new SubscriptionId(playlistUUID, userId);
        if (!loadSubscriptionPort.existsById(id)){
                throw new SubscriptionNotFoundException(playlistUUID, userId);
        }
        saveSubscriptionPort.delete(id);

        playlist.decreaseSubscriberCount();
        savePlaylistPort.save(playlist);
    }

    private UserSummaryDto getUserDto(UUID ownerId){
//        Profile profile = loadProfilePort.findById(ownerId)
//                .orElseThrow(() -> new ProfileNotFoundException());
//        return new UserSummaryDto(
//                ownerId,
//                profile.getName(),
//                profile.getImage().url()
//        );
        return null;
    }

//    private List<ContentDto> getContents(playlistUUID){
//        List<UUID> contentIds = loadCurationPort.findAllByPlaylistId();
//        return loadContentsPort.findAllByIdIn(contentIds)
//                .stream().map(ContentDto::toDto).toList();
//    }

    private UUID parseUUID(String id) {
        try {
            return UUID.fromString(id);
        } catch (IllegalArgumentException | NullPointerException e) {
            throw new InvalidIdentifierException(id);
        }
    }
}

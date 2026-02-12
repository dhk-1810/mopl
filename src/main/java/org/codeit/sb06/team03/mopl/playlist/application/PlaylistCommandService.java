package org.codeit.sb06.team03.mopl.playlist.application;

import lombok.RequiredArgsConstructor;
import org.codeit.sb06.team03.mopl.account.domain.exception.InvalidIdentifierException;
import org.codeit.sb06.team03.mopl.playlist.application.in.*;
import org.codeit.sb06.team03.mopl.playlist.application.out.LoadSinglePlaylistPort;
import org.codeit.sb06.team03.mopl.playlist.application.out.LoadSubscriptionPort;
import org.codeit.sb06.team03.mopl.playlist.application.out.SavePlaylistPort;
import org.codeit.sb06.team03.mopl.playlist.application.out.SaveSubscriptionPort;
import org.codeit.sb06.team03.mopl.playlist.domain.SubscriptionService;
import org.codeit.sb06.team03.mopl.playlist.domain.entity.Playlist;
import org.codeit.sb06.team03.mopl.playlist.domain.PlaylistService;
import org.codeit.sb06.team03.mopl.playlist.domain.entity.Subscription;
import org.codeit.sb06.team03.mopl.playlist.domain.entity.SubscriptionId;
import org.codeit.sb06.team03.mopl.playlist.domain.event.PlaylistEvent;
import org.codeit.sb06.team03.mopl.playlist.domain.exception.PlaylistNotFoundException;
import org.codeit.sb06.team03.mopl.playlist.domain.exception.SelfSubscriptionNotAllowedException;
import org.codeit.sb06.team03.mopl.playlist.domain.exception.SubscriptionAlreadyExistsException;
import org.codeit.sb06.team03.mopl.playlist.domain.exception.SubscriptionNotFoundException;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@RequiredArgsConstructor
@Service
@Transactional(readOnly = true)
public class PlaylistCommandService implements CreatePlaylistUseCase, UpdatePlaylistUseCase, DeletePlaylistUseCase, AddContentToCurationUseCase, DeleteContentFromCurationUseCase,SubscribePlaylistUseCase, UnsubscribePlaylistUseCase {

    private final SavePlaylistPort savePlaylistPort;
    private final SaveSubscriptionPort saveSubscriptionPort;
    private final LoadSinglePlaylistPort loadPlaylistPort;
    private final LoadSubscriptionPort loadSubscriptionPort;
    private final PlaylistService playlistService;
    private final SubscriptionService subscriptionService;
    private final ApplicationEventPublisher eventPublisher;

    @Override
    @Transactional
    public Playlist create(CreatePlaylistCommand command, UUID ownerId) {

        final String title = command.title();
        final String description = command.description();

        Playlist newPlaylist = playlistService.create(title, description, ownerId);
        savePlaylistPort.save(newPlaylist);

        eventPublisher.publishEvent(new PlaylistEvent.PlaylistCreatedEvent(ownerId)); // TODO 저장?
        return newPlaylist;
    }

    @Override
    // TODO 소유자 검증
    public Playlist update(String playlistId, UpdatePlaylistCommand command, UUID ownerId) {

        UUID playlistUUID = parseUUID(playlistId);
        final String title = command.title();
        final String description = command.description();

        Playlist playlist = loadPlaylistPort.findById(playlistUUID)
                        .orElseThrow(() -> new PlaylistNotFoundException(playlistUUID));
        playlist = playlistService.update(playlist, title, description);
        savePlaylistPort.save(playlist);
        return playlist;
    }

    @Override
    // TODO 소유자 검증
    public void delete(String playlistId, UUID ownerId) {
        UUID playlistUUID = parseUUID(playlistId);
        loadPlaylistPort.findById(playlistUUID)
                .orElseThrow(() -> new PlaylistNotFoundException(playlistUUID));
        savePlaylistPort.delete(playlistUUID);
    }

    @Override
    public void add(String playlistId, String contentId, UUID ownerId) {
        UUID playlistUUID = parseUUID(playlistId);
        UUID contentUUID = parseUUID(contentId);

        Playlist playlist = loadPlaylistPort.findById(playlistUUID)
                .orElseThrow(() -> new PlaylistNotFoundException(playlistUUID));
        // loadContentPort.findById()
        if (loadCurationPort.existsByPlaylist_IdAndContent_Id()){
            throw new ContentAlreadyCuratedException();
        }
        Curation curation = curationService.create();
        saveCurationPort.save(curation);
        playlist.increaseContentCount();
        savePlaylistPort.save(playlist);
    }

    @Override
    public void delete(String playlistId, String contentId, UUID ownerId) {
        UUID playlistUUID = parseUUID(playlistId);
        UUID contentUUID = parseUUID(contentId);

        loadPlaylistPort.findById(playlistUUID)
                .orElseThrow(() -> new PlaylistNotFoundException(playlistUUID));
        Playlist playlist = loadPlaylistPort.findById(playlistUUID)
                        .orElseThrow(() -> new PlaylistNotFoundException(playlistUUID));
        loadCurationPort.findById(new CurationId(playlistUUID, contentId))
                .orElseThrow(() -> new CurationNotFoundException(playlistUUID, contentId));
        saveCurationPort.delete(curation);
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

    private UUID parseUUID(String id) {
        try {
            return UUID.fromString(id);
        } catch (IllegalArgumentException | NullPointerException e) {
            throw new InvalidIdentifierException(id);
        }
    }
}

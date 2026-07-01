package org.codeit.sb06.team03.mopl.playlist.application;

import lombok.RequiredArgsConstructor;
import org.codeit.sb06.team03.mopl.playlist.application.in.*;
import org.codeit.sb06.team03.mopl.playlist.application.out.*;
import org.codeit.sb06.team03.mopl.playlist.domain.CurationService;
import org.codeit.sb06.team03.mopl.playlist.domain.SubscriptionService;
import org.codeit.sb06.team03.mopl.playlist.domain.entity.*;
import org.codeit.sb06.team03.mopl.playlist.domain.PlaylistService;
import org.codeit.sb06.team03.mopl.playlist.domain.event.PlaylistEvent;
import org.codeit.sb06.team03.mopl.playlist.domain.exception.*;
import org.codeit.sb06.team03.mopl.profile.application.out.LoadProfilePort;

import org.codeit.sb06.team03.mopl.profile.domain.entity.Profile;
import org.codeit.sb06.team03.mopl.profile.domain.exception.ProfileNotFoundException;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@RequiredArgsConstructor
@Service
@Transactional
public class PlaylistCommandService implements CreatePlaylistUseCase, UpdatePlaylistUseCase, DeletePlaylistUseCase, AddCurationUseCase, DeleteCurationUseCase, SubscribePlaylistUseCase, UnsubscribePlaylistUseCase {

    private final SavePlaylistPort savePlaylistPort;
    private final SaveSubscriptionPort saveSubscriptionPort;
    private final SaveCurationPort saveCurationPort;
    private final LoadSinglePlaylistPort loadPlaylistPort;
    private final LoadCurationPort loadCurationPort;
    private final LoadSubscriptionPort loadSubscriptionPort;
    private final LoadProfilePort loadProfilePort;

    private final PlaylistService playlistService;
    private final CurationService curationService;
    private final SubscriptionService subscriptionService;

    private final ApplicationEventPublisher eventPublisher;

    @Override
    public Playlist create(CreatePlaylistCommand command, UUID ownerId) {

        final String title = command.title();
        final String description = command.description();

        Playlist playlist = playlistService.create(title, description, ownerId);
        savePlaylistPort.save(playlist);
        return playlist;
    }

    @Override
    public Playlist update(UUID playlistId, UpdatePlaylistCommand command, UUID ownerId) {

        final String title = command.title();
        final String description = command.description();

        Playlist playlist = loadPlaylistPort.findById(playlistId)
                        .orElseThrow(() -> new PlaylistNotFoundException(playlistId));
        if (!playlist.getOwnerId().equals(ownerId)) {
            throw new PlaylistAccessDeniedException(playlistId, ownerId);
        }

        playlist = playlistService.update(playlist, title, description);
        savePlaylistPort.save(playlist);

        return playlist;
    }

    @Override
    public void delete(UUID playlistId, UUID ownerId) {
        Playlist playlist = loadPlaylistPort.findById(playlistId)
                .orElseThrow(() -> new PlaylistNotFoundException(playlistId));
        if (!playlist.getOwnerId().equals(ownerId)) {
            throw new PlaylistAccessDeniedException(playlistId, ownerId);
        }
        savePlaylistPort.delete(playlistId);

        eventPublisher.publishEvent(new PlaylistEvent.PlaylistDeletedEvent(playlistId));
    }

    @Override
    public void addContentToPlaylist(UUID playlistId, UUID contentId, String contentTitle, UUID ownerId) {

        Playlist playlist = loadPlaylistPort.findById(playlistId)
                .orElseThrow(() -> new PlaylistNotFoundException(playlistId));
        if (!playlist.getOwnerId().equals(ownerId)) {
            throw new PlaylistAccessDeniedException(playlistId, ownerId);
        }
        if (loadCurationPort.existsById(new CurationId(playlistId, contentId))) {
            throw new ContentAlreadyBeenCuratedException(playlistId, contentId);
        }

        Curation curation = curationService.create(playlistId, contentId, contentTitle);
        saveCurationPort.save(curation);

        playlist.increaseContentCount();
        savePlaylistPort.save(playlist);

        eventPublisher.publishEvent(new PlaylistEvent.CurationAddedEvent(playlist.getId(), playlist.getTitle(), curation.getContentTitle()));
    }

    @Override
    public void deleteContentFromPlaylist(UUID playlistId, UUID contentId, UUID ownerId) {

        Playlist playlist = loadPlaylistPort.findById(playlistId)
                        .orElseThrow(() -> new PlaylistNotFoundException(playlistId));
        if (!playlist.getOwnerId().equals(ownerId)) {
            throw new PlaylistAccessDeniedException(playlistId, ownerId);
        }

        CurationId id = new CurationId(playlistId, contentId);
        loadCurationPort.findById(id)
                .orElseThrow(() -> new CurationNotFoundException(playlistId, contentId));

        saveCurationPort.delete(id);

        playlist.decreaseContentCount();
        savePlaylistPort.delete(playlistId);
    }

    @Override
    public void deleteCurationByContentId(UUID contentId) {
        saveCurationPort.deleteAllByContentId(contentId);
    }

    @Override
    public void subscribe(UUID playlistId, UUID userId) {

        Playlist playlist = loadPlaylistPort.findById(playlistId)
                .orElseThrow(() -> new PlaylistNotFoundException(playlistId));
        Profile subscriber = loadProfilePort.load(userId)
                .orElseThrow(() -> new ProfileNotFoundException(userId));

        SubscriptionId id = new SubscriptionId(playlistId, userId);
        if (loadSubscriptionPort.existsById(id)){
            throw new SubscriptionAlreadyExistsException(playlistId, userId);
        }
        if (playlist.getOwnerId().equals(userId)){
            throw new SelfSubscriptionNotAllowedException(playlistId, userId);
        }

        Subscription subscription = subscriptionService.create(playlistId, userId);
        saveSubscriptionPort.save(subscription);

        playlist.increaseSubscriberCount();
        savePlaylistPort.save(playlist);

        eventPublisher.publishEvent(new PlaylistEvent.SubscriptionCreatedEvent(
                playlistId,
                playlist.getTitle(),
                userId,
                subscriber.getName(),
                playlist.getOwnerId()
        ));
    }

    @Override
    public void unsubscribe(UUID playlistId, UUID userId) {

        Playlist playlist = loadPlaylistPort.findById(playlistId)
                .orElseThrow(() -> new PlaylistNotFoundException(playlistId));

        SubscriptionId id = new SubscriptionId(playlistId, userId);
        if (!loadSubscriptionPort.existsById(id)){
                throw new SubscriptionNotFoundException(playlistId, userId);
        }
        saveSubscriptionPort.delete(id);

        playlist.decreaseSubscriberCount();
        savePlaylistPort.save(playlist);
    }
}

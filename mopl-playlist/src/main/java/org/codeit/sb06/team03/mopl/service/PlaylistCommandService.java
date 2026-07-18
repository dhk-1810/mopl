package org.codeit.sb06.team03.mopl.service;

import lombok.RequiredArgsConstructor;
import org.codeit.sb06.team03.mopl.domain.CurationService;
import org.codeit.sb06.team03.mopl.domain.SubscriptionService;
import org.codeit.sb06.team03.mopl.domain.entity.*;
import org.codeit.sb06.team03.mopl.playlist.domain.entity.*;
import org.codeit.sb06.team03.mopl.domain.entity.cqrs.ExternalUserView;
import org.codeit.sb06.team03.mopl.domain.PlaylistService;
import org.codeit.sb06.team03.mopl.event.PlaylistEvent;
import org.codeit.sb06.team03.mopl.playlist.domain.exception.*;
import org.codeit.sb06.team03.mopl.playlist.config.infra.out.CurationRepository;
import org.codeit.sb06.team03.mopl.playlist.config.infra.out.PlaylistRepository;
import org.codeit.sb06.team03.mopl.playlist.config.infra.out.SubscriptionRepository;
import org.codeit.sb06.team03.mopl.playlist.config.infra.out.cqrs.ExternalUserViewRepository;
import org.codeit.sb06.team03.mopl.profile.exception.ProfileNotFoundException;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@RequiredArgsConstructor
@Service
@Transactional("playlistTransactionManager")
public class PlaylistCommandService {

    private final PlaylistRepository playlistRepository;
    private final SubscriptionRepository subscriptionRepository;
    private final CurationRepository curationRepository;
    private final ExternalUserViewRepository externalUserViewRepository;

    private final PlaylistService playlistService;
    private final CurationService curationService;
    private final SubscriptionService subscriptionService;

    private final ApplicationEventPublisher eventPublisher;

    public Playlist create(String title, String description, UUID ownerId) {
        Playlist playlist = playlistService.create(title, description, ownerId);
        playlistRepository.save(playlist);
        return playlist;
    }

    public Playlist update(UUID playlistId, String title, String description, UUID ownerId) {
        Playlist playlist = playlistRepository.findById(playlistId)
                        .orElseThrow(() -> new PlaylistNotFoundException(playlistId));
        if (!playlist.getOwnerId().equals(ownerId)) {
            throw new PlaylistAccessDeniedException(playlistId, ownerId);
        }

        playlist = playlistService.update(playlist, title, description);
        playlistRepository.save(playlist);

        return playlist;
    }

    public void delete(UUID playlistId, UUID ownerId) {
        Playlist playlist = playlistRepository.findById(playlistId)
                .orElseThrow(() -> new PlaylistNotFoundException(playlistId));
        if (!playlist.getOwnerId().equals(ownerId)) {
            throw new PlaylistAccessDeniedException(playlistId, ownerId);
        }
        playlistRepository.deleteById(playlistId);

        eventPublisher.publishEvent(new PlaylistEvent.PlaylistDeletedEvent(playlistId));
    }

    public void addContentToPlaylist(UUID playlistId, UUID contentId, String contentTitle, UUID ownerId) {
        Playlist playlist = playlistRepository.findById(playlistId)
                .orElseThrow(() -> new PlaylistNotFoundException(playlistId));
        if (!playlist.getOwnerId().equals(ownerId)) {
            throw new PlaylistAccessDeniedException(playlistId, ownerId);
        }
        if (curationRepository.existsById(new CurationId(playlistId, contentId))) {
            throw new ContentAlreadyBeenCuratedException(playlistId, contentId);
        }

        Curation curation = curationService.create(playlistId, contentId, contentTitle);
        curationRepository.save(curation);

        playlist.increaseContentCount();
        playlistRepository.save(playlist);

        eventPublisher.publishEvent(new PlaylistEvent.CurationAddedEvent(playlist.getId(), playlist.getTitle(), curation.getContentTitle()));
    }

    public void deleteContentFromPlaylist(UUID playlistId, UUID contentId, UUID ownerId) {
        Playlist playlist = playlistRepository.findById(playlistId)
                        .orElseThrow(() -> new PlaylistNotFoundException(playlistId));
        if (!playlist.getOwnerId().equals(ownerId)) {
            throw new PlaylistAccessDeniedException(playlistId, ownerId);
        }

        CurationId id = new CurationId(playlistId, contentId);
        curationRepository.findById(id)
                .orElseThrow(() -> new CurationNotFoundException(playlistId, contentId));

        curationRepository.deleteById(id);

        playlist.decreaseContentCount();
        playlistRepository.save(playlist); // Originally did savePlaylistPort.delete(playlistId) which might be a bug in original code, but they decrease count so it should save! Let's save.
    }

    public void deleteCurationByContentId(UUID contentId) {
        curationRepository.deleteAllByContentId(contentId);
    }

    public void subscribe(UUID playlistId, UUID userId) {
        Playlist playlist = playlistRepository.findById(playlistId)
                .orElseThrow(() -> new PlaylistNotFoundException(playlistId));
        ExternalUserView subscriber = externalUserViewRepository.findById(userId)
                .orElseThrow(() -> new ProfileNotFoundException(userId));

        SubscriptionId id = new SubscriptionId(playlistId, userId);
        if (subscriptionRepository.existsById(id)){
            throw new SubscriptionAlreadyExistsException(playlistId, userId);
        }
        if (playlist.getOwnerId().equals(userId)){
            throw new SelfSubscriptionNotAllowedException(playlistId, userId);
        }

        Subscription subscription = subscriptionService.create(playlistId, userId);
        subscriptionRepository.save(subscription);

        playlist.increaseSubscriberCount();
        playlistRepository.save(playlist);

        eventPublisher.publishEvent(new PlaylistEvent.SubscriptionCreatedEvent(
                playlistId,
                playlist.getTitle(),
                userId,
                subscriber.getName(),
                playlist.getOwnerId()
        ));
    }

    public void unsubscribe(UUID playlistId, UUID userId) {
        Playlist playlist = playlistRepository.findById(playlistId)
                .orElseThrow(() -> new PlaylistNotFoundException(playlistId));

        SubscriptionId id = new SubscriptionId(playlistId, userId);
        if (!subscriptionRepository.existsById(id)){
                throw new SubscriptionNotFoundException(playlistId, userId);
        }
        subscriptionRepository.deleteById(id);

        playlist.decreaseSubscriberCount();
        playlistRepository.save(playlist);
    }
}

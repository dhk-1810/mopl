package org.codeit.sb06.team03.mopl.service.application;

import lombok.RequiredArgsConstructor;
import org.codeit.sb06.team03.mopl.entity.*;
import org.codeit.sb06.team03.mopl.entity.cqrs.ExternalUserView;
import org.codeit.sb06.team03.mopl.event.PlaylistEvent;
import org.codeit.sb06.team03.mopl.exception.*;
import org.codeit.sb06.team03.mopl.repository.CurationRepository;
import org.codeit.sb06.team03.mopl.repository.cqrs.ExternalUserViewRepository;
import org.codeit.sb06.team03.mopl.repository.PlaylistRepository;
import org.codeit.sb06.team03.mopl.repository.SubscriptionRepository;
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

    private final ApplicationEventPublisher eventPublisher;

    public Playlist create(String title, String description, UUID ownerId) {
        Playlist playlist = Playlist.create(title, description, ownerId);
        playlistRepository.save(playlist);
        return playlist;
    }

    public Playlist update(UUID playlistId, String title, String description, UUID ownerId) {
        Playlist playlist = playlistRepository.findById(playlistId)
                        .orElseThrow(() -> new PlaylistNotFoundException(playlistId));
        if (!playlist.getOwnerId().equals(ownerId)) {
            throw new PlaylistAccessDeniedException(playlistId, ownerId);
        }

        playlist.update(title, description);
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

        Curation curation = Curation.create(playlistId, contentId, contentTitle);
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
        playlistRepository.save(playlist);
    }

    public void deleteCurationByContentId(UUID contentId) {
        curationRepository.deleteAllByContentId(contentId);
    }

    public void subscribe(UUID playlistId, UUID userId) {
        Playlist playlist = playlistRepository.findById(playlistId)
                .orElseThrow(() -> new PlaylistNotFoundException(playlistId));
        ExternalUserView subscriber = externalUserViewRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));

        SubscriptionId id = new SubscriptionId(playlistId, userId);
        if (subscriptionRepository.existsById(id)){
            throw new SubscriptionAlreadyExistsException(playlistId, userId);
        }
        if (playlist.getOwnerId().equals(userId)){
            throw new SelfSubscriptionNotAllowedException(playlistId, userId);
        }

        Subscription subscription = Subscription.create(playlistId, userId);
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

    public void deleteCascadeByPlaylistId(UUID playlistId) {
        curationRepository.deleteAllByPlaylistId(playlistId);
        subscriptionRepository.deleteAllByPlaylistId(playlistId);
    }
}

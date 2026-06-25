package org.codeit.sb06.team03.mopl.composite;

import lombok.RequiredArgsConstructor;
import org.codeit.sb06.team03.mopl.common.security.MoplUserDetails;
import org.codeit.sb06.team03.mopl.dm.conversation.application.in.*;
import org.codeit.sb06.team03.mopl.dm.conversation.domain.Conversation;
import org.codeit.sb06.team03.mopl.dm.conversation.domain.entity.LiveMessageStat;
import org.codeit.sb06.team03.mopl.dm.conversation.infra.in.*;
import org.codeit.sb06.team03.mopl.dm.conversation.infra.in.request.*;
import org.codeit.sb06.team03.mopl.dm.livemessage.application.in.GetDMUseCase;
import org.codeit.sb06.team03.mopl.dm.livemessage.application.in.MessageSendCommand;
import org.codeit.sb06.team03.mopl.dm.livemessage.application.in.MessageSendUseCase;
import org.codeit.sb06.team03.mopl.dm.livemessage.domain.LiveMessage;
import org.codeit.sb06.team03.mopl.dm.livemessage.infra.in.request.MessageSendRequest;
import org.codeit.sb06.team03.mopl.common.enums.SortDirection;
import org.codeit.sb06.team03.mopl.image.application.in.GetPresignedUrlUseCase;
import org.codeit.sb06.team03.mopl.playlist.infra.in.response.UserSummaryDto;
import org.codeit.sb06.team03.mopl.profile.ProfileReadModel;
import org.codeit.sb06.team03.mopl.profile.application.in.GetProfileUseCase;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

// TODO 트랜잭션
@RequiredArgsConstructor
@Service
public class DMCompositeService {

    private final CreateConversationUseCase createConversationUseCase;
    private final GetConversationUseCase getConversationUseCase;
    private final ReadMessageUseCase messageReadUseCase;
    private final MessageSendUseCase messageSendUseCase;
    private final GetDMUseCase getDirectMessageUseCase;
    private final GetProfileUseCase getProfileUseCase;
    private final GetPresignedUrlUseCase getPresignedUrlUseCase;
    private final DMMapper dmMapper;

    public CursorResponseConversationDto getConversations(CursorRequestConversationDto request) {
        UUID userId = getCurrentUserId();
        int limit = request.limit();

        List<Conversation> items = getConversationUseCase.findAll(
                userId,
                request.cursor(),
                request.idAfter(),
                limit,
                request.sortDirection(),
                request.sortBy()
        );

        boolean hasNext = items.size() > limit;
        List<Conversation> page = hasNext ? items.subList(0, limit) : items;

        String nextCursor = null;
        String nextIdAfter = null;
        if (hasNext && !page.isEmpty()) {
            Conversation last = page.getLast();
            nextCursor = last.getCreatedAt().toString();
            nextIdAfter = last.getId().toString();
        }

        long totalCount = getConversationUseCase.countAll(userId);

        Set<UUID> userIds = page.stream()
                .map(conv -> conv.getOtherParticipant(userId))
                .collect(Collectors.toSet());

        Set<UUID> convIds = page.stream()
                .map(Conversation::getId)
                .collect(Collectors.toSet());
        Map<UUID, LiveMessage> latestMessages = getDirectMessageUseCase
                .findLatestByConversationIds(convIds);

        latestMessages.values().forEach(msg -> {
            userIds.add(msg.getSenderId());
            userIds.add(msg.getReceiverId());
        });

        Map<UUID, ProfileReadModel> profilesMap = getProfileUseCase.getProfileReadModels(new ArrayList<>(userIds));
        Map<UUID, UserSummaryDto> userMap = profilesMap.entrySet().stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        entry -> {
                            ProfileReadModel profile = entry.getValue();
                            String url = getPresignedUrlUseCase.getPresignedUrl(profile.imageKey());
                            return new UserSummaryDto(profile.userId(), profile.name(), url);
                        }
                ));

        List<ConversationDto> data = page.stream()
                .map(conv -> toCursorConversationDto(conv, userId, userMap, latestMessages))
                .toList();

        return new CursorResponseConversationDto(
                data,
                nextCursor,
                nextIdAfter,
                hasNext,
                totalCount,
                request.sortBy(),
                SortDirection.parse(request.sortDirection())
        );
    }

    public ConversationDto createConversation(ConversationCreateRequest request) {
        UUID userId = getCurrentUserId();
        CreateConversationCommand command = dmMapper.toCommand(request.withUserId());
        Conversation conversation = createConversationUseCase.create(userId, command);
        return toConversationDto(conversation, userId, Optional.empty());
    }

    public void readDirectMessage(UUID conversationId, UUID messageId) {
        UUID userId = getCurrentUserId();
        messageReadUseCase.read(new ReadMessageCommand(
                conversationId,
                messageId,
                userId
        ));
    }

    public ConversationDto getConversation(UUID conversationId) {
        UUID userId = getCurrentUserId();
        Conversation conversation = getConversationUseCase.findById(userId, conversationId);
        Optional<LiveMessage> liveMessage = getDirectMessageUseCase.findLatestByConversationId(conversationId);
        return toConversationDto(conversation, userId, liveMessage);
    }

    public CursorResponseDirectMessageDto getDirectMessages(UUID conversationId, CursorRequestDirectMessageDto request) {
        int limit = request.limit();

        List<LiveMessage> items = getDirectMessageUseCase.findAll(
                conversationId,
                request.cursor(),
                request.idAfter(),
                limit,
                request.sortDirection(),
                request.sortBy()
        );

        boolean hasNext = items.size() > limit;
        List<LiveMessage> page = hasNext ? items.subList(0, limit) : items;

        String nextCursor = null;
        String nextIdAfter = null;
        if (hasNext && !page.isEmpty()) {
            LiveMessage last = page.getLast();
            nextCursor = last.getCreatedAt().toString();
            nextIdAfter = last.getId().toString();
        }

        long totalCount = getDirectMessageUseCase.countAll(conversationId);

        Set<UUID> userIds = page.stream()
                .flatMap(msg -> Stream.of(msg.getSenderId(), msg.getReceiverId()))
                .collect(Collectors.toSet());
        Map<UUID, ProfileReadModel> profilesMap = getProfileUseCase.getProfileReadModels(new ArrayList<>(userIds));
        Map<UUID, UserSummaryDto> userMap = profilesMap.entrySet().stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        entry -> {
                            ProfileReadModel profile = entry.getValue();
                            String url = getPresignedUrlUseCase.getPresignedUrl(profile.imageKey());
                            return new UserSummaryDto(profile.userId(), profile.name(), url);
                        }
                ));

        List<DirectMessageDto> data = page.stream()
                .map(msg -> toDirectMessageDto(msg, userMap))
                .toList();

        return new CursorResponseDirectMessageDto(
                data,
                nextCursor,
                nextIdAfter,
                hasNext,
                totalCount,
                request.sortBy(),
                SortDirection.parse(request.sortDirection())
        );
    }

    public ConversationDto getConversationWith(UUID partnerId) {
        UUID userId = getCurrentUserId();
        Conversation conversation = getConversationUseCase.findByWith(userId, partnerId);
        return toConversationDto(conversation, userId, Optional.empty());
    }

    public void sendMessage(UUID conversationId, UUID senderId, MessageSendRequest request) {
        Conversation conversation = getConversationUseCase.findById(senderId, conversationId);
        UUID receiverId = conversation.getOtherParticipant(senderId);
        messageSendUseCase.send(new MessageSendCommand(conversationId, senderId, receiverId, request.content()));
    }

    /**
     * 헬퍼 메서드들
     */

    private ConversationDto toConversationDto(Conversation conversation, UUID userId, Optional<LiveMessage> liveMessage) {
        UUID withUserId = conversation.getOtherParticipant(userId);
        ProfileReadModel withProfile = getProfileUseCase.getProfileReadModel(withUserId);
        String withUrl = getPresignedUrlUseCase.getPresignedUrl(withProfile.imageKey());
        UserSummaryDto with = new UserSummaryDto(withProfile.userId(), withProfile.name(), withUrl);
        DirectMessageDto latestMessage = liveMessage
                .map(msg -> {
                    ProfileReadModel myProfile = getProfileUseCase.getProfileReadModel(userId);
                    String myUrl = getPresignedUrlUseCase.getPresignedUrl(myProfile.imageKey());
                    UserSummaryDto me = new UserSummaryDto(myProfile.userId(), myProfile.name(), myUrl);
                    return toDirectMessageDto(msg, Map.of(withUserId, with, userId, me));
                })
                .orElse(null);

        return new ConversationDto(
                conversation.getId().toString(),
                with,
                latestMessage,
                false
        );
    }

    private ConversationDto toCursorConversationDto(
            Conversation conversation,
            UUID userId,
            Map<UUID, UserSummaryDto> userMap,
            Map<UUID, LiveMessage> latestMessages
    ) {
        UUID withUserId = conversation.getOtherParticipant(userId);
        UserSummaryDto with = userMap.get(withUserId);

        LiveMessageStat stat = conversation.getLiveMessageStats().get(userId);
        boolean hasUnread = stat != null && stat.isHasUnread();

        LiveMessage latestMsg = latestMessages.get(conversation.getId());
        DirectMessageDto latestMessage = latestMsg != null
                ? toDirectMessageDto(latestMsg, userMap)
                : null;

        return new ConversationDto(
                conversation.getId().toString(),
                with,
                latestMessage,
                hasUnread
        );
    }

    private DirectMessageDto toDirectMessageDto(LiveMessage msg, Map<UUID, UserSummaryDto> userMap) {
        UserSummaryDto sender = userMap.get(msg.getSenderId());
        UserSummaryDto receiver = userMap.get(msg.getReceiverId());
        return new DirectMessageDto(
                msg.getId().toString(),
                msg.getConversationId().toString(),
                msg.getCreatedAt().toString(),
                sender,
                receiver,
                msg.getContent()
        );
    }

    private UUID getCurrentUserId() {
        MoplUserDetails user = (MoplUserDetails) SecurityContextHolder.getContext()
                .getAuthentication().getPrincipal();
        return user.getId();
    }
}

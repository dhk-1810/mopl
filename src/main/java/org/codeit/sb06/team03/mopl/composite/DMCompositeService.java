package org.codeit.sb06.team03.mopl.composite;

import lombok.RequiredArgsConstructor;
import org.codeit.sb06.team03.mopl.common.security.MoplUserDetails;
import org.codeit.sb06.team03.mopl.dm.dmChatRoom.application.in.*;
import org.codeit.sb06.team03.mopl.dm.dmChatRoom.domain.DMChatRoom;
import org.codeit.sb06.team03.mopl.dm.dmChatRoom.domain.entity.DMChatRoomStat;
import org.codeit.sb06.team03.mopl.dm.dmChatRoom.infra.in.*;
import org.codeit.sb06.team03.mopl.dm.dmChatRoom.infra.in.request.*;
import org.codeit.sb06.team03.mopl.dm.dmMessage.application.in.GetDMUseCase;
import org.codeit.sb06.team03.mopl.dm.dmMessage.application.in.MessageSendCommand;
import org.codeit.sb06.team03.mopl.dm.dmMessage.application.in.MessageSendUseCase;
import org.codeit.sb06.team03.mopl.dm.dmMessage.domain.DMMessage;
import org.codeit.sb06.team03.mopl.dm.dmMessage.infra.in.request.MessageSendRequest;
import org.codeit.sb06.team03.mopl.common.enums.SortDirection;
import org.codeit.sb06.team03.mopl.image.application.in.GetPresignedUrlUseCase;
import org.codeit.sb06.team03.mopl.playlist.infra.in.response.UserSummary;
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

    private final CreateDMChatRoomUseCase createDMChatRoomUseCase;
    private final GetDMChatRoomUseCase getDMChatRoomUseCase;
    private final ReadMessageUseCase messageReadUseCase;
    private final MessageSendUseCase messageSendUseCase;
    private final GetDMUseCase getDirectMessageUseCase;
    private final GetProfileUseCase getProfileUseCase;
    private final GetPresignedUrlUseCase getPresignedUrlUseCase;
    private final DMMapper dmMapper;

    public CursorResponseDMChatRoomDto getDMChatRooms(CursorRequestDMChatRoomDto request) {
        UUID userId = getCurrentUserId();
        int limit = request.limit();

        List<DMChatRoom> items = getDMChatRoomUseCase.findAll(
                userId,
                request.cursor(),
                request.idAfter(),
                limit,
                request.sortDirection(),
                request.sortBy()
        );

        boolean hasNext = items.size() > limit;
        List<DMChatRoom> page = hasNext ? items.subList(0, limit) : items;

        String nextCursor = null;
        String nextIdAfter = null;
        if (hasNext && !page.isEmpty()) {
            DMChatRoom last = page.getLast();
            nextCursor = last.getCreatedAt().toString();
            nextIdAfter = last.getId().toString();
        }

        long totalCount = getDMChatRoomUseCase.countAll(userId);

        Set<UUID> userIds = page.stream()
                .map(conv -> conv.getOtherParticipant(userId))
                .collect(Collectors.toSet());

        Set<UUID> convIds = page.stream()
                .map(DMChatRoom::getId)
                .collect(Collectors.toSet());
        Map<UUID, DMMessage> latestMessages = getDirectMessageUseCase
                .findLatestByDMChatRoomIds(convIds);

        latestMessages.values().forEach(msg -> {
            userIds.add(msg.getSenderId());
            userIds.add(msg.getReceiverId());
        });

        Map<UUID, ProfileReadModel> profilesMap = getProfileUseCase.getProfileReadModels(new ArrayList<>(userIds));
        Map<UUID, UserSummary> userMap = profilesMap.entrySet().stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        entry -> {
                            ProfileReadModel profile = entry.getValue();
                            String url = getPresignedUrlUseCase.getPresignedUrl(profile.imageKey());
                            return new UserSummary(profile.userId(), profile.name(), url);
                        }
                ));

        List<DMChatRoomDto> data = page.stream()
                .map(conv -> toCursorDMChatRoomDto(conv, userId, userMap, latestMessages))
                .toList();

        return new CursorResponseDMChatRoomDto(
                data,
                nextCursor,
                nextIdAfter,
                hasNext,
                totalCount,
                request.sortBy(),
                SortDirection.parse(request.sortDirection())
        );
    }

    public DMChatRoomDto createDMChatRoom(DMChatRoomCreateRequest request) {
        UUID userId = getCurrentUserId();
        CreateDMChatRoomCommand command = dmMapper.toCommand(request.withUserId());
        DMChatRoom dmChatRoom = createDMChatRoomUseCase.create(userId, command);
        return toDMChatRoomDto(dmChatRoom, userId, Optional.empty());
    }

    public void readDirectMessage(UUID dmChatRoomId, UUID messageId) {
        UUID userId = getCurrentUserId();
        messageReadUseCase.read(new ReadMessageCommand(
                dmChatRoomId,
                messageId,
                userId
        ));
    }

    public DMChatRoomDto getDMChatRoom(UUID dmChatRoomId) {
        UUID userId = getCurrentUserId();
        DMChatRoom dmChatRoom = getDMChatRoomUseCase.findById(userId, dmChatRoomId);
        Optional<DMMessage> dmMessage = getDirectMessageUseCase.findLatestByDMChatRoomId(dmChatRoomId);
        return toDMChatRoomDto(dmChatRoom, userId, dmMessage);
    }

    public CursorResponseDirectMessageDto getDirectMessages(UUID dmChatRoomId, CursorRequestDirectMessageDto request) {
        int limit = request.limit();

        List<DMMessage> items = getDirectMessageUseCase.findAll(
                dmChatRoomId,
                request.cursor(),
                request.idAfter(),
                limit,
                request.sortDirection(),
                request.sortBy()
        );

        boolean hasNext = items.size() > limit;
        List<DMMessage> page = hasNext ? items.subList(0, limit) : items;

        String nextCursor = null;
        String nextIdAfter = null;
        if (hasNext && !page.isEmpty()) {
            DMMessage last = page.getLast();
            nextCursor = last.getCreatedAt().toString();
            nextIdAfter = last.getId().toString();
        }

        long totalCount = getDirectMessageUseCase.countAll(dmChatRoomId);

        Set<UUID> userIds = page.stream()
                .flatMap(msg -> Stream.of(msg.getSenderId(), msg.getReceiverId()))
                .collect(Collectors.toSet());
        Map<UUID, ProfileReadModel> profilesMap = getProfileUseCase.getProfileReadModels(new ArrayList<>(userIds));
        Map<UUID, UserSummary> userMap = profilesMap.entrySet().stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        entry -> {
                            ProfileReadModel profile = entry.getValue();
                            String url = getPresignedUrlUseCase.getPresignedUrl(profile.imageKey());
                            return new UserSummary(profile.userId(), profile.name(), url);
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

    public DMChatRoomDto getDMChatRoomWith(UUID partnerId) {
        UUID userId = getCurrentUserId();
        DMChatRoom dmChatRoom = getDMChatRoomUseCase.findByWith(userId, partnerId);
        return toDMChatRoomDto(dmChatRoom, userId, Optional.empty());
    }

    public void sendMessage(UUID dmChatRoomId, UUID senderId, MessageSendRequest request) {
        DMChatRoom dmChatRoom = getDMChatRoomUseCase.findById(senderId, dmChatRoomId);
        UUID receiverId = dmChatRoom.getOtherParticipant(senderId);
        messageSendUseCase.send(new MessageSendCommand(dmChatRoomId, senderId, receiverId, request.content()));
    }

    /**
     * 헬퍼 메서드들
     */

    private DMChatRoomDto toDMChatRoomDto(DMChatRoom dmChatRoom, UUID userId, Optional<DMMessage> dmMessage) {
        UUID withUserId = dmChatRoom.getOtherParticipant(userId);
        ProfileReadModel withProfile = getProfileUseCase.getProfileReadModel(withUserId);
        String withUrl = getPresignedUrlUseCase.getPresignedUrl(withProfile.imageKey());
        UserSummary with = new UserSummary(withProfile.userId(), withProfile.name(), withUrl);
        DirectMessageDto latestMessage = dmMessage
                .map(msg -> {
                    ProfileReadModel myProfile = getProfileUseCase.getProfileReadModel(userId);
                    String myUrl = getPresignedUrlUseCase.getPresignedUrl(myProfile.imageKey());
                    UserSummary me = new UserSummary(myProfile.userId(), myProfile.name(), myUrl);
                    return toDirectMessageDto(msg, Map.of(withUserId, with, userId, me));
                })
                .orElse(null);

        return new DMChatRoomDto(
                dmChatRoom.getId().toString(),
                with,
                latestMessage,
                false
        );
    }

    private DMChatRoomDto toCursorDMChatRoomDto(
            DMChatRoom dmChatRoom,
            UUID userId,
            Map<UUID, UserSummary> userMap,
            Map<UUID, DMMessage> latestMessages
    ) {
        UUID withUserId = dmChatRoom.getOtherParticipant(userId);
        UserSummary with = userMap.get(withUserId);

        DMChatRoomStat stat = dmChatRoom.getDmChatRoomStats().get(userId);
        boolean hasUnread = stat != null && stat.isHasUnread();

        DMMessage latestMsg = latestMessages.get(dmChatRoom.getId());
        DirectMessageDto latestMessage = latestMsg != null
                ? toDirectMessageDto(latestMsg, userMap)
                : null;

        return new DMChatRoomDto(
                dmChatRoom.getId().toString(),
                with,
                latestMessage,
                hasUnread
        );
    }

    private DirectMessageDto toDirectMessageDto(DMMessage msg, Map<UUID, UserSummary> userMap) {
        UserSummary sender = userMap.get(msg.getSenderId());
        UserSummary receiver = userMap.get(msg.getReceiverId());
        return new DirectMessageDto(
                msg.getId().toString(),
                msg.getDmChatRoomId().toString(),
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

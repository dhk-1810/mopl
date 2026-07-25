package org.codeit.sb06.team03.mopl.service.composite;

import lombok.RequiredArgsConstructor;
import org.codeit.sb06.team03.mopl.dto.response.DirectMessageDto;
import org.codeit.sb06.team03.mopl.dto.response.CursorResponseDMChatRoomDto;
import org.codeit.sb06.team03.mopl.dto.response.DMChatRoomDto;
import org.codeit.sb06.team03.mopl.dto.response.CursorResponseDirectMessageDto;
import org.codeit.sb06.team03.mopl.dto.request.CursorRequestDirectMessageDto;
import org.codeit.sb06.team03.mopl.dto.request.DMChatRoomCreateRequest;
import org.codeit.sb06.team03.mopl.dto.request.CursorRequestDMChatRoomDto;
import org.codeit.sb06.team03.mopl.dto.UserSummary;
import org.codeit.sb06.team03.mopl.entity.DMChatRoom;
import org.codeit.sb06.team03.mopl.entity.DMChatRoomStat;
import org.codeit.sb06.team03.mopl.entity.DMMessage;
import org.codeit.sb06.team03.mopl.service.application.*;
import org.codeit.sb06.team03.mopl.service.cqrs.ExternalUserQueryService;
import org.codeit.sb06.team03.mopl.entity.cqrs.ExternalUserView;
import org.codeit.sb06.team03.mopl.service.application.DMCommandService;
import org.codeit.sb06.team03.mopl.service.application.DMQueryService;
import org.codeit.sb06.team03.mopl.dto.request.MessageSendRequest;
import org.codeit.sb06.team03.mopl.image.service.ExternalImageQueryService;
import org.codeit.sb06.team03.mopl.enums.SortDirection;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@RequiredArgsConstructor
@Service
public class DMCompositeService {

    private final DMChatRoomCommandService dmChatRoomCommandService;
    private final DMChatRoomQueryService dmChatRoomQueryService;
    private final DMCommandService dmCommandService;
    private final DMQueryService dmQueryService;
    private final ExternalUserQueryService externalUserQueryService;
    private final ExternalImageQueryService imageQueryService;

    public DMChatRoomDto createDMChatRoom(DMChatRoomCreateRequest request, UUID userId) {
        DMChatRoom dmChatRoom = dmChatRoomCommandService.create(userId, request.withUserId());
        return toDMChatRoomDto(dmChatRoom, userId, Optional.empty());
    }

    public CursorResponseDMChatRoomDto getDMChatRooms(CursorRequestDMChatRoomDto request, UUID userId) {
        int limit = request.limit();

        List<DMChatRoom> items = dmChatRoomQueryService.findAll(
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

        long totalCount = dmChatRoomQueryService.countAll(userId);

        Set<UUID> userIds = page.stream()
                .map(conv -> conv.getOtherParticipant(userId))
                .collect(Collectors.toSet());

        Set<UUID> convIds = page.stream()
                .map(DMChatRoom::getId)
                .collect(Collectors.toSet());
        Map<UUID, DMMessage> latestMessages = dmQueryService.findLatestByDMChatRoomIds(convIds);

        latestMessages.values().forEach(msg -> {
            userIds.add(msg.getSenderId());
            userIds.add(msg.getReceiverId());
        });

        Map<UUID, ExternalUserView> profilesMap = externalUserQueryService.getProfiles(userIds);
        Map<UUID, UserSummary> userMap = profilesMap.entrySet().stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        entry -> {
                            ExternalUserView profile = entry.getValue();
                            String url = imageQueryService.getPresignedUrl(profile.getProfileImageKey());
                            return new UserSummary(profile.getId(), profile.getName(), url);
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

    public DMChatRoomDto getDMChatRoom(UUID dmChatRoomId, UUID userId) {
        DMChatRoom dmChatRoom = dmChatRoomQueryService.findById(userId, dmChatRoomId);
        Optional<DMMessage> dmMessage = dmQueryService.findLatestByDMChatRoomId(dmChatRoomId);
        return toDMChatRoomDto(dmChatRoom, userId, dmMessage);
    }

    public DMChatRoomDto getDMChatRoomWith(UUID partnerId, UUID userId) {
        DMChatRoom dmChatRoom = dmChatRoomQueryService.findByWith(userId, partnerId);
        return toDMChatRoomDto(dmChatRoom, userId, Optional.empty());
    }

    public CursorResponseDirectMessageDto getDMs(UUID dmChatRoomId, CursorRequestDirectMessageDto request, UUID userId) {
        int limit = request.limit();

        List<DMMessage> items = dmQueryService.findAll(
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

        long totalCount = dmQueryService.countAll(dmChatRoomId);

        Set<UUID> userIds = page.stream()
                .flatMap(msg -> Stream.of(msg.getSenderId(), msg.getReceiverId()))
                .collect(Collectors.toSet());
        Map<UUID, ExternalUserView> profilesMap = externalUserQueryService.getProfiles(userIds);
        Map<UUID, UserSummary> userMap = profilesMap.entrySet().stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        entry -> {
                            ExternalUserView profile = entry.getValue();
                            String url = imageQueryService.getPresignedUrl(profile.getProfileImageKey());
                            return new UserSummary(profile.getId(), profile.getName(), url);
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


    public void sendDM(UUID dmChatRoomId, UUID senderId, MessageSendRequest request) {
        DMChatRoom dmChatRoom = dmChatRoomQueryService.findById(senderId, dmChatRoomId);
        UUID receiverId = dmChatRoom.getOtherParticipant(senderId);
        dmCommandService.send(dmChatRoomId, senderId, receiverId, request.content());
    }

    public void readDM(UUID dmChatRoomId, UUID messageId, UUID userId) {
        dmChatRoomCommandService.read(dmChatRoomId, messageId, userId);
    }

    /**
     * 헬퍼 메서드들
     */

    private DMChatRoomDto toDMChatRoomDto(DMChatRoom dmChatRoom, UUID userId, Optional<DMMessage> dmMessage) {
        UUID withUserId = dmChatRoom.getOtherParticipant(userId);
        ExternalUserView withProfile = externalUserQueryService.getProfile(withUserId);
        String withName = "Unknown User";
        String withImageKey = null;
        if (withProfile != null) {
            withName = withProfile.getName();
            withImageKey = withProfile.getProfileImageKey();
        }
        String withUrl = imageQueryService.getPresignedUrl(withImageKey);
        UserSummary with = new UserSummary(withUserId, withName, withUrl);

        DirectMessageDto latestMessage = dmMessage
                .map(msg -> {
                    ExternalUserView myProfile = externalUserQueryService.getProfile(userId);
                    String myName = "Unknown User";
                    String myImageKey = null;
                    if (myProfile != null) {
                        myName = myProfile.getName();
                        myImageKey = myProfile.getProfileImageKey();
                    }
                    String myUrl = imageQueryService.getPresignedUrl(myImageKey);
                    UserSummary me = new UserSummary(userId, myName, myUrl);

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

}

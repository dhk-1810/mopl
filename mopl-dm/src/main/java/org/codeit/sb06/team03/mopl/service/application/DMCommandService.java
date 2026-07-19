package org.codeit.sb06.team03.mopl.service.application;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.codeit.sb06.team03.mopl.UserSummary;
import org.codeit.sb06.team03.mopl.domain.entity.DMMessage;
import org.codeit.sb06.team03.mopl.event.DMEvent;
import org.codeit.sb06.team03.mopl.repository.DMMessageRepository;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Slf4j
@RequiredArgsConstructor
@Service
@Transactional("dmTransactionManager")
public class DMCommandService {

    private final DMMessageRepository dmMessageRepository;
    private final LoadLiveDMUserAdapter loadDMUserAdapter;
    private final DMChatRoomCommandService dmChatRoomCommandService;
    private final ApplicationEventPublisher eventPublisher;

    public DMMessage send(UUID dmChatRoomId, UUID senderId, UUID receiverId, String content) {
        log.info("DMCommandService.send called: dmChatRoomId={}, senderId={}, receiverId={}, content={}", dmChatRoomId, senderId, receiverId, content);
        UserSummary sender = loadDMUserAdapter.findByUserId(senderId);
        UserSummary receiver = loadDMUserAdapter.findByUserId(receiverId);

        DMMessage message = DMMessage.create(
                dmChatRoomId, senderId,
                receiverId, content,
                sender, receiver
        );
        DMMessage savedMessage = dmMessageRepository.save(message);
        log.info("DMCommandService.send saved message: id={}, content={}", savedMessage.getId(), savedMessage.getContent());

        dmChatRoomCommandService.markAsUnread(dmChatRoomId, receiverId);

        eventPublisher.publishEvent(new DMEvent.MessageSentEvent(
                savedMessage.getId(),
                savedMessage.getDmChatRoomId(),
                savedMessage.getSenderId(),
                savedMessage.getReceiverId(),
                savedMessage.getContent(),
                savedMessage.getCreatedAt(),
                sender,
                receiver
        ));

        return savedMessage;
    }
}
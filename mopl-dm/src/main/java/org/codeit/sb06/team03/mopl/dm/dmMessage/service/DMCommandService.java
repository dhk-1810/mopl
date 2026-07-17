package org.codeit.sb06.team03.mopl.dm.dmMessage.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.codeit.sb06.team03.mopl.dm.dmChatRoom.service.DMChatRoomCommandService;
import org.codeit.sb06.team03.mopl.UserSummary;
import org.codeit.sb06.team03.mopl.dm.dmMessage.service.MessageSendCommand;
import org.codeit.sb06.team03.mopl.dm.dmMessage.domain.DMMessage;
import org.codeit.sb06.team03.mopl.dm.dmMessage.domain.DMService;
import org.codeit.sb06.team03.mopl.dm.dmMessage.domain.event.DMMessageEvent;
import org.codeit.sb06.team03.mopl.dm.dmMessage.repository.DMMessageRepository;
import org.codeit.sb06.team03.mopl.dm.dmMessage.service.LoadLiveDMUserAdapter;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@RequiredArgsConstructor
@Service
@Transactional("dmTransactionManager")
public class DMCommandService {

    private final DMService dmMessageService;
    private final DMMessageRepository dmMessageRepository;
    private final LoadLiveDMUserAdapter loadDMUserAdapter;
    private final DMChatRoomCommandService dmChatRoomCommandService;
    private final ApplicationEventPublisher eventPublisher;

    public DMMessage send(MessageSendCommand command) {
        log.info("DMCommandService.send called: command={}", command);
        UserSummary sender = loadDMUserAdapter.findByUserId(command.senderId());
        UserSummary receiver = loadDMUserAdapter.findByUserId(command.receiverId());

        DMMessage message = dmMessageService.create(
                command.dmChatRoomId(), command.senderId(),
                command.receiverId(), command.content(),
                sender, receiver
        );
        DMMessage savedMessage = dmMessageRepository.save(message);
        log.info("DMCommandService.send saved message: id={}, content={}", savedMessage.getId(), savedMessage.getContent());

        dmChatRoomCommandService.markAsUnread(command.dmChatRoomId(), command.receiverId());

        eventPublisher.publishEvent(new DMMessageEvent.MessageSentEvent(
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
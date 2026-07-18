package org.codeit.sb06.team03.mopl.service;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class PlaylistSubscribedMessage {
    private UUID playlistId;
    private String playlistTitle;
    private UUID subscriberId;
    private String subscriberName;
    private UUID ownerId;
}

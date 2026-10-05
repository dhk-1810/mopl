package org.codeit.sb06.team03.mopl.util;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.springframework.util.StringUtils;

import java.util.regex.Pattern;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class DestinationUtils {

    private static final Pattern liveChatRoomWatchSubDestinationPattern =
            Pattern.compile("^/sub/contents/[0-9a-zA-Z-]+/watch$");
    private static final Pattern liveChatRoomSubDestinationPattern =
            Pattern.compile("^/sub/contents/[0-9a-zA-Z-]+/chat$");
    private static final Pattern liveChatRoomPubDestinationPattern =
            Pattern.compile("^/pub/contents/[0-9a-zA-Z-]+/chat$");

    public static final String liveChatRoomSendResponseDestinationFormat = "/sub/contents/%s/chat";

    public static String extractContentId(String destination) {
        String[] parts = destination.split("/"); // "", "sub", "content", "[contentId]", "watch"
        if (parts.length < 5 || !StringUtils.hasText(parts[3])) {
            return null;
        }
        return parts[3];
    }

    public static boolean matchWatchSubDestination(String destination) {
        return liveChatRoomWatchSubDestinationPattern.matcher(destination).matches();
    }

    public static boolean matchChatSubDestination(String destination) {
        return liveChatRoomSubDestinationPattern.matcher(destination).matches();
    }

    public static boolean matchPubDestination(String destination) {
        return liveChatRoomPubDestinationPattern.matcher(destination).matches();
    }

    public static boolean isContentDestination(String destination) {
        return destination.startsWith("/sub/contents/") || destination.startsWith("/pub/contents/");
    }
}

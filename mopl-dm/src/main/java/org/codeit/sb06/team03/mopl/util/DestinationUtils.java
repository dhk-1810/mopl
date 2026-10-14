package org.codeit.sb06.team03.mopl.util;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.regex.Pattern;

/**
 * STOMP 클라이언트가 요청한 발행(/pub) 및 구독(/sub) Destination이 올바른 형식인지 정규식 패턴으로 검증
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class DestinationUtils {

    private static final Pattern DM_SUB_DESTINATION_PATTERN =
            Pattern.compile("^/sub/conversations/[0-9a-fA-F-]+/direct-messages$");
    private static final Pattern DM_PUB_DESTINATION_PATTERN =
            Pattern.compile("^/pub/conversations/[0-9a-fA-F-]+/direct-messages$");

    public static boolean matchDmSubDestination(String destination) {
        return destination != null && DM_SUB_DESTINATION_PATTERN.matcher(destination).matches();
    }

    public static boolean matchDmPubDestination(String destination) {
        return destination != null && DM_PUB_DESTINATION_PATTERN.matcher(destination).matches();
    }
}

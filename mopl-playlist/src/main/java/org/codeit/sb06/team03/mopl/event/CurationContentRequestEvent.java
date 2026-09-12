package org.codeit.sb06.team03.mopl.event;

import java.util.List;

public record CurationContentRequestEvent (
        List<String> contentIds
) {
}

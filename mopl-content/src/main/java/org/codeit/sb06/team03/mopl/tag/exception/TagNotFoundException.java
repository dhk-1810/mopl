package org.codeit.sb06.team03.mopl.tag.exception;

import java.util.UUID;

public class TagNotFoundException extends TagException {

    private static final String fromIdFormat = "Tag를 찾을 수 없습니다. id: '%s'";
    private static final String fromNameFormat = "Tag를 찾을 수 없습니다. name: '%s'";

    public TagNotFoundException(String message) {
        super(message);
    }

    public static TagNotFoundException fromId(UUID id) {
        return new TagNotFoundException(fromIdFormat.formatted(id));
    }

    public static TagNotFoundException fromName(String name) {
        return new TagNotFoundException(fromNameFormat.formatted(name));
    }
}

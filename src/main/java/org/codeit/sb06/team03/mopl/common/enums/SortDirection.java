package org.codeit.sb06.team03.mopl.common.enums;

public enum SortDirection {
    ASCENDING, DESCENDING;

    public static SortDirection parse(String name) {
        return SortDirection.valueOf(name.toUpperCase());
    }
}
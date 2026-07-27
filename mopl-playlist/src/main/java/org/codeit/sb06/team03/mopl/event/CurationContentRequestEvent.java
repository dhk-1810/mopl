package org.codeit.sb06.team03.mopl.event;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class CurationContentRequestEvent {
    private List<String> contentIds;
}

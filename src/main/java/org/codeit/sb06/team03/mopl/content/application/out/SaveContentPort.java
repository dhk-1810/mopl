package org.codeit.sb06.team03.mopl.content.application.out;


import org.codeit.sb06.team03.mopl.content.Content;

import java.util.UUID;

public interface SaveContentPort {

    void save(Content content);

    void deleteById(UUID id);
}

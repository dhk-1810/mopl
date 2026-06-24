package org.codeit.sb06.team03.mopl.image.application.out;

import org.codeit.sb06.team03.mopl.image.domain.TimeoutImage;

public interface SaveImagePort {

    void save(TimeoutImage timeoutImage);

    void deleteByKey(String key);

}

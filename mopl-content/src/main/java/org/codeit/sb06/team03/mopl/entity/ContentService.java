package org.codeit.sb06.team03.mopl.entity;

import org.codeit.sb06.team03.mopl.enums.ContentType;
import org.springframework.stereotype.Service;

@Service
public class ContentService {

    public Content create(ContentType type, String title, String description, String thumbnailKey){
        return Content.create(type, title, description, thumbnailKey);
    }

    public void update(Content content, String title, String description) {
        content.update(title, description);
    }
}

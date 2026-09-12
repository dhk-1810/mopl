package org.codeit.sb06.team03.mopl.entity;

import org.codeit.sb06.team03.mopl.enums.ContentType;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class ContentService {

    public Content create(UUID id, ContentType type, String title, String description, String thumbnailKey){
        return Content.create(id, type, title, description, thumbnailKey);
    }

    public Content create(ContentType type, String title, String description, String thumbnailKey){
        return Content.create(type, title, description, thumbnailKey);
    }

    public void update(Content content, String title, String description) {
        content.update(title, description);
    }

    public void update(Content content, String title, String description, String thumbnailKey) {
        content.update(title, description, thumbnailKey);
    }
}

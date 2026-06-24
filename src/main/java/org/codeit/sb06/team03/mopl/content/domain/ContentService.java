package org.codeit.sb06.team03.mopl.content.domain;

import org.codeit.sb06.team03.mopl.content.Content;
import org.codeit.sb06.team03.mopl.content.domain.vo.ContentType;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class ContentService {

    public Content create(ContentType type, String title, String description, String thumbnailKey){
        return Content.create(type, title, description, thumbnailKey);
    }

    public Content update(Content content, String title, String description){
        content.update(title, description);
        return content;
    }
}


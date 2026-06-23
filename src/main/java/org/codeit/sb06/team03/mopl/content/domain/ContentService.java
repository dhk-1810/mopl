package org.codeit.sb06.team03.mopl.content.domain;

import lombok.RequiredArgsConstructor;
import org.codeit.sb06.team03.mopl.content.Content;
import org.codeit.sb06.team03.mopl.content.domain.vo.ContentType;
import org.codeit.sb06.team03.mopl.user.domain.policy.ImageRegistrationPolicy;
import org.springframework.stereotype.Service;

import java.util.UUID;

@RequiredArgsConstructor
@Service
public class ContentService {

    private final ImageRegistrationPolicy imageRegistrationPolicy;

    public Content create(ContentType type, String title, String description, UUID thumbnailKey){
        return Content.create(type, title, description, thumbnailKey);
    }

    public Content update(Content content, String title, String description){
        content.update(title, description);
        return content;
    }
}

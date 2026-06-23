package org.codeit.sb06.team03.mopl.user.domain.policy;

import org.codeit.sb06.team03.mopl.user.domain.vo.TimeoutImage;
import org.springframework.web.multipart.MultipartFile;

public interface ImageRegistrationPolicy {

    TimeoutImage register(MultipartFile image);
}

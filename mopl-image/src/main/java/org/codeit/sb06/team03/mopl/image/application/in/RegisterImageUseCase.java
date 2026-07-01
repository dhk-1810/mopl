package org.codeit.sb06.team03.mopl.image.application.in;

import org.springframework.web.multipart.MultipartFile;

public interface RegisterImageUseCase {

    String register(MultipartFile image);

    // TODO Batch 다건?
}

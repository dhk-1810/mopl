package org.codeit.sb06.team03.mopl.image.application.in;

import java.util.List;
import java.util.Map;

public interface GetPresignedUrlUseCase {

    String getPresignedUrl(String key);

    Map<String, String> getPresignedUrls(List<String> keys);

}


package org.codeit.sb06.team03.mopl.batch.client;

import lombok.extern.slf4j.Slf4j;
import org.codeit.sb06.team03.mopl.batch.dto.CollectedContentDto;
import org.codeit.sb06.team03.mopl.content.domain.vo.ContentType;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
public class TmdbClient {

    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${mopl.api.tmdb.key}")
    private String apiKey;

    @Value("${mopl.api.tmdb.url}")
    private String apiUrl;

    public List<CollectedContentDto> fetchMovies() {
        try {
            String url = String.format("%s/movie/popular?api_key=%s&language=ko-KR&page=1", apiUrl, apiKey);
            Map<String, Object> response = restTemplate.getForObject(url, Map.class);
            return parseTmdbResponse(response, ContentType.movie);
        } catch (Exception e) {
            log.error("Failed to fetch movies from TMDB API: {}", e.getMessage(), e);
            return List.of();
        }
    }

    public List<CollectedContentDto> fetchTvSeries() {
        try {
            String url = String.format("%s/tv/popular?api_key=%s&language=ko-KR&page=1", apiUrl, apiKey);
            Map<String, Object> response = restTemplate.getForObject(url, Map.class);
            return parseTmdbResponse(response, ContentType.tvSeries);
        } catch (Exception e) {
            log.error("Failed to fetch TV series from TMDB API: {}", e.getMessage(), e);
            return List.of();
        }
    }

    @SuppressWarnings("unchecked")
    private List<CollectedContentDto> parseTmdbResponse(Map<String, Object> response, ContentType type) {
        List<CollectedContentDto> results = new ArrayList<>();
        if (response == null || !response.containsKey("results")) {
            return results;
        }

        List<Map<String, Object>> items = (List<Map<String, Object>>) response.get("results");
        for (Map<String, Object> item : items) {
            String title = (String) (type == ContentType.movie ? item.get("title") : item.get("name"));
            String overview = (String) item.get("overview");
            String posterPath = (String) item.get("poster_path");

            if (title == null || title.isBlank()) {
                continue;
            }
            if (overview == null || overview.isBlank()) {
                overview = title + " - 상세 설명 준비 중입니다.";
            }
            String thumbnailKey = posterPath != null ? "https://image.tmdb.org/t/p/w500" + posterPath : "default-thumbnail.jpg";

            results.add(new CollectedContentDto(type, title, overview, thumbnailKey));
        }
        return results;
    }
}

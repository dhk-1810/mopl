package org.codeit.sb06.team03.mopl.client;

import lombok.extern.slf4j.Slf4j;
import org.codeit.sb06.team03.mopl.dto.CollectedContentDto;
import org.codeit.sb06.team03.mopl.enums.ContentType;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
public class SportsDbClient {

    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${mopl.api.sportsdb.key}")
    private String apiKey;

    @Value("${mopl.api.sportsdb.url:https://www.thesportsdb.com/api/v1/json}")
    private String apiUrl;

    public List<CollectedContentDto> fetchSports() {
        try {
            // Fetch English Premier League events (League ID: 4328)
            String url = String.format("%s/%s/eventsseason.php?id=4328", apiUrl, apiKey);
            log.info("Fetching SportsDB events from URL: {} (apiKey={})", url.replaceAll(apiKey, "***"), apiKey);
            Map<String, Object> response = restTemplate.getForObject(url, Map.class);
            List<CollectedContentDto> result = parseSportsDbResponse(response);
            log.info("Successfully fetched {} sports events from TheSportsDB.", result.size());
            return result;
        } catch (Exception e) {
            log.error("Failed to fetch sports events from TheSportsDB API: {}", e.getMessage(), e);
            return List.of();
        }
    }

    @SuppressWarnings("unchecked")
    private List<CollectedContentDto> parseSportsDbResponse(Map<String, Object> response) {
        List<CollectedContentDto> results = new ArrayList<>();
        if (response == null || !response.containsKey("events")) {
            log.warn("TheSportsDB API response is null or missing 'events' key. Response: {}", response);
            return results;
        }

        List<Map<String, Object>> events = (List<Map<String, Object>>) response.get("events");
        if (events == null) {
            log.warn("TheSportsDB API 'events' list is null.");
            return results;
        }

        for (Map<String, Object> event : events) {
            String eventName = (String) event.get("strEvent");
            String league = (String) event.get("strLeague");
            String date = (String) event.get("dateEvent");
            String time = (String) event.get("strTime");
            String thumb = (String) event.get("strThumb");

            if (eventName == null || eventName.isBlank()) {
                continue;
            }

            String description = String.format("[%s] %s 경기. 경기 일자: %s %s. 두 팀의 치열한 승부를 실시간으로 감상하세요.",
                    league, eventName, date != null ? date : "", time != null ? time : "");

            String thumbnailKey = thumb != null ? thumb : "default-sports-thumbnail.jpg";

            results.add(new CollectedContentDto(ContentType.sport, eventName, description, thumbnailKey));
        }

        return results;
    }
}

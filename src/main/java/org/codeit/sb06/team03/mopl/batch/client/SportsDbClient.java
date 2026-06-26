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
public class SportsDbClient {

    private final RestTemplate restTemplate = new RestTemplate();

    // Use '3' as the free development key for TheSportsDB API
    @Value("${mopl.api.sportsdb.key:3}")
    private String apiKey;

    @Value("${mopl.api.sportsdb.url:https://www.thesportsdb.com/api/v1/json}")
    private String apiUrl;

    public List<CollectedContentDto> fetchSports() {
        if ("mock".equalsIgnoreCase(apiKey) || apiKey.isBlank()) {
            log.info("Using mock Sports DB content");
            return getMockSports();
        }

        try {
            // Fetch English Premier League events (League ID: 4328)
            String url = String.format("%s/%s/eventsseason.php?id=4328", apiUrl, apiKey);
            Map<String, Object> response = restTemplate.getForObject(url, Map.class);
            return parseSportsDbResponse(response);
        } catch (Exception e) {
            log.error("Failed to fetch sports events from TheSportsDB API, falling back to mock data: {}", e.getMessage());
            return getMockSports();
        }
    }

    @SuppressWarnings("unchecked")
    private List<CollectedContentDto> parseSportsDbResponse(Map<String, Object> response) {
        List<CollectedContentDto> results = new ArrayList<>();
        if (response == null || !response.containsKey("events")) {
            return results;
        }

        List<Map<String, Object>> events = (List<Map<String, Object>>) response.get("events");
        if (events == null) {
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

    private List<CollectedContentDto> getMockSports() {
        return List.of(
                new CollectedContentDto(
                        ContentType.sport,
                        "아스널 vs 첼시 (Arsenal vs Chelsea)",
                        "[English Premier League] 프리미어리그의 대표적인 런던 더비 매치. 우승 도약을 위한 두 팀의 중요한 일전.",
                        "arsenal-chelsea-thumbnail"
                ),
                new CollectedContentDto(
                        ContentType.sport,
                        "맨체스터 유나이티드 vs 리버풀 (Man United vs Liverpool)",
                        "[English Premier League] 역사 깊은 노스웨스트 더비 라이벌 매치. 자존심을 건 치열한 격돌을 감상해 보세요.",
                        "united-liverpool-thumbnail"
                ),
                new CollectedContentDto(
                        ContentType.sport,
                        "레알 마드리드 vs 바르셀로나 (Real Madrid vs Barcelona)",
                        "[La Liga] 전 세계가 주목하는 스페인 최고의 라이벌 전, 엘 클라시코(El Clásico). 최고의 스타들이 격돌합니다.",
                        "el-clasico-thumbnail"
                ),
                new CollectedContentDto(
                        ContentType.sport,
                        "골든스테이트 워리어스 vs LA 레이커스 (Warriors vs Lakers)",
                        "[NBA] 미국 프로농구 최고의 스타 플레이어들이 펼치는 명승부. 서부 컨퍼런스의 최강자를 가립니다.",
                        "nba-warriors-lakers-thumbnail"
                )
        );
    }
}

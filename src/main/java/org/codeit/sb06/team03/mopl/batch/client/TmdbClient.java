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
        if ("mock".equalsIgnoreCase(apiKey) || apiKey.isBlank()) {
            log.info("Using mock TMDB movies");
            return getMockMovies();
        }

        try {
            String url = String.format("%s/movie/popular?api_key=%s&language=ko-KR&page=1", apiUrl, apiKey);
            Map<String, Object> response = restTemplate.getForObject(url, Map.class);
            return parseTmdbResponse(response, ContentType.movie);
        } catch (Exception e) {
            log.error("Failed to fetch movies from TMDB API, falling back to mock data: {}", e.getMessage());
            return getMockMovies();
        }
    }

    public List<CollectedContentDto> fetchTvSeries() {
        if ("mock".equalsIgnoreCase(apiKey) || apiKey.isBlank()) {
            log.info("Using mock TMDB TV series");
            return getMockTvSeries();
        }

        try {
            String url = String.format("%s/tv/popular?api_key=%s&language=ko-KR&page=1", apiUrl, apiKey);
            Map<String, Object> response = restTemplate.getForObject(url, Map.class);
            return parseTmdbResponse(response, ContentType.tvSeries);
        } catch (Exception e) {
            log.error("Failed to fetch TV series from TMDB API, falling back to mock data: {}", e.getMessage());
            return getMockTvSeries();
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

    private List<CollectedContentDto> getMockMovies() {
        return List.of(
                new CollectedContentDto(
                        ContentType.movie,
                        "인셉션 (Inception)",
                        "타인의 꿈속에 침투해 생각을 심는 기밀 작전을 다룬 크리스토퍼 놀란 감독의 SF 액션 걸작.",
                        "inception-poster-key"
                ),
                new CollectedContentDto(
                        ContentType.movie,
                        "인터스텔라 (Interstellar)",
                        "세계 각국의 정부와 경제가 붕괴된 미래, 인류의 구원을 위해 시공간의 틈을 찾아 떠나는 우주 여행가들의 이야기.",
                        "interstellar-poster-key"
                ),
                new CollectedContentDto(
                        ContentType.movie,
                        "기생충 (Parasite)",
                        "전원백수로 살 길 막막하지만 사이는 좋은 기택 가족과 글로벌 IT기업 CEO 박사장 가족의 만남이 걷잡을 수 없는 사건으로 번져가는 이야기.",
                        "parasite-poster-key"
                ),
                new CollectedContentDto(
                        ContentType.movie,
                        "다크 나이트 (The Dark Knight)",
                        "고담시의 평화를 지키려는 배트맨과 도시를 혼돈에 빠뜨리려는 최악의 범죄자 조커의 숨 막히는 대결.",
                        "darkknight-poster-key"
                ),
                new CollectedContentDto(
                        ContentType.movie,
                        "겨울왕국 2 (Frozen 2)",
                        "아렌델 왕국의 숨겨진 과거의 비밀과 엘사의 신비로운 힘의 원천을 찾아 떠나는 엘사와 안나의 새로운 모험.",
                        "frozen2-poster-key"
                )
        );
    }

    private List<CollectedContentDto> getMockTvSeries() {
        return List.of(
                new CollectedContentDto(
                        ContentType.tvSeries,
                        "오징어 게임 (Squid Game)",
                        "456억 원의 상금이 걸린 의문의 서바이벌 게임에 참가한 사람들이 최후의 승자가 되기 위해 목숨을 걸고 도전하는 이야기.",
                        "squidgame-poster-key"
                ),
                new CollectedContentDto(
                        ContentType.tvSeries,
                        "사랑의 불시착 (Crash Landing on You)",
                        "어느 날 패러글라이딩 사고로 북한에 불시착한 재벌 상속녀 윤세리와 그녀를 숨기고 지키다 사랑하게 되는 특급 장교 리정혁의 극비 러브스토리.",
                        "cloy-poster-key"
                ),
                new CollectedContentDto(
                        ContentType.tvSeries,
                        "이태원 클라쓰 (Itaewon Class)",
                        "불합리한 세상 속, 고집과 객기로 뭉친 청춘들의 '힙'한 반란. 세계를 압축해 놓은 듯한 이태원 골목에서 각자의 가치관으로 자유를 쫓는 그들의 창업 신화.",
                        "itaewonclass-poster-key"
                ),
                new CollectedContentDto(
                        ContentType.tvSeries,
                        "기묘한 이야기 (Stranger Things)",
                        "인디애나주의 작은 마을에서 한 소년이 흔적도 없이 사라지자 미스터리한 힘을 가진 소녀가 나타나고, 초자연적 현상에 맞서 싸우는 친구들의 이야기.",
                        "strangerthings-poster-key"
                )
        );
    }
}

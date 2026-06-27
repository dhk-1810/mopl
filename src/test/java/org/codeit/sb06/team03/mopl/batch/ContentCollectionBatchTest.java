package org.codeit.sb06.team03.mopl.batch;

import org.codeit.sb06.team03.mopl.batch.client.TmdbClient;
import org.codeit.sb06.team03.mopl.batch.client.SportsDbClient;
import org.codeit.sb06.team03.mopl.batch.dto.CollectedContentDto;
import org.codeit.sb06.team03.mopl.content.infra.out.ContentRepository;
import org.codeit.sb06.team03.mopl.content.domain.vo.ContentType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.test.JobLauncherTestUtils;
import org.springframework.batch.test.context.SpringBatchTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@SpringBatchTest
@ActiveProfiles("test")
public class ContentCollectionBatchTest {

    @Autowired
    private JobLauncherTestUtils jobLauncherTestUtils;

    @Autowired
    private ContentRepository contentRepository;

    @MockBean
    private TmdbClient tmdbClient;

    @MockBean
    private SportsDbClient sportsDbClient;

    @BeforeEach
    void setUp() {
        Mockito.when(tmdbClient.fetchMovies()).thenReturn(List.of(
                new CollectedContentDto(ContentType.movie, "Inception", "Inception desc", "inception-key")
        ));
        Mockito.when(tmdbClient.fetchTvSeries()).thenReturn(List.of(
                new CollectedContentDto(ContentType.tvSeries, "Squid Game", "Squid Game desc", "squid-game-key")
        ));
        Mockito.when(sportsDbClient.fetchSports()).thenReturn(List.of(
                new CollectedContentDto(ContentType.sport, "Arsenal vs Chelsea", "Arsenal vs Chelsea desc", "arsenal-key")
        ));
    }

    @Test
    public void testJobExecution() throws Exception {
        // Given
        JobParameters jobParameters = new JobParametersBuilder()
                .addLong("runTime", System.currentTimeMillis())
                .toJobParameters();

        // When
        JobExecution jobExecution = jobLauncherTestUtils.launchJob(jobParameters);

        // Then
        assertEquals(ExitStatus.COMPLETED.getExitCode(), jobExecution.getExitStatus().getExitCode());

        // Check that movies, TV series, and sports are collected in database
        long movieCount = contentRepository.findAll().stream()
                .filter(c -> c.getType() == ContentType.movie)
                .count();
        long tvCount = contentRepository.findAll().stream()
                .filter(c -> c.getType() == ContentType.tvSeries)
                .count();
        long sportCount = contentRepository.findAll().stream()
                .filter(c -> c.getType() == ContentType.sport)
                .count();

        logCounts(movieCount, tvCount, sportCount);

        assertTrue(movieCount > 0, "Movies should be collected");
        assertTrue(tvCount > 0, "TV series should be collected");
        assertTrue(sportCount > 0, "Sports events should be collected");
    }

    private void logCounts(long movieCount, long tvCount, long sportCount) {
        System.out.println("Collected Movie Count: " + movieCount);
        System.out.println("Collected TV Series Count: " + tvCount);
        System.out.println("Collected Sport Count: " + sportCount);
    }
}

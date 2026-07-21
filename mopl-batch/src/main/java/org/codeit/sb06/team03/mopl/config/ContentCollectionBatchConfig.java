package org.codeit.sb06.team03.mopl.config;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.codeit.sb06.team03.mopl.dto.CollectedContentDto;
import org.codeit.sb06.team03.mopl.dto.ContentCreateRequest;
import org.codeit.sb06.team03.mopl.dto.CursorResponseContentDto;
import org.codeit.sb06.team03.mopl.client.TmdbClient;
import org.codeit.sb06.team03.mopl.client.SportsDbClient;
import org.codeit.sb06.team03.mopl.client.ContentClient;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.lang.Nullable;
import org.springframework.transaction.PlatformTransactionManager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class ContentCollectionBatchConfig {

    private final PlatformTransactionManager transactionManager;
    private final JobRepository jobRepository;
    private final TmdbClient tmdbClient;
    private final SportsDbClient sportsDbClient;
    private final ContentClient contentClient;
    private final MeterRegistry meterRegistry;

    @Bean
    public Job contentCollectionJob(Step collectTmdbStep, Step collectSportsStep) {
        return new JobBuilder("contentCollectionJob", jobRepository)
                .start(collectTmdbStep)
                .next(collectSportsStep)
                .build();
    }

    @Bean
    public Step collectTmdbStep() {
        return new StepBuilder("collectTmdbStep", jobRepository)
                .<CollectedContentDto, ContentCreateRequest>chunk(10, transactionManager)
                .reader(tmdbReader())
                .processor(contentProcessor())
                .writer(contentWriter("TMDB"))
                .build();
    }

    @Bean
    public Step collectSportsStep() {
        return new StepBuilder("collectSportsStep", jobRepository)
                .<CollectedContentDto, ContentCreateRequest>chunk(10, transactionManager)
                .reader(sportsDbReader())
                .processor(contentProcessor())
                .writer(contentWriter("SportsDB"))
                .build();
    }

    @Bean
    public ItemReader<CollectedContentDto> tmdbReader() {
        return new ItemReader<>() {
            private List<CollectedContentDto> items;
            private int nextIndex = 0;

            @Nullable
            @Override
            public CollectedContentDto read() {
                if (items == null) {
                    log.info("Fetching TMDB content...");
                    items = new ArrayList<>();
                    items.addAll(tmdbClient.fetchMovies());
                    items.addAll(tmdbClient.fetchTvSeries());
                }
                if (nextIndex < items.size()) {
                    return items.get(nextIndex++);
                }
                return null;
            }
        };
    }

    @Bean
    public ItemReader<CollectedContentDto> sportsDbReader() {
        return new ItemReader<>() {
            private List<CollectedContentDto> items;
            private int nextIndex = 0;

            @Nullable
            @Override
            public CollectedContentDto read() {
                if (items == null) {
                    log.info("Fetching Sports DB content...");
                    items = sportsDbClient.fetchSports();
                }
                if (nextIndex < items.size()) {
                    return items.get(nextIndex++);
                }
                return null;
            }
        };
    }

    @Bean
    public ItemProcessor<CollectedContentDto, ContentCreateRequest> contentProcessor() {
        return dto -> {
            CursorResponseContentDto response = contentClient.getContents(dto.title(), dto.type().name());
            boolean exists = response != null && response.data() != null && !response.data().isEmpty();
            if (exists) {
                log.info("Content already exists, skipping: {} ({})", dto.title(), dto.type());
                return null;
            }
            return new ContentCreateRequest(dto.type(), dto.title(), dto.description(), Collections.emptySet(), dto.thumbnailKey());
        };
    }

    @Bean
    public ItemWriter<ContentCreateRequest> contentWriter(String source) {
        Counter successCounter = Counter.builder("mopl.batch.content.collected")
                .description("Number of contents collected successfully")
                .tag("source", source)
                .register(meterRegistry);

        return chunk -> {
            List<? extends ContentCreateRequest> items = chunk.getItems();
            for (ContentCreateRequest item : items) {
                contentClient.createInternal(item);
                successCounter.increment();
            }
            log.info("Saved {} contents from {}", items.size(), source);
        };
    }
}

package org.codeit.sb06.team03.mopl.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@EnableScheduling
@RequiredArgsConstructor
public class BatchScheduler {

    private final JobLauncher jobLauncher;
    private final Job contentCollectionJob;

    @Scheduled(cron = "${mopl.batch.cron}")
    public void runContentCollectionJob() {
        try {
            log.info("Starting scheduled Content Collection Job...");
            JobParameters jobParameters = new JobParametersBuilder()
                    .addLong("runTime", System.currentTimeMillis())
                    .toJobParameters();
            jobLauncher.run(contentCollectionJob, jobParameters);
            log.info("Scheduled Content Collection Job completed successfully.");
        } catch (Exception e) {
            log.error("Failed to run scheduled Content Collection Job: {}", e.getMessage(), e);
        }
    }
}

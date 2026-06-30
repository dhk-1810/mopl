package org.codeit.sb06.team03.mopl.batch;

import jakarta.annotation.security.RolesAllowed;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/admin/batch")
@RequiredArgsConstructor
public class AdminBatchController {

    private final JobLauncher jobLauncher;
    private final Job contentCollectionJob;

    @PostMapping("/run-collection")
    @RolesAllowed("ADMIN")
    public ResponseEntity<String> runCollectionJob() {
        try {
            log.info("Manual trigger: Starting Content Collection Job...");
            JobParameters jobParameters = new JobParametersBuilder()
                    .addLong("runTime", System.currentTimeMillis())
                    .toJobParameters();
            JobExecution execution = jobLauncher.run(contentCollectionJob, jobParameters);
            return ResponseEntity.ok("Batch job started. Status: " + execution.getStatus().toString());
        } catch (Exception e) {
            log.error("Failed to manually start Content Collection Job: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().body("Failed to run batch job: " + e.getMessage());
        }
    }
}

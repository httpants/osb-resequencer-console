package au.com.cyberavenue.osb.resequencer.batch.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.launch.JobOperator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

import au.com.cyberavenue.osb.resequencer.entity.seqretryprocessor.GlobalConfig;
import au.com.cyberavenue.osb.resequencer.service.RetryConfigService;
import net.javacrumbs.shedlock.spring.annotation.EnableSchedulerLock;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;

@Configuration
@EnableScheduling
@EnableSchedulerLock(defaultLockAtMostFor = "10m", defaultLockAtLeastFor = "1m")
@EnableConfigurationProperties(ApplicationProperties.class)
public class SchedulerConfiguration {

    private static final Logger log = LoggerFactory.getLogger(SchedulerConfiguration.class);

    @Autowired
    private JobLauncher jobLauncher;

    @Autowired
    private JobOperator jobOperator;

    @Autowired
    private Job retryJob;

    @Autowired
    private Job purgeJob;

    @Autowired
    private RetryConfigService retryConfigService;

    @Scheduled(cron = "#{@applicationProperties.retryJobSchedule}")
    @SchedulerLock(name = "performRetry")
    public void performRetry() {
        GlobalConfig gc = retryConfigService.getGlobalConfig();
        if (gc.isRetriesEnabled()) {
            log.debug("::::::::::::::::::: Performing Retry For Faulted Messages");
            Long jobId = System.currentTimeMillis();
            JobParameters params = new JobParametersBuilder().addLong("JobID", jobId).toJobParameters();
            try {
                jobLauncher.run(retryJob, params);
            } catch (Exception e) {
                log.error("***** Exception: " + e.getMessage(), e);
            }
        } else {
            log.warn("Automatic retries are disabled!");
        }
    }

    @Scheduled(cron = "#{@applicationProperties.purgeJobSchedule}")
    @SchedulerLock(name = "performPurge")
    public void performPurge() {
        log.info("::::::::::::::::::: Performing Purge Operation For Messages That Are Already Processed");
        Long jobId = System.currentTimeMillis();
        JobParameters params = new JobParametersBuilder().addLong("JobID", jobId).toJobParameters();
        try {
            jobLauncher.run(purgeJob, params);
        } catch (Exception e) {
            log.error("***** Exception: " + e.getMessage(), e);
        }
    }

    @Scheduled(cron = "#{@applicationProperties.purgeOsbResequencerJobSchedule}")
    @SchedulerLock(name = "purgeOsbResequencer", lockAtLeastFor = "15m", lockAtMostFor = "240m")
    public void purgeOsbReseqeuncer() {
        log.info("::::::::::::::::::: Performing Purge Of Osb reseqeuncer tables ");
        try {
            jobOperator.startNextInstance("purgeOsbResequencer");
        } catch (Exception e) {
            log.error("***** Exception: " + e.getMessage(), e);
        }
    }

    @Scheduled(cron = "#{@applicationProperties.purgeSpringBatchJobSchedule}")
    @SchedulerLock(name = "purgeSpringBatch", lockAtLeastFor = "15m", lockAtMostFor = "240m")
    public void purgeSpringBatch() {
        log.info("::::::::::::::::::: purging spring batch tables ");
        try {
            jobOperator.startNextInstance("purgeSpringBatch");
        } catch (Exception e) {
            log.error("***** Exception: " + e.getMessage(), e);
        }
    }

}

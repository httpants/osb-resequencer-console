package au.com.cyberavenue.osb.resequencer.batch.config;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Properties specific to Retry Batch Job.
 * <p>
 * Properties are configured in the application.yml file.
 */

@ConfigurationProperties(prefix = "application", ignoreUnknownFields = false)
@Component("applicationProperties")
public class ApplicationProperties {

    private static final Logger log = LoggerFactory.getLogger(ApplicationProperties.class);

    private String environment;

    private String navbarBackgroundColor;

    private String retryJobSchedule;

    private String purgeJobSchedule;

    private String purgeOsbResequencerJobSchedule;

    private String purgeSpringBatchJobSchedule;

    private String iconsoleSearchUrl;

    private final Defaults defaults = new Defaults();

    public String getEnvironment() {
        return environment;
    }

    public void setEnvironment(String environment) {
        this.environment = environment;
    }

    public String getNavbarBackgroundColor() {
        return navbarBackgroundColor;
    }

    public void setNavbarBackgroundColor(String navbarBackgroundColor) {
        this.navbarBackgroundColor = navbarBackgroundColor;
    }

    public String getRetryJobSchedule() {
        return retryJobSchedule;
    }

    public void setRetryJobSchedule(String retryJobSchedule) {
        this.retryJobSchedule = retryJobSchedule;
    }

    public String getPurgeJobSchedule() {
        return purgeJobSchedule;
    }

    public void setPurgeJobSchedule(String purgeJobSchedule) {
        this.purgeJobSchedule = purgeJobSchedule;
    }

    public String getPurgeOsbResequencerJobSchedule() {
        return purgeOsbResequencerJobSchedule;
    }

    public void setPurgeOsbResequencerJobSchedule(String purgeOsbResequencerJobSchedule) {
        this.purgeOsbResequencerJobSchedule = purgeOsbResequencerJobSchedule;
    }

    public String getPurgeSpringBatchJobSchedule() {
        return purgeSpringBatchJobSchedule;
    }

    public void setPurgeSpringBatchJobSchedule(String purgeSpringBatchJobSchedule) {
        this.purgeSpringBatchJobSchedule = purgeSpringBatchJobSchedule;
    }

    public String getIconsoleSearchUrl() {
        return iconsoleSearchUrl;
    }

    public void setIconsoleSearchUrl(String iconsoleSearchUrl) {
        this.iconsoleSearchUrl = iconsoleSearchUrl;
    }

    public Defaults getDefaults() {
        return defaults;
    }

    @Override
    public String toString() {
        return "ApplicationProperties [defaults=" + defaults + ", environment=" + environment + ", retryJobSchedule="
                + retryJobSchedule + ", purgeJobSchedule=" + purgeJobSchedule + ", purgeOsbResequencerJobSchedule="
                + purgeOsbResequencerJobSchedule + ", purgeSpringBatchJobSchedule=" + purgeSpringBatchJobSchedule
                + ", iconsoleSearchUrl=" + iconsoleSearchUrl + "]";
    }

    @PostConstruct
    public void log() {
        log.debug(toString());
    }

    public static class Defaults {

        private boolean retriesEnabled = true;
        private int retries = 10;
        private int delay = 300;
        private int delayFactor = 2;

        public boolean isRetriesEnabled() {
            return retriesEnabled;
        }

        public void setRetriesEnabled(boolean retriesEnabled) {
            this.retriesEnabled = retriesEnabled;
        }

        public int getRetries() {
            return retries;
        }

        public void setRetries(int reties) {
            this.retries = reties;
        }

        public int getDelay() {
            return delay;
        }

        public void setDelay(int delay) {
            this.delay = delay;
        }

        public int getDelayFactor() {
            return delayFactor;
        }

        public void setDelayFactor(int delayFactor) {
            this.delayFactor = delayFactor;
        }

        @Override
        public String toString() {
            return "Defaults [retries=" + retries + ", delay=" + delay + ", delayFactor=" + delayFactor + "]";
        }

    }
}

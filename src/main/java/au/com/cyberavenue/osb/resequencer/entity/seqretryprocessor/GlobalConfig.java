package au.com.cyberavenue.osb.resequencer.entity.seqretryprocessor;

import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "GLOBAL_CONFIG")
public class GlobalConfig {

    public static final String ID = "resequencer-retry-processor";

    @Id
    @Column(name = "ID")
    private String id = ID;

    @Column(name = "RETRIES_ENABLED")
    @Convert(converter = BooleanConverter.class)
    private boolean retriesEnabled;

    @Column(name = "RETRY_LIMIT")
    private Integer retryLimit;

    @Column(name = "DELAY")
    private Integer delay;

    @Column(name = "DELAY_FACTOR")
    private Integer delayFactor;

    public String getId() {
        return id;
    }

    public boolean isRetriesEnabled() {
        return retriesEnabled;
    }

    public void setRetriesEnabled(boolean retriesEnabled) {
        this.retriesEnabled = retriesEnabled;
    }

    public Integer getRetryLimit() {
        return retryLimit;
    }

    public void setRetryLimit(Integer retryLimit) {
        this.retryLimit = retryLimit;
    }

    public Integer getDelay() {
        return delay;
    }

    public void setDelay(Integer delay) {
        this.delay = delay;
    }

    public Integer getDelayFactor() {
        return delayFactor;
    }

    public void setDelayFactor(Integer delayFactor) {
        this.delayFactor = delayFactor;
    }
}

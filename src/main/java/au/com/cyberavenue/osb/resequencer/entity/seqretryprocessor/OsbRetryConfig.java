package au.com.cyberavenue.osb.resequencer.entity.seqretryprocessor;

import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.Table;

@Entity
@Table(name = "OSB_RETRY_CONFIG")
public class OsbRetryConfig {

    @EmbeddedId
    private ComponentOperationId id;

    @Column(name = "RETRY_LIMIT")
    private Integer retryLimit;

    @Column(name = "DELAY")
    private Integer delay;

    @Column(name = "DELAY_FACTOR")
    private Integer delayFactor;

    public ComponentOperationId getId() {
        return id;
    }

    public void setId(ComponentOperationId id) {
        this.id = id;
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

    @Override
    public String toString() {
        return "OsbRetryConfig [id=" + id + ", retryLimit=" + retryLimit + ", delay=" + delay + ", delayFactor="
                + delayFactor + "]";
    }

}

package au.com.cyberavenue.osb.resequencer.entity.seqretryprocessor;

import java.math.BigInteger;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

@XmlRootElement(name = "RETRYCONFIG")
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "RetryConfigType", propOrder = { "retryLimit", "delay", "delayFactor" })
public class OperationRetryConfig {

    @XmlElement(name = "Retry_Limit", required = true)
    protected BigInteger retryLimit;

    @XmlElement(name = "Delay", required = true)
    protected BigInteger delay;

    @XmlElement(name = "Delay_Factor", required = true)
    protected BigInteger delayFactor;

    public OperationRetryConfig() {}

    public OperationRetryConfig(Integer retryLimit, Integer delay, Integer delayFactor) {
        this(BigInteger.valueOf(retryLimit), BigInteger.valueOf(delay), BigInteger.valueOf(delayFactor));
    }

    public OperationRetryConfig(BigInteger retryLimit, BigInteger delay, BigInteger delayFactor) {
        this.retryLimit = retryLimit;
        this.delay = delay;
        this.delayFactor = delayFactor;
    }

    public BigInteger getRetryLimit() {
        return retryLimit;
    }

    public void setRetryLimit(BigInteger retryLimit) {
        this.retryLimit = retryLimit;
    }

    public BigInteger getDelay() {
        return delay;
    }

    public void setDelay(BigInteger delay) {
        this.delay = delay;
    }

    public BigInteger getDelayFactor() {
        return delayFactor;
    }

    public void setDelayFactor(BigInteger delayFactor) {
        this.delayFactor = delayFactor;
    }

    @Override
    public String toString() {
        return "RetryConfig{" +
                "retryLimit=" + retryLimit +
                ", delay=" + delay +
                ", delayFactor=" + delayFactor +
                '}';
    }
}

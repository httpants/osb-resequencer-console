package au.com.cyberavenue.osb.resequencer.batch.retry.config;

import java.sql.Types;
import java.util.List;

import javax.sql.DataSource;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcOperations;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.SqlParameter;
import org.springframework.jdbc.object.SqlUpdate;
import org.springframework.util.Assert;

import au.com.cyberavenue.osb.resequencer.entity.seqretryprocessor.OperationRetryConfig;
import au.com.cyberavenue.osb.resequencer.service.RetryConfigService;
import io.github.resilience4j.core.IntervalFunction;

public class RetryMessageProcessor implements ItemProcessor<Message, RetryMessage>, InitializingBean {

    private static final Logger log = LoggerFactory.getLogger(RetryMessageProcessor.class);

    private DataSource soaInfraDataSource;

    private JdbcOperations soaInfraJdbcTemplate;

    private JdbcOperations retryProcessorJdbcTemplate;

    private static final String UPDATE_MESSAGE_SQL = "update OSB_RESEQUENCER_MESSAGE set STATUS = ? where ID = ?";

    private static final String UPDATE_GROUP_SQL = "update OSB_GROUP_STATUS set STATUS = ? where ID = ?";

    private static final String SELECT_RETRY_MESSAGES_SQL = "SELECT * from OSB_RETRY_MESSAGE WHERE MESSAGE_ID = ? ORDER BY RETRY_DATE DESC";

    public static final DateTimeFormatter formatter = DateTimeFormat.forPattern("dd/MM/yyyy HH:mm:ss");

    @Autowired
    private RetryConfigService retryConfigService;

    @Autowired
    @Qualifier("soaInfraDataSource")
    public void setSoaInfraDataSource(DataSource soaInfraDataSource) {
        this.soaInfraDataSource = soaInfraDataSource;
        soaInfraJdbcTemplate = new JdbcTemplate(soaInfraDataSource);
    }

    @Autowired
    @Qualifier("batchDataSource")
    public void setRetryProcessorDataSource(DataSource retryProcessorDataSource) {
        retryProcessorJdbcTemplate = new JdbcTemplate(retryProcessorDataSource);
    }

    @Override
    public final void afterPropertiesSet() {
        Assert.notNull(soaInfraJdbcTemplate, "You must provide a DataSource.");
    }

    @Override
    public RetryMessage process(Message message) {

        OperationRetryConfig operationRetryConfig = retryConfigService
                .getOperationConfig(message.getComponentDn(), message.getOperation());

        log.debug("The Operation Config for ComponentDN: '{}' and Operation: '{}' is: {}", message.getComponentDn(),
                message.getOperation(), operationRetryConfig);

        List<RetryMessage> retries = retryProcessorJdbcTemplate.query(SELECT_RETRY_MESSAGES_SQL,
                new Object[] { message.getId() }, new RetryMessageRowMapper());

        if (retries.size() >= operationRetryConfig.getRetryLimit().intValue()) {
            log.info("Retry limits " + retries.size() + "/" + operationRetryConfig.getRetryLimit()
                    + " reached for message: " + message);
            update(message.getId(), 3, UPDATE_MESSAGE_SQL);
            update(message.getOwnerId(), 6, UPDATE_GROUP_SQL);
            return null;
        } else {
            if (isRetryable(message, retries, operationRetryConfig)) {
                int attemptNo = retries.size() + 1;
                log.debug("Retrying attempt no. " + (attemptNo) + "/" + operationRetryConfig.getRetryLimit()
                        + " for message: " + message);
                update(message.getId(), 0, UPDATE_MESSAGE_SQL);
                update(message.getOwnerId(), 0, UPDATE_GROUP_SQL);
                return new RetryMessage(message.getId(), message.getOwnerId(), message.getComponentDn(),
                        message.getGroupId());
            } else {
                return null;
            }
        }

    }

    private boolean isRetryable(Message message, List<RetryMessage> retries,
            OperationRetryConfig operationRetryConfig) {

        int attemptNo = retries.size() + 1;

        int retryDelaySeconds = IntervalFunction.ofExponentialBackoff(
                operationRetryConfig.getDelay().longValue(),
                operationRetryConfig.getDelayFactor().doubleValue())
                .apply(attemptNo)
                .intValue();

        DateTime lastAttemptDateTime = new DateTime(
                attemptNo > 1 ? retries.get(0).getRetryDate() : message.getGroupLastUpdated());
        DateTime retryDateTime = lastAttemptDateTime.plusSeconds(retryDelaySeconds);

        log.debug("Retry attempt no " + attemptNo + ": last attempt time was "
                + lastAttemptDateTime.toString(formatter) + ".  After " + retryDelaySeconds
                + " seconds next attempt time is "
                + retryDateTime.toString(formatter));

        return retryDateTime.isBeforeNow();
    }

    private void update(String id, int status, String sql) {
        SqlUpdate sqlUpdate = new SqlUpdate(soaInfraDataSource, sql);
        sqlUpdate.declareParameter(new SqlParameter("STATUS", Types.INTEGER));
        sqlUpdate.declareParameter(new SqlParameter("ID", Types.VARCHAR));
        sqlUpdate.compile();
        sqlUpdate.update(status, id);
    }

}

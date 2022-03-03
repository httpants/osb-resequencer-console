package au.com.cyberavenue.osb.resequencer.batch.retry.config;

import javax.sql.DataSource;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.item.database.BeanPropertyItemSqlParameterSourceProvider;
import org.springframework.batch.item.database.JdbcBatchItemWriter;
import org.springframework.batch.item.database.JdbcCursorItemReader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

@Configuration
public class RetryJobConfig {

    private JobBuilderFactory jobBuilderFactory;

    private StepBuilderFactory stepBuilderFactory;

    private DataSource soaInfraDataSource;
    private DataSource retryProcessorDataSource;

    private static final String SELECT_FAULTED_MESSAGES_FOR_FAULTED_GROUPS_SQL = "SELECT m.id, m.owner_id, m.component_dn, m.operation, m.group_id, m.status, g.last_received_time FROM OSB_RESEQUENCER_MESSAGE m INNER JOIN OSB_GROUP_STATUS g ON g.ID = m.owner_id WHERE g.status = 3 AND m.status = 3 ORDER BY CREATION_DATE ASC";

    @Bean
    public JdbcTemplate soaInfraJdbcTemplate() {
        return new JdbcTemplate(soaInfraDataSource);
    }

    @Bean
    public NamedParameterJdbcTemplate soaInfraNamedParameterJdbcTemplate() {
        return new NamedParameterJdbcTemplate(soaInfraDataSource);
    }

    @Bean
    public JdbcCursorItemReader<Message> faultedMessagesReader() {
        JdbcCursorItemReader<Message> reader = new JdbcCursorItemReader<>();
        reader.setSql(SELECT_FAULTED_MESSAGES_FOR_FAULTED_GROUPS_SQL);
        reader.setDataSource(soaInfraDataSource);
        reader.setRowMapper(new MessageRowMapper());
        return reader;
    }

    @Bean
    public RetryMessageProcessor faultedMessageProcessor() {
        return new RetryMessageProcessor();
    }

    @Bean
    public JdbcBatchItemWriter<RetryMessage> faultedMessagesWriter() {
        JdbcBatchItemWriter<RetryMessage> writer = new JdbcBatchItemWriter<>();
        writer.setDataSource(retryProcessorDataSource);
        writer.setSql(
                "INSERT INTO OSB_RETRY_MESSAGE (MESSAGE_ID, OWNER_ID, COMPONENT_DN, GROUP_ID, RETRY_DATE) VALUES (:messageId, :ownerId, :componentDn, :groupId, :retryDate)");
        writer.setItemSqlParameterSourceProvider(new BeanPropertyItemSqlParameterSourceProvider<>());
        writer.afterPropertiesSet();
        return writer;
    }

    @Bean
    public Step faultedMessageRetryStep() {
        return stepBuilderFactory.get("faultedMessageRetryStep").<Message, RetryMessage>chunk(100)
                .reader(faultedMessagesReader()).processor(faultedMessageProcessor()).writer(faultedMessagesWriter())
                .build();
    }

    @Bean
    public Job retryJob() {
        return jobBuilderFactory.get("retryJob").flow(faultedMessageRetryStep()).end().build();
    }

    @Autowired
    public void setJobBuilderFactory(JobBuilderFactory jobBuilderFactory) {
        this.jobBuilderFactory = jobBuilderFactory;
    }

    @Autowired
    public void setStepBuilderFactory(StepBuilderFactory stepBuilderFactory) {
        this.stepBuilderFactory = stepBuilderFactory;
    }

    @Autowired
    @Qualifier("soaInfraDataSource")
    public void setSoaInfraDataSource(DataSource soaInfraDataSource) {
        this.soaInfraDataSource = soaInfraDataSource;
    }

    @Autowired
    @Qualifier("batchDataSource")
    public void setRetryProcessorDataSource(DataSource retryProcessorDataSource) {
        this.retryProcessorDataSource = retryProcessorDataSource;
    }
}
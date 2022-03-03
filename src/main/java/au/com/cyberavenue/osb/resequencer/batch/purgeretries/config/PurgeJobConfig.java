package au.com.cyberavenue.osb.resequencer.batch.purgeretries.config;

import javax.sql.DataSource;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.database.JdbcBatchItemWriter;
import org.springframework.batch.item.database.JdbcCursorItemReader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import au.com.cyberavenue.osb.resequencer.batch.purgeretries.processor.PurgeMessageProcessor;

@Configuration
public class PurgeJobConfig {

    private JobBuilderFactory jobBuilderFactory;

    private StepBuilderFactory stepBuilderFactory;

    private DataSource batchDataSource;

    private static final String SELECT_MESSAGES_FOR_PURGING_SQL = "select distinct MESSAGE_ID from OSB_RETRY_MESSAGE";

    @Bean
    @StepScope
    public JdbcCursorItemReader<String> purgeMessagesReader() {
        JdbcCursorItemReader<String> reader = new JdbcCursorItemReader<>();
        reader.setSql(SELECT_MESSAGES_FOR_PURGING_SQL);
        reader.setDataSource(batchDataSource);
        reader.setRowMapper((rs, i) -> rs.getString("message_id"));
        return reader;
    }

    @Bean
    @StepScope
    public PurgeMessageProcessor purgeMessageProcessor() {
        return new PurgeMessageProcessor();
    }

    @Bean
    @StepScope
    public JdbcBatchItemWriter<String> purgeMessageWriter() {
        JdbcBatchItemWriter<String> writer = new JdbcBatchItemWriter<>();
        writer.setDataSource(batchDataSource);
        writer.setSql("DELETE FROM OSB_RETRY_MESSAGE WHERE MESSAGE_ID = ?");
        writer.setItemPreparedStatementSetter((id, ps) -> ps.setString(1, id));
        return writer;
    }

    @Bean
    public Step purgeStep() {
        return stepBuilderFactory.get("purgeStep").<String, String>chunk(100)
                .reader(purgeMessagesReader())
                .processor(purgeMessageProcessor())
                .writer(purgeMessageWriter())
                .build();
    }

    @Bean
    public Job purgeJob() {
        return jobBuilderFactory
                .get("purgeJob")
                .flow(purgeStep()).end()
                .build();
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
    @Qualifier("batchDataSource")
    public void setBatchDataSource(DataSource batchDataSource) {
        this.batchDataSource = batchDataSource;
    }

}

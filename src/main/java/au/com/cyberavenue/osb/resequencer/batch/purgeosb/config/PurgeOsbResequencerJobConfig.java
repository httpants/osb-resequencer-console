package au.com.cyberavenue.osb.resequencer.batch.purgeosb.config;

import java.sql.Timestamp;
import java.time.LocalDate;

import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.database.JdbcCursorItemReader;
import org.springframework.batch.item.database.builder.JdbcBatchItemWriterBuilder;
import org.springframework.batch.item.database.builder.JdbcCursorItemReaderBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;

@Configuration
public class PurgeOsbResequencerJobConfig {

    private static final Logger log = LoggerFactory.getLogger(PurgeOsbResequencerJobConfig.class);

    @Autowired
    private JobBuilderFactory jobBuilderFactory;

    @Autowired
    private StepBuilderFactory stepBuilderFactory;

    @Autowired
    @Qualifier("soaInfraDataSource")
    private DataSource soaInfraDataSource;

    private static final String SELECT_PURGABLE_OSG_MSGS_SQL = "select a.MSG_ID from OSB_MSG a"
            + " WHERE EXISTS (SELECT 1 FROM OSB_RESEQUENCER_MESSAGE b, OSB_GROUP_STATUS c "
            + "   WHERE b.ID = a.MSG_ID "
            + "   AND b.OWNER_ID = c.ID "
            + "   AND b.STATUS in (2, 5) "
            + "   AND b.CREATION_DATE <= ? "
            + "   )";

    @Bean
    @StepScope
    public JdbcCursorItemReader<String> purgableOsbMsgReader() {
        return new JdbcCursorItemReaderBuilder<String>()
                .name("purgableOsbMsgReader")
                .dataSource(soaInfraDataSource)
                .sql(SELECT_PURGABLE_OSG_MSGS_SQL)
                .queryArguments(Timestamp.valueOf(LocalDate.now().minusDays(1).atStartOfDay()))
                .rowMapper((rs, i) -> rs.getString("MSG_ID"))
                .saveState(false)
                .build();
    }

    @Bean
    @StepScope
    public ItemProcessor<String, String> purgableOsbMsgProcessor() {
        return (osbMsgId) -> {
            log.debug("deleting OSB_MSG {}", osbMsgId);
            return osbMsgId;
        };
    }

    @Bean
    @StepScope
    public ItemWriter<String> purgableOsbMsgWriter() {
        return new JdbcBatchItemWriterBuilder<String>()
                .dataSource(soaInfraDataSource)
                .sql("delete from OSB_MSG a where a.MSG_ID = :msgId")
                .itemSqlParameterSourceProvider((osbMsgId) -> new MapSqlParameterSource("msgId", osbMsgId))
                .build();
    }

    @Bean
    public Step purgeOsbMsgsStep() {
        return stepBuilderFactory.get("purgeOsbMsgs").<String, String>chunk(100)
                .reader(purgableOsbMsgReader())
                .processor(purgableOsbMsgProcessor())
                .writer(purgableOsbMsgWriter())
                .build();
    }

    private static final String SELECT_PURGABLE_OSB_RESEQUECER_MESSAGES_SQL = "select b.ID from OSB_RESEQUENCER_MESSAGE b"
            + " WHERE b.STATUS in (2, 5) AND NOT EXISTS (SELECT 1 FROM OSB_MSG a WHERE b.ID = a.MSG_ID)";

    @Bean
    @StepScope
    public JdbcCursorItemReader<String> purgableOsbResequencerMessageReader() {
        return new JdbcCursorItemReaderBuilder<String>()
                .name("purgableOsbResequencerMessageReader")
                .dataSource(soaInfraDataSource)
                .sql(SELECT_PURGABLE_OSB_RESEQUECER_MESSAGES_SQL)
                .rowMapper((rs, i) -> rs.getString("ID"))
                .saveState(false)
                .build();
    }

    @Bean
    @StepScope
    public ItemProcessor<String, String> purgableOsbResequencerMessageProcessor() {
        return (osbResequencerMessageId) -> {
            log.debug("deleting OSB_RESEQUENCER_MESSAGE {}", osbResequencerMessageId);
            return osbResequencerMessageId;
        };
    }

    @Bean
    @StepScope
    public ItemWriter<String> purgableOsbResequencerMessageWriter() {
        return new JdbcBatchItemWriterBuilder<String>()
                .dataSource(soaInfraDataSource)
                .sql("delete from OSB_RESEQUENCER_MESSAGE b where b.ID = :id")
                .itemSqlParameterSourceProvider(
                        (osbResequencerMessageId) -> new MapSqlParameterSource("id", osbResequencerMessageId))
                .build();
    }

    @Bean
    public Step purgeOsbResequencerMessagesStep() {
        return stepBuilderFactory.get("purgeOsbResequencerMessages").<String, String>chunk(100)
                .reader(purgableOsbResequencerMessageReader())
                .processor(purgableOsbResequencerMessageProcessor())
                .writer(purgableOsbResequencerMessageWriter())
                .build();
    }

    private static final String SELECT_PURGABLE_OSB_GROUPS_SQL = "select a.ID from OSB_GROUP_STATUS a"
            + " WHERE a.STATUS = 0 AND a.resequencer_type != 'Standard' "
            + " AND NOT EXISTS (SELECT 1 FROM OSB_RESEQUENCER_MESSAGE b WHERE b.OWNER_ID = a.ID)";

    @Bean
    @StepScope
    public JdbcCursorItemReader<String> purgableOsbGroupReader() {
        return new JdbcCursorItemReaderBuilder<String>()
                .name("purgableOsbGroupReader")
                .dataSource(soaInfraDataSource)
                .sql(SELECT_PURGABLE_OSB_GROUPS_SQL)
                .rowMapper((rs, i) -> rs.getString("ID"))
                .saveState(false)
                .build();
    }

    @Bean
    @StepScope
    public ItemProcessor<String, String> purgableOsbGroupProcessor() {
        return (osbGroupStatusId) -> {
            log.debug("deleting OSB_GROUP_STATUS {}", osbGroupStatusId);
            return osbGroupStatusId;
        };
    }

    @Bean
    @StepScope
    public ItemWriter<String> purgableOsbGroupWriter() {
        return new JdbcBatchItemWriterBuilder<String>()
                .dataSource(soaInfraDataSource)
                .sql("delete from OSB_GROUP_STATUS a where a.ID = :id AND NOT EXISTS (SELECT 1 FROM OSB_RESEQUENCER_MESSAGE b WHERE b.OWNER_ID = a.ID)")
                .itemSqlParameterSourceProvider((osbGroupStatusId) -> new MapSqlParameterSource("id", osbGroupStatusId))
                .assertUpdates(false)
                .build();
    }

    @Bean
    public Step purgeOsbGroupsStep() {
        return stepBuilderFactory.get("purgeOsbGroups").<String, String>chunk(100)
                .reader(purgableOsbGroupReader())
                .processor(purgableOsbGroupProcessor())
                .writer(purgableOsbGroupWriter())
                .build();
    }

    @Bean
    public Job purgeOsbResequencerJob() {
        return jobBuilderFactory
                .get("purgeOsbResequencer")
                .incrementer(new RunIdIncrementer())
                .start(purgeOsbMsgsStep())
                .next(purgeOsbResequencerMessagesStep())
                .next(purgeOsbGroupsStep())
                .build();
    }

}

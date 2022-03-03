package au.com.cyberavenue.osb.resequencer.batch.purgeretries.config;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import java.util.Collections;

import javax.sql.DataSource;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.item.database.JdbcBatchItemWriter;
import org.springframework.batch.item.database.JdbcCursorItemReader;
import org.springframework.batch.test.MetaDataInstanceFactory;
import org.springframework.batch.test.StepScopeTestExecutionListener;
import org.springframework.batch.test.StepScopeTestUtils;
import org.springframework.batch.test.context.SpringBatchTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;
import org.springframework.test.context.transaction.TransactionalTestExecutionListener;
import org.springframework.transaction.annotation.Transactional;

import au.com.cyberavenue.osb.resequencer.TestDatabaseConfiguration;
import au.com.cyberavenue.osb.resequencer.batch.purgeretries.config.PurgeJobConfig;
import au.com.cyberavenue.osb.resequencer.batch.purgeretries.processor.PurgeMessageProcessor;

@SpringBatchTest
@RunWith(SpringRunner.class)
@SpringBootTest(classes = { TestDatabaseConfiguration.class, PurgeJobConfig.class })
@TestExecutionListeners({ DependencyInjectionTestExecutionListener.class, StepScopeTestExecutionListener.class,
		TransactionalTestExecutionListener.class })
@Transactional
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
public class PurgeJobConfigTest {

	@Autowired
	private Job purgeJob;

	@Autowired
	private PurgeMessageProcessor purgeMessageProcessor;

	@Autowired
	private JdbcCursorItemReader<String> purgeMessagesReader;

	@Autowired
	private JdbcBatchItemWriter<String> purgeMessageWriter;

	@Autowired
	@Qualifier("batchDataSource")
	private DataSource batchDataSource;

	private JobParameters jobParameters;

	@Before
	public void setUp() {
		Long jobId = System.currentTimeMillis();
		jobParameters = new JobParametersBuilder().addLong("JobID", jobId).toJobParameters();
	}

	@Test
	public void testNotNull() {
		assertNotNull(purgeJob);
		Assert.assertEquals("purgeJob", purgeJob.getName());
	}

	@Test
	public void testReader() {
		StepExecution stepExecution = MetaDataInstanceFactory.createStepExecution(jobParameters);
		int count = 0;
		try {

			count = StepScopeTestUtils.doInStepScope(stepExecution, () -> {
				int numIds = 0;
				String id;
				try {
					purgeMessagesReader.open(stepExecution.getExecutionContext());
					while ((id = purgeMessagesReader.read()) != null) {
						assertNotNull(id);
						Assert.assertEquals("7f000101.N11aaaf6e.N61.174be386125.N7fa8GreetingService", id);
						numIds++;
					}
				} finally {
					try {
						purgeMessagesReader.close();
					} catch (Exception e) {
						fail(e.toString());
					}
				}
				return numIds;
			});

		} catch (Exception e) {
			fail(e.toString());
		}
		Assert.assertEquals(1, count);
	}

	@Test
	public void testProcessor() {
		String messageId = "7f000101.N11aaaf6e.N61.174be386125.N7fa8GreetingService";
		String processedMessageId = purgeMessageProcessor.process(messageId);
		assertEquals(messageId, processedMessageId);
	}

	@Test
	public void testWriter() throws Exception {
		JdbcTemplate jdbcTemplate = new JdbcTemplate(batchDataSource);
		int count = jdbcTemplate.queryForObject("select count(*) from OSB_RETRY_MESSAGE", Integer.class);
		assertEquals(10, count);

		String messageId = "7f000101.N11aaaf6e.N61.174be386125.N7fa8GreetingService";
		StepExecution execution = MetaDataInstanceFactory.createStepExecution();
		StepScopeTestUtils.doInStepScope(execution, () -> {
			purgeMessageWriter.write(Collections.singletonList(messageId));
			return null;
		});

		count = jdbcTemplate.queryForObject("select count(*) from OSB_RETRY_MESSAGE", Integer.class);
		assertEquals(0, count);
	}

}
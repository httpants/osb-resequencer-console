package au.com.cyberavenue.osb.resequencer.batch.retry.config;

import static org.junit.Assert.*;

import org.joda.time.DateTime;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.item.database.JdbcCursorItemReader;
import org.springframework.batch.test.MetaDataInstanceFactory;
import org.springframework.batch.test.StepScopeTestExecutionListener;
import org.springframework.batch.test.StepScopeTestUtils;
import org.springframework.batch.test.context.SpringBatchTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;
import org.springframework.test.context.transaction.TransactionalTestExecutionListener;
import org.springframework.transaction.annotation.Transactional;

import au.com.cyberavenue.osb.resequencer.TestDatabaseConfiguration;
import au.com.cyberavenue.osb.resequencer.batch.retry.config.Message;
import au.com.cyberavenue.osb.resequencer.batch.retry.config.RetryJobConfig;
import au.com.cyberavenue.osb.resequencer.batch.retry.config.RetryMessage;
import au.com.cyberavenue.osb.resequencer.batch.retry.config.RetryMessageProcessor;

@SpringBatchTest
@RunWith(SpringRunner.class)
@SpringBootTest(classes = { TestDatabaseConfiguration.class, RetryJobConfig.class })
@TestExecutionListeners({ DependencyInjectionTestExecutionListener.class, StepScopeTestExecutionListener.class,
        TransactionalTestExecutionListener.class })
@Transactional
//@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
public class RetryJobConfigTest {

    @Autowired
    private Job retryJob;

    @Autowired
    private JdbcCursorItemReader<Message> faultedMessagesReader;

    @Autowired
    private RetryMessageProcessor faultedMessageProcessor;

    private JobParameters jobParameters;

    @Before
    public void setUp() {
        Long jobId = System.currentTimeMillis();
        jobParameters = new JobParametersBuilder().addLong("JobID", jobId).toJobParameters();
    }

    @Test
    public void testNotNull() {
        assertNotNull(retryJob);
        Assert.assertEquals("retryJob", retryJob.getName());
    }

    @Test
    public void testAReader() throws Exception {
        StepExecution stepExecution = MetaDataInstanceFactory.createStepExecution(jobParameters);
        int count = 0;

        count = StepScopeTestUtils.doInStepScope(stepExecution, () -> {
            int numMessages = 0;
            Message message;
            try {
                faultedMessagesReader.open(stepExecution.getExecutionContext());
                while ((message = faultedMessagesReader.read()) != null) {
                    assertNotNull(message);
                    assertEquals("7f000101.N11aaaf6e.Ne.174c2c3ffd8.N7fdaGreetingService", message.getId());
                    numMessages++;
                }
            } finally {
                try {
                    faultedMessagesReader.close();
                } catch (Exception e) {
                    fail(e.toString());
                }
            }
            return numMessages;
        });

        assertEquals(1, count);
    }

    @Test
    public void testBProcessor() {
        Message message = new Message("7f000101.N11aaaf6e.Ne.174c2c3ffd8.N7fdaGreetingService",
                "TMozVNKrthlXaTLwZb3FQ0t6CRJFLUqt1kQnXVSVG7A=", "Pipeline$sequencer$proxy$GreetingService", "greet",
                "group_1", 3, DateTime.now().minusDays(1).toDate());
        RetryMessage retryMessage = faultedMessageProcessor.process(message);
        assertNotNull(retryMessage);
        assertEquals("7f000101.N11aaaf6e.Ne.174c2c3ffd8.N7fdaGreetingService", retryMessage.getMessageId());
        assertEquals("TMozVNKrthlXaTLwZb3FQ0t6CRJFLUqt1kQnXVSVG7A=", retryMessage.getOwnerId());
        assertEquals("Pipeline$sequencer$proxy$GreetingService", retryMessage.getComponentDn());
        assertEquals("group_1", retryMessage.getGroupId());
    }

}
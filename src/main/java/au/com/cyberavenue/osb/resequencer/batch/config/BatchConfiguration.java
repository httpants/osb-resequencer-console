package au.com.cyberavenue.osb.resequencer.batch.config;

import javax.annotation.PostConstruct;
import javax.sql.DataSource;

import org.springframework.batch.core.configuration.annotation.BatchConfigurer;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.explore.JobExplorer;
import org.springframework.batch.core.explore.support.JobExplorerFactoryBean;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.launch.support.SimpleJobLauncher;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.repository.support.JobRepositoryFactoryBean;
import org.springframework.batch.support.DatabaseType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;

@Component
@EnableBatchProcessing
public class BatchConfiguration implements BatchConfigurer {

    private JobRepository jobRepository;
    private JobExplorer jobExplorer;
    private JobLauncher jobLauncher;

    @Autowired
    @Qualifier(value = "seqRetryProcessorTransactionManager")
    private PlatformTransactionManager batchTransactionManager;

    @Autowired
    @Qualifier(value = "batchDataSource")
    private DataSource batchDataSource;

    @Override
    public JobRepository getJobRepository() {
        return this.jobRepository;
    }

    @Override
    public JobLauncher getJobLauncher() {
        return this.jobLauncher;
    }

    @Override
    public JobExplorer getJobExplorer() {
        return this.jobExplorer;
    }

    @Override
    public PlatformTransactionManager getTransactionManager() {
        return this.batchTransactionManager;
    }

    protected JobLauncher createJobLauncher() throws Exception {
        SimpleJobLauncher jobLauncher = new SimpleJobLauncher();
        jobLauncher.setJobRepository(jobRepository);
        jobLauncher.afterPropertiesSet();
        return jobLauncher;
    }

    protected JobRepository createJobRepository() throws Exception {
        JobRepositoryFactoryBean factory = new JobRepositoryFactoryBean();
        factory.setDatabaseType(String.valueOf(DatabaseType.ORACLE));
        factory.setDataSource(this.batchDataSource);
        factory.setTransactionManager(getTransactionManager());
        factory.setIsolationLevelForCreate("ISOLATION_READ_COMMITTED");
        factory.afterPropertiesSet();
        return factory.getObject();
    }

    @PostConstruct
    public void afterPropertiesSet() throws Exception {
        this.jobRepository = createJobRepository();
        JobExplorerFactoryBean jobExplorerFactoryBean = new JobExplorerFactoryBean();
        jobExplorerFactoryBean.setDataSource(this.batchDataSource);
        jobExplorerFactoryBean.afterPropertiesSet();
        this.jobExplorer = jobExplorerFactoryBean.getObject();
        this.jobLauncher = createJobLauncher();
    }
}

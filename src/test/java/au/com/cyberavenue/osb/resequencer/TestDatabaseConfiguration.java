package au.com.cyberavenue.osb.resequencer;

import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.orm.jpa.JpaVendorAdapter;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import au.com.cyberavenue.osb.resequencer.batch.config.ApplicationProperties;
import au.com.cyberavenue.osb.resequencer.batch.config.BatchConfiguration;
import au.com.cyberavenue.osb.resequencer.batch.config.RepositorySeqRetryProcessorConfiguration;
import au.com.cyberavenue.osb.resequencer.service.RetryConfigService;
import au.com.cyberavenue.osb.resequencer.service.RetryConfigServiceImpl;
import liquibase.integration.spring.SpringLiquibase;

@Configuration
@Import({ RepositorySeqRetryProcessorConfiguration.class, BatchConfiguration.class })
@EnableTransactionManagement
@EnableConfigurationProperties(value = ApplicationProperties.class)
@TestPropertySource("classpath:application.yml")
public class TestDatabaseConfiguration {

    private static final Logger log = LoggerFactory.getLogger(TestDatabaseConfiguration.class);

    @Primary
    @Bean(name = "batchDataSource", destroyMethod = "")
    public DataSource batchDataSource() {
        DriverManagerDataSource ds = new DriverManagerDataSource();
        ds.setDriverClassName("org.h2.Driver");
        ds.setUrl("jdbc:h2:mem:retryprocessor;DB_CLOSE_DELAY=-1");
        ds.setUsername("retryprocessor");
        ds.setPassword("");
        return ds;
    }

    @Bean(name = "soaInfraDataSource")
    public DataSource soaInfraDataSource() {
        DriverManagerDataSource ds = new DriverManagerDataSource();
        ds.setDriverClassName("org.h2.Driver");
        ds.setUrl("jdbc:h2:mem:soainfra;DB_CLOSE_DELAY=-1");
        ds.setUsername("soainfra");
        ds.setPassword("");
        return ds;
    }

    @Bean
    public JpaVendorAdapter jpaVendorAdapter() {
        return new HibernateJpaVendorAdapter();
    }

    @Bean
    public SpringLiquibase springBatchLiquibase() {
        log.info("############## Liquibase initializing database 'seqRetryProcessor'");
        SpringLiquibase liquibase = new SpringLiquibase();
        liquibase.setDataSource(batchDataSource());
        liquibase.setChangeLog("classpath:/db/changelog/batch/seq-retry-processor-master.xml");
        return liquibase;
    }

    @Bean
    public SpringLiquibase soaInfraLiquibase() {
        log.info("############## Liquibase initializing database 'soaInfraDataSource'");
        SpringLiquibase liquibase = new SpringLiquibase();
        liquibase.setDataSource(soaInfraDataSource());
        liquibase.setChangeLog("classpath:/db/changelog/batch/soa-infra-master.xml");
        return liquibase;
    }

    @Bean
    public RetryConfigService retryConfigRepository() {
        return new RetryConfigServiceImpl();
    }
}

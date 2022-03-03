package au.com.cyberavenue.osb.resequencer.batch.config;

import java.util.Properties;

import javax.naming.Context;
import javax.naming.NamingException;
import javax.sql.DataSource;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.env.Environment;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jndi.JndiTemplate;
import org.springframework.orm.jpa.JpaVendorAdapter;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import liquibase.integration.spring.SpringLiquibase;
import net.javacrumbs.shedlock.core.LockProvider;
import net.javacrumbs.shedlock.provider.jdbctemplate.JdbcTemplateLockProvider;

@Configuration
@EnableTransactionManagement
public class DataSourceConfiguration {

    private final Environment env;

    public DataSourceConfiguration(Environment env) {
        this.env = env;
    }

    @Bean
    public JndiTemplate jndiTemplate() {
        Properties properties = new Properties();
        properties.put(Context.INITIAL_CONTEXT_FACTORY, "weblogic.jndi.WLInitialContextFactory");
        return new JndiTemplate(properties);
    }

    @Primary
    @Bean(name = "batchDataSource", destroyMethod = "")
    public DataSource batchDataSource() throws NamingException {
        return (DataSource) jndiTemplate().lookup(env.getProperty("datasources.retry-processor-datasource-jndi-name"));
    }

    @Bean
    public SpringLiquibase seqRetryProcessorLiquibase() throws NamingException {
        SpringLiquibase springLiquibase = new SpringLiquibase();
        springLiquibase.setChangeLog("classpath:/META-INF/liquibase/seq-retry-processor-changelog.xml");
        springLiquibase.setDataSource(batchDataSource());
        return springLiquibase;
    }

    @Bean
    public LockProvider lockProvider() throws NamingException {
        return new JdbcTemplateLockProvider(JdbcTemplateLockProvider.Configuration.builder()
                .withJdbcTemplate(new JdbcTemplate(batchDataSource())).usingDbTime().build());
    }

    @Bean(name = "soaInfraDataSource", destroyMethod = "")
    public DataSource soaInfraDataSource() throws NamingException {
        return (DataSource) jndiTemplate().lookup(env.getProperty("datasources.soa-infra-datasource-jndi-name"));
    }

    @Bean
    public JpaVendorAdapter jpaVendorAdapter() {
        return new HibernateJpaVendorAdapter();
    }

    @Bean
    @ConfigurationProperties(prefix = "spring.jpa.properties")
    public Properties jpaProperties() {
        return new Properties();
    }

}

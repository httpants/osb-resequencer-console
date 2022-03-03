package au.com.cyberavenue.osb.resequencer.batch.config;

import java.util.Properties;

import javax.naming.NamingException;
import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.JpaVendorAdapter;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
@EnableJpaRepositories(entityManagerFactoryRef = "seqRetryProcessorEntityManagerFactory",
    transactionManagerRef = "seqRetryProcessorTransactionManager", basePackageClasses = {
            au.com.cyberavenue.osb.resequencer.repository.seqretryprocessor.RetryMessageRepository.class })
public class RepositorySeqRetryProcessorConfiguration {

    @Autowired
    @Qualifier("batchDataSource")
    private DataSource batchDataSource;

    @Autowired
    private JpaVendorAdapter jpaVendorAdapter;

    @Bean
    public PlatformTransactionManager seqRetryProcessorTransactionManager() throws NamingException {
        JpaTransactionManager tm = new JpaTransactionManager();
        tm.setDataSource(batchDataSource);
        tm.setEntityManagerFactory(seqRetryProcessorEntityManagerFactory().getObject());
        return tm;
    }

    @Bean(name = "seqRetryProcessorEntityManagerFactory")
    public LocalContainerEntityManagerFactoryBean seqRetryProcessorEntityManagerFactory() throws NamingException {
        LocalContainerEntityManagerFactoryBean emfBean = new LocalContainerEntityManagerFactoryBean();
        emfBean.setDataSource(batchDataSource);
        emfBean.setPackagesToScan("au.com.cyberavenue.osb.resequencer.entity.seqretryprocessor");
        emfBean.setBeanName("seqRetryProcessorEntityManagerFactory");
        emfBean.setJpaVendorAdapter(jpaVendorAdapter);

        Properties jpaProps = new Properties();
        jpaProps.put("hibernate.hbm2ddl.auto", "none");
        jpaProps.put("hibernate.physical_naming_strategy",
                "org.springframework.boot.orm.jpa.hibernate.SpringPhysicalNamingStrategy");
//		jpaProps.put("hibernate.jdbc.fetch_size", "200");
//		jpaProps.put("hibernate.jdbc.batch_size", 100);
//		jpaProps.put("hibernate.order_inserts", "true");
//		jpaProps.put("hibernate.order_updates", "true");
        jpaProps.put("hibernate.show_sql", "true");
        jpaProps.put("hibernate.format_sql", "true");
        emfBean.setJpaProperties(jpaProps);
        return emfBean;
    }

}

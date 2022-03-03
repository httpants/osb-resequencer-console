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
@EnableJpaRepositories(entityManagerFactoryRef = "soaInfraEntityManagerFactory",
    transactionManagerRef = "soaInfraTransactionManager", basePackageClasses = {
            au.com.cyberavenue.osb.resequencer.repository.soainfra.OsbResequencerMessageRepository.class })
public class RepositorySoaInfraConfiguration {

    @Autowired
    @Qualifier("soaInfraDataSource")
    private DataSource soaInfraDataSource;

    @Autowired
    private JpaVendorAdapter jpaVendorAdapter;

    @Autowired
    private Properties jpaProperties;

    @Bean(name = "soaInfraTransactionManager")
    public PlatformTransactionManager soaInfraTransactionManager() throws NamingException {
        JpaTransactionManager tm = new JpaTransactionManager();
        tm.setDataSource(soaInfraDataSource);
        tm.setEntityManagerFactory(soaInfraEntityManagerFactory().getObject());
        return tm;
    }

    @Bean(name = "soaInfraEntityManagerFactory")
    public LocalContainerEntityManagerFactoryBean soaInfraEntityManagerFactory() throws NamingException {
        LocalContainerEntityManagerFactoryBean em = new LocalContainerEntityManagerFactoryBean();
        em.setDataSource(soaInfraDataSource);
        em.setPackagesToScan("au.com.cyberavenue.osb.resequencer.entity.soainfra");
        em.setBeanName("soaInfraEntityManagerFactory");
        em.setJpaVendorAdapter(jpaVendorAdapter);
        em.setJpaProperties(jpaProperties);
        return em;
    }

}

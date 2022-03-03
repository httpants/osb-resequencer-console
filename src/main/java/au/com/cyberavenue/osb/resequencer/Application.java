package au.com.cyberavenue.osb.resequencer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.core.env.Environment;

import au.com.cyberavenue.osb.resequencer.batch.config.ApplicationProperties;

@SpringBootApplication
@EnableConfigurationProperties({ ApplicationProperties.class })
public class Application {

    private static final Logger log = LoggerFactory.getLogger(Application.class);

    public static void main(String[] args) {
        SpringApplication app = new SpringApplication(Application.class);
        Environment env = app.run(args).getEnvironment();
        log.debug(":::: Running '{}'", env.getProperty("spring.application.name"));
    }

}

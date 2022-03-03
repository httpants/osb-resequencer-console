package au.com.cyberavenue.osb.resequencer.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import au.com.cyberavenue.osb.resequencer.batch.config.ApplicationProperties;
import au.com.cyberavenue.osb.resequencer.batch.config.ApplicationProperties.Defaults;
import au.com.cyberavenue.osb.resequencer.entity.seqretryprocessor.GlobalConfig;
import au.com.cyberavenue.osb.resequencer.entity.seqretryprocessor.OperationRetryConfig;
import au.com.cyberavenue.osb.resequencer.repository.seqretryprocessor.GlobalConfigRepository;
import au.com.cyberavenue.osb.resequencer.repository.seqretryprocessor.OsbRetryConfigRepository;

@Component
@Transactional(transactionManager = "seqRetryProcessorTransactionManager", propagation = Propagation.REQUIRES_NEW)
public class RetryConfigServiceImpl implements RetryConfigService {

    @Autowired
    private ApplicationProperties applicationProperties;

    @Autowired
    private OsbRetryConfigRepository osbRetryConfigRepository;

    @Autowired
    private GlobalConfigRepository globalConfigRepository;

    @Override
    public OperationRetryConfig getOperationConfig(String service, String operation) {

        return osbRetryConfigRepository.findByComponentDnAndOperation(service, operation)
                .map(rc -> new OperationRetryConfig(
                        rc.getRetryLimit(),
                        rc.getDelay(),
                        rc.getDelayFactor()))
                .orElse(getGlobalOperationConfig());
    }

    private OperationRetryConfig getGlobalOperationConfig() {
        GlobalConfig gc = getGlobalConfig();
        return new OperationRetryConfig(gc.getRetryLimit(), gc.getDelay(), gc.getDelayFactor());
    }

    @Override
    public GlobalConfig getGlobalConfig() {
        GlobalConfig gc = globalConfigRepository.getGlobalConfig().orElse(null);
        if (gc == null) {
            Defaults defaults = applicationProperties.getDefaults();
            gc = new GlobalConfig();
            gc.setRetriesEnabled(defaults.isRetriesEnabled());
            gc.setRetryLimit(defaults.getRetries());
            gc.setDelay(defaults.getDelay());
            gc.setDelayFactor(defaults.getDelayFactor());
            globalConfigRepository.save(gc);
        }
        return gc;
    }

    @Override
    public GlobalConfig save(GlobalConfig gc) {
        return globalConfigRepository.save(gc);
    }

}

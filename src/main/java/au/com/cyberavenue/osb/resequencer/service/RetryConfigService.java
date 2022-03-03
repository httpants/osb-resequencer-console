package au.com.cyberavenue.osb.resequencer.service;

import au.com.cyberavenue.osb.resequencer.entity.seqretryprocessor.GlobalConfig;
import au.com.cyberavenue.osb.resequencer.entity.seqretryprocessor.OperationRetryConfig;

public interface RetryConfigService {

    GlobalConfig getGlobalConfig();

    OperationRetryConfig getOperationConfig(String component, String operation);

    GlobalConfig save(GlobalConfig gc);

}

package au.com.cyberavenue.osb.resequencer.web;

import static java.util.stream.Collectors.*;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.view.RedirectView;

import au.com.cyberavenue.osb.resequencer.batch.config.ApplicationProperties;
import au.com.cyberavenue.osb.resequencer.entity.seqretryprocessor.ComponentOperationId;
import au.com.cyberavenue.osb.resequencer.entity.seqretryprocessor.GlobalConfig;
import au.com.cyberavenue.osb.resequencer.entity.seqretryprocessor.OsbRetryConfig;
import au.com.cyberavenue.osb.resequencer.repository.seqretryprocessor.OsbRetryConfigRepository;
import au.com.cyberavenue.osb.resequencer.repository.soainfra.OsbGroupStatusRepository;
import au.com.cyberavenue.osb.resequencer.service.RetryConfigService;

@Controller
public class SettingsController {

    @Autowired
    private ApplicationProperties applicationProperties;

    @Autowired
    private OsbGroupStatusRepository osbGroupStatusRepository;

    @Autowired
    private OsbRetryConfigRepository osbRetryConfigRepository;

    @Autowired
    private RetryConfigService retryConfigService;

    @GetMapping("/settings")
    public ModelAndView getSettings() {

        Map<ComponentOperationId, OsbRetryConfig> osbRetryConfigs = osbRetryConfigRepository.findAll()
                .stream()
                .collect(toMap(OsbRetryConfig::getId, Function.identity()));

        osbGroupStatusRepository.findDistinctComponentOperations()
                .forEach((componentOperationId) -> {
                    if (!osbRetryConfigs.containsKey(componentOperationId)) {
                        OsbRetryConfig retryConfig = new OsbRetryConfig();
                        retryConfig.setId(componentOperationId);
                        osbRetryConfigs.put(componentOperationId, retryConfig);
                    }
                });

        List<OsbRetryConfig> componentOperationRetryConfig = osbRetryConfigs.values()
                .stream()
                .sorted((a, b) -> a.getId().compareTo(b.getId()))
                .collect(toList());

        GlobalConfig globalConfig = retryConfigService.getGlobalConfig();

        ModelAndView mav = new ModelAndView("settings");
        mav.addObject("componentRetryConfig", componentOperationRetryConfig);
        mav.addObject("globalConfig", globalConfig);

        return mav;
    }

    @PostMapping(value = "/settings", params = "action=saveComponentRetryConfig")
    public RedirectView saveComponentRetryConfig(
            @RequestParam(name = "componentDn") String componentDn,
            @RequestParam(name = "operation") String operation,
            @RequestParam(name = "retries") Integer retries,
            @RequestParam(name = "delay") Integer delay,
            @RequestParam(name = "delayFactor") Integer delayFactor,
            RedirectAttributes redirectAttributes)
            throws IOException {

        ComponentOperationId componentOperationId = new ComponentOperationId(componentDn, operation);
        OsbRetryConfig osbRetryConfig = new OsbRetryConfig();
        osbRetryConfig.setId(componentOperationId);
        osbRetryConfig.setRetryLimit(retries);
        osbRetryConfig.setDelay(delay);
        osbRetryConfig.setDelayFactor(delayFactor);

        osbRetryConfigRepository.save(osbRetryConfig);

        return new RedirectView("/settings", true);
    }

    @PostMapping(value = "/settings", params = "action=clearComponentRetryConfig")
    public RedirectView clearComponentRetryConfig(
            @RequestParam(name = "componentDn") String componentDn,
            @RequestParam(name = "operation") String operation)
            throws IOException {

        ComponentOperationId componentOperationId = new ComponentOperationId(componentDn, operation);
        osbRetryConfigRepository.deleteById(componentOperationId);

        return new RedirectView("/settings", true);
    }

    @PostMapping(value = "/settings", params = "action=saveGlobalConfig")
    public RedirectView saveGlobalConfig(
            @RequestParam(name = "retriesEnabled", required = false, defaultValue = "false") Boolean retriesEnabled,
            @RequestParam(name = "retries") Integer retries,
            @RequestParam(name = "delay") Integer delay,
            @RequestParam(name = "delayFactor") Integer delayFactor,
            RedirectAttributes redirectAttributes)
            throws IOException {

        GlobalConfig gc = retryConfigService.getGlobalConfig();
        gc.setRetriesEnabled(retriesEnabled);
        gc.setRetryLimit(retries);
        gc.setDelay(delay);
        gc.setDelayFactor(delayFactor);
        retryConfigService.save(gc);

        return new RedirectView("/settings", true);
    }

}

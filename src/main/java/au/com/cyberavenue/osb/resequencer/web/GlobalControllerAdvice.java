package au.com.cyberavenue.osb.resequencer.web;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import au.com.cyberavenue.osb.resequencer.batch.config.ApplicationProperties;

@ControllerAdvice
public class GlobalControllerAdvice {

    @Autowired
    private ApplicationProperties applicationProperties;

    @ModelAttribute("applicationProperties")
    public ApplicationProperties applicationProperties() {
        return applicationProperties;
    }
}

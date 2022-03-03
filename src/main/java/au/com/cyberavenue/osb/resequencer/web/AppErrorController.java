package au.com.cyberavenue.osb.resequencer.web;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.error.ErrorAttributeOptions;
import org.springframework.boot.web.servlet.error.ErrorAttributes;
import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.context.request.ServletWebRequest;

@Controller
public class AppErrorController implements ErrorController {

    @Autowired
    private ErrorAttributes errorAttributes;

    @RequestMapping("/error")
    public String getErrorPage(HttpServletRequest request, Model model) {
        model.addAllAttributes(getErrorAttributes(
                request,
                ErrorAttributeOptions.defaults()));
        return "error-detail";
    }

    private Map<String, Object> getErrorAttributes(HttpServletRequest request,
            ErrorAttributeOptions options) {
        return errorAttributes.getErrorAttributes(new ServletWebRequest(request),
                options);
    }

    @Override
    public String getErrorPath() {
        // TODO Auto-generated method stub
        return "/error";
    }

}

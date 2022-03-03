package au.com.cyberavenue.osb.resequencer.web;

import javax.servlet.http.HttpServletRequest;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class LoginController {

    @RequestMapping("/login")
    public String getLoginForm(HttpServletRequest request, Model model) {
        return "login";
    }

    @GetMapping("/bye")
    public String logoutSuccess() {
        return "logout";
    }

}

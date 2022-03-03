package au.com.cyberavenue.osb.resequencer.web;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.lang3.StringUtils;
import org.springframework.util.CollectionUtils;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.condition.PatternsRequestCondition;
import org.springframework.web.servlet.support.RequestContext;
import org.springframework.web.servlet.view.RedirectView;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;
import org.springframework.web.util.UriTemplate;
import org.springframework.web.util.UrlPathHelper;

public class SessionNavigationHandlerInterceptor implements HandlerInterceptor {

    private final UrlPathHelper pathHelper = UrlPathHelper.defaultInstance;

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler,
            ModelAndView modelAndView) throws Exception {
        bindNavigationLink(request, "/groups", modelAndView, "groupsHref");
        bindNavigationLink(request, "/groups/{groupId}", modelAndView, "groupDetailHref");
        bindNavigationLink(request, "/messages", modelAndView, "messagesHref");
        bindNavigationLink(request, "/dashboard", modelAndView, "dashboardHref");
    }

    private void bindNavigationLink(HttpServletRequest request, String relativeUrl, ModelAndView modelAndView,
            String attributeName) {

        HttpSession session = request.getSession();
        RequestContext rc = new RequestContext(request);
        String requestUrl = rc.getRequestUri();
        String queryString = rc.getQueryString();
        if (StringUtils.isNotBlank(queryString)) {
            requestUrl += StringUtils.join("?", queryString);
        }

        String sessionUri = null;
        if (matches(request, relativeUrl)) {
            sessionUri = requestUrl;
        } else {
            UriTemplate template = new UriTemplate(relativeUrl);
            if (CollectionUtils.isEmpty(template.getVariableNames())) {
                sessionUri = (String) session.getAttribute(attributeName);
                if (sessionUri == null) {
                    sessionUri = rc.getContextUrl(relativeUrl);
                }
            } else {
                String requestPath = pathHelper.getLookupPathForRequest(request);
                Map<String, String> pathVariables = template.match(requestPath);
                if (!CollectionUtils.isEmpty(pathVariables)) {
                    String matchingUri = rc.getContextUrl(template.expand(pathVariables).toString());
                    sessionUri = (String) session.getAttribute(attributeName);
                    if (sessionUri != null) {
                        UriComponents matchingUriComponents = UriComponentsBuilder.fromUriString(matchingUri).build();
                        UriComponents sessionUriComponents = UriComponentsBuilder.fromUriString(sessionUri).build();
                        if (!StringUtils.equals(sessionUriComponents.getPath(), matchingUriComponents.getPath())) {
                            sessionUri = matchingUri;
                        }
                    } else {
                        sessionUri = matchingUri;
                    }
                }
            }
        }

        if (sessionUri != null) {
            session.setAttribute(attributeName, sessionUri);
            if (modelAndView != null && !(modelAndView.getView() instanceof RedirectView)) {
                if (!StringUtils.startsWith(modelAndView.getViewName(), "redirect")) {
                    modelAndView.addObject(attributeName, sessionUri);
                }
            }
        }
    }

    public boolean matches(HttpServletRequest request, String relativeUrl) {
        PatternsRequestCondition prc = new PatternsRequestCondition(relativeUrl).getMatchingCondition(request);
        return prc != null;
    }

}

package au.com.cyberavenue.osb.resequencer.batch.config;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.ObjectFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ResourceLoaderAware;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.ImportSelector;
import org.springframework.core.MethodParameter;
import org.springframework.core.convert.ConversionService;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.io.support.SpringFactoriesLoader;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.querydsl.QuerydslUtils;
import org.springframework.data.web.HateoasPageableHandlerMethodArgumentResolver;
import org.springframework.data.web.HateoasSortHandlerMethodArgumentResolver;
import org.springframework.data.web.config.HateoasAwareSpringDataWebConfiguration;
import org.springframework.data.web.config.ProjectingArgumentResolverRegistrar;
import org.springframework.data.web.config.QuerydslWebConfiguration;
import org.springframework.data.web.config.SpringDataJacksonModules;
import org.springframework.lang.Nullable;
import org.springframework.util.ClassUtils;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.method.support.ModelAndViewContainer;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.util.UrlPathHelper;
import org.springframework.web.util.WebUtils;

import au.com.cyberavenue.osb.resequencer.web.PagedMessageAssembler;
import au.com.cyberavenue.osb.resequencer.web.PagedMessageGroupReportAssembler;
import au.com.cyberavenue.osb.resequencer.web.SessionNavigationHandlerInterceptor;

@Configuration
@Import({ WebConfiguration.WebConfigurationImportSelector.class })
public class WebConfiguration extends HateoasAwareSpringDataWebConfiguration {

    public WebConfiguration(ApplicationContext context, ObjectFactory<ConversionService> conversionService) {
        super(context, conversionService);
    }

    @Bean
    @Override
    public CookieValuePageableHandlerMethodArgumentResolver pageableResolver() {

        CookieValuePageableHandlerMethodArgumentResolver pageableResolver = new CookieValuePageableHandlerMethodArgumentResolver(
                sortResolver());
        customizePageableResolver(pageableResolver);
        return pageableResolver;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new SessionNavigationHandlerInterceptor());
    }

    @Bean
    public PagedMessageGroupReportAssembler pagedMessageGroupReportAssembler() {
        return new PagedMessageGroupReportAssembler(pageableResolver(), null);
    }

    @Bean
    public PagedMessageAssembler pagedMessageAssembler() {
        return new PagedMessageAssembler(pageableResolver(), null);
    }

    static class CookieValuePageableHandlerMethodArgumentResolver extends HateoasPageableHandlerMethodArgumentResolver {

        private static final HateoasSortHandlerMethodArgumentResolver DEFAULT_SORT_RESOLVER = new HateoasSortHandlerMethodArgumentResolver();

        private HateoasSortHandlerMethodArgumentResolver sortResolver;

        private UrlPathHelper urlPathHelper = UrlPathHelper.defaultInstance;

        public CookieValuePageableHandlerMethodArgumentResolver() {
            this(null);
        }

        public CookieValuePageableHandlerMethodArgumentResolver(HateoasSortHandlerMethodArgumentResolver sortResolver) {
            super(getDefaultedSortResolver(sortResolver));
            this.sortResolver = getDefaultedSortResolver(sortResolver);
        }

        public void setUrlPathHelper(UrlPathHelper urlPathHelper) {
            this.urlPathHelper = urlPathHelper;
        }

        @Override
        public Pageable resolveArgument(MethodParameter methodParameter, ModelAndViewContainer mavContainer,
                NativeWebRequest webRequest, WebDataBinderFactory binderFactory) {

            String sizeParameterName = getParameterNameToUse(getSizeParameterName(), methodParameter);
            String pageSize = StringUtils.defaultIfBlank(
                    webRequest.getParameter(sizeParameterName),
                    getCookieValue(webRequest, sizeParameterName));

            String page = webRequest.getParameter(getParameterNameToUse(getPageParameterName(), methodParameter));
            Sort sort = sortResolver.resolveArgument(methodParameter, mavContainer, webRequest, binderFactory);
            Pageable pageable = getPageable(methodParameter, page, pageSize);

            webRequest.setAttribute(sizeParameterName, pageable.getPageSize(), RequestAttributes.SCOPE_REQUEST);

            if (sort.isSorted()) {
                return PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), sort);
            }

            return pageable;
        }

        private String getCookieValue(NativeWebRequest webRequest, String parameterName) {
            HttpServletRequest servletRequest = webRequest.getNativeRequest(HttpServletRequest.class);
            if (servletRequest != null) {
                Cookie cookieValue = WebUtils.getCookie(servletRequest, parameterName);
                if (cookieValue != null) {
                    return this.urlPathHelper.decodeRequestString(servletRequest, cookieValue.getValue());
                }
            }
            return null;
        }

        private static HateoasSortHandlerMethodArgumentResolver getDefaultedSortResolver(
                @Nullable HateoasSortHandlerMethodArgumentResolver sortResolver) {
            return sortResolver == null ? DEFAULT_SORT_RESOLVER : sortResolver;
        }

    }

    static class WebConfigurationImportSelector implements ImportSelector, ResourceLoaderAware {

        private Optional<ClassLoader> resourceLoader = Optional.empty();

        @Override
        public void setResourceLoader(ResourceLoader resourceLoader) {
            this.resourceLoader = Optional.of(resourceLoader).map(ResourceLoader::getClassLoader);
        }

        @Override
        public String[] selectImports(AnnotationMetadata importingClassMetadata) {

            List<String> imports = new ArrayList<>();

            imports.add(ProjectingArgumentResolverRegistrar.class.getName());

            resourceLoader//
                    .filter(it -> ClassUtils.isPresent("com.fasterxml.jackson.databind.ObjectMapper", it))//
                    .map(it -> SpringFactoriesLoader.loadFactoryNames(SpringDataJacksonModules.class, it))//
                    .ifPresent(it -> imports.addAll(it));

            if (QuerydslUtils.QUERY_DSL_PRESENT) {
                imports.add(QuerydslWebConfiguration.class.getName());
            }

            return imports.toArray(new String[imports.size()]);
        }
    }

}

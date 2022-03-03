package au.com.cyberavenue.osb.resequencer.batch.config;

import java.util.Arrays;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.authority.mapping.Attributes2GrantedAuthoritiesMapper;
import org.springframework.security.core.authority.mapping.MappableAttributesRetriever;
import org.springframework.security.core.authority.mapping.SimpleAttributes2GrantedAuthoritiesMapper;
import org.springframework.security.core.userdetails.AuthenticationUserDetailsService;
import org.springframework.security.web.authentication.preauth.AbstractPreAuthenticatedProcessingFilter;
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationProvider;
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationToken;
import org.springframework.security.web.authentication.preauth.PreAuthenticatedGrantedAuthoritiesUserDetailsService;
import org.springframework.security.web.authentication.preauth.j2ee.J2eeBasedPreAuthenticatedWebAuthenticationDetailsSource;
import org.springframework.security.web.authentication.preauth.j2ee.J2eePreAuthenticatedProcessingFilter;
import org.springframework.security.web.authentication.preauth.j2ee.WebXmlMappableAttributesRetriever;

@Configuration
@EnableWebSecurity
public class SecurityConfiguration extends WebSecurityConfigurerAdapter {

    @Bean
    public MappableAttributesRetriever webXmlRolesParser() {
        return new WebXmlMappableAttributesRetriever();
    }

    @Bean
    public Attributes2GrantedAuthoritiesMapper roles2GrantedAuthoritiesMapper() {
        SimpleAttributes2GrantedAuthoritiesMapper var = new SimpleAttributes2GrantedAuthoritiesMapper();
        var.setAttributePrefix("");
        return var;
    }

    @Bean
    public J2eeBasedPreAuthenticatedWebAuthenticationDetailsSource authenticationDetailsSource() {
        J2eeBasedPreAuthenticatedWebAuthenticationDetailsSource var = new J2eeBasedPreAuthenticatedWebAuthenticationDetailsSource();
        var.setMappableRolesRetriever(webXmlRolesParser());
        var.setUserRoles2GrantedAuthoritiesMapper(roles2GrantedAuthoritiesMapper());
        return var;
    }

    @Bean
    public AuthenticationUserDetailsService<PreAuthenticatedAuthenticationToken> getUserDetailsService() {
        return new PreAuthenticatedGrantedAuthoritiesUserDetailsService();
    }

    @Bean
    public AuthenticationProvider preAuthenticatedAuthenticationProvider() {
        PreAuthenticatedAuthenticationProvider var = new PreAuthenticatedAuthenticationProvider();
        var.setPreAuthenticatedUserDetailsService(getUserDetailsService());
        return var;
    }

    @Override
    @Bean
    public AuthenticationManager authenticationManager() {
        return new ProviderManager(Arrays.asList(preAuthenticatedAuthenticationProvider()));
    }

    @Bean
    public J2eePreAuthenticatedProcessingFilter j2eePreAuthFilter() {
        J2eePreAuthenticatedProcessingFilter var = new J2eePreAuthenticatedProcessingFilter();
        var.setAuthenticationDetailsSource(authenticationDetailsSource());
        var.setAuthenticationManager(authenticationManager());
        return var;
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.addFilterAfter(j2eePreAuthFilter(), AbstractPreAuthenticatedProcessingFilter.class)
                .authorizeRequests()
                .anyRequest()
                .permitAll()
                .and()
                .logout().logoutSuccessUrl("/bye")
                .and()
                .csrf().disable();
    }

    @Override
    public void configure(WebSecurity web) throws Exception {
        web.ignoring().antMatchers("/webjars/**", "/login");
        super.configure(web);
    }

}

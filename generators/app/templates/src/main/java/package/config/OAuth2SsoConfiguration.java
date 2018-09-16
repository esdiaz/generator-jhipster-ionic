<%#
    Copyright 2013-2018 the original author or authors from the JHipster project.

    This file is part of the JHipster project, see http://www.jhipster.tech/
    for more information.

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
-%>
package <%=packageName%>.config;

import <%=packageName%>.security.AuthoritiesConstants;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.security.oauth2.client.EnableOAuth2Sso;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.oauth2.client.OAuth2ClientContext;
import org.springframework.security.oauth2.client.OAuth2RestTemplate;
import org.springframework.security.oauth2.client.resource.OAuth2ProtectedResourceDetails;
import org.springframework.security.web.csrf.CsrfFilter;
import org.springframework.security.web.util.matcher.NegatedRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;
import org.springframework.web.filter.CorsFilter;

import io.github.jhipster.security.AjaxLogoutSuccessHandler;

@EnableOAuth2Sso
@Configuration
public class OAuth2SsoConfiguration extends WebSecurityConfigurerAdapter {

    private final RequestMatcher authorizationHeaderRequestMatcher;

    private final CorsFilter corsFilter;

    public OAuth2SsoConfiguration(@Qualifier("authorizationHeaderRequestMatcher")
                                  RequestMatcher authorizationHeaderRequestMatcher, CorsFilter corsFilter) {
        this.authorizationHeaderRequestMatcher = authorizationHeaderRequestMatcher;
        this.corsFilter = corsFilter;
    }

    @Bean
    public AjaxLogoutSuccessHandler ajaxLogoutSuccessHandler() {
        return new AjaxLogoutSuccessHandler();
    }

    @Override
    public void configure(WebSecurity web) throws Exception {
        web.ignoring()
            .antMatchers(HttpMethod.OPTIONS, "/**")
            .antMatchers("/app/**/*.{js,html}")
            .antMatchers("/i18n/**")
            .antMatchers("/content/**")
            .antMatchers("/swagger-ui/index.html")
            .antMatchers("/test/**")
            .antMatchers("/h2-console/**");
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
            .csrf()
            .disable()
            .addFilterBefore(corsFilter, CsrfFilter.class)
            .headers()
            .frameOptions()
            .disable()
        .and()
            .logout()
            .logoutUrl("/api/logout")
            .logoutSuccessHandler(ajaxLogoutSuccessHandler())
        .and()
            .requestMatcher(new NegatedRequestMatcher(authorizationHeaderRequestMatcher))
            .authorizeRequests()
            .antMatchers("/api/auth-info").permitAll()
            .antMatchers("/api/**").authenticated()
            .antMatchers("/management/health").permitAll()
            .antMatchers("/management/**").hasAuthority(AuthoritiesConstants.ADMIN)
            .anyRequest().permitAll();
    }

    /**
     * This OAuth2RestTemplate is used by org.springframework.cloud.security.oauth2.proxy.OAuth2TokenRelayFilter
     * from Spring Cloud Security to refresh the access token when needed.
     */
    @Bean
    public OAuth2RestTemplate oAuth2RestTemplate(OAuth2ProtectedResourceDetails oAuth2ProtectedResourceDetails,
                                                 OAuth2ClientContext oAuth2ClientContext) {
        return new OAuth2RestTemplate(oAuth2ProtectedResourceDetails, oAuth2ClientContext);
    }
}

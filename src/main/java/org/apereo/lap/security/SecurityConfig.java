/*******************************************************************************
 * Copyright (c) 2015 Unicon (R) Licensed under the
 * Educational Community License, Version 2.0 (the "License"); you may
 * not use this file except in compliance with the License. You may
 * obtain a copy of the License at
 *
 * http://www.osedu.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an "AS IS"
 * BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing
 * permissions and limitations under the License.
 *******************************************************************************/
/**
 * 
 */
package org.apereo.lap.security;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.annotation.Order;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.oauth2.config.annotation.configurers.ClientDetailsServiceConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configuration.AuthorizationServerConfigurerAdapter;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableAuthorizationServer;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableResourceServer;
import org.springframework.security.oauth2.config.annotation.web.configuration.ResourceServerConfigurerAdapter;
import org.springframework.security.oauth2.config.annotation.web.configurers.AuthorizationServerSecurityConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configurers.ResourceServerSecurityConfigurer;
import org.springframework.security.web.authentication.SavedRequestAwareAuthenticationSuccessHandler;
import org.springframework.security.web.authentication.www.BasicAuthenticationEntryPoint;
import org.springframework.security.web.csrf.CsrfFilter;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.security.web.csrf.CsrfTokenRepository;
import org.springframework.security.web.csrf.HttpSessionCsrfTokenRepository;
import org.springframework.security.web.savedrequest.DefaultSavedRequest;
import org.springframework.security.web.savedrequest.HttpSessionRequestCache;
import org.springframework.security.web.savedrequest.RequestCache;
import org.springframework.security.web.session.SessionManagementFilter;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.WebUtils;

/**
 * @author ggilbert
 *
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

  private Logger log = Logger.getLogger(SecurityConfig.class);

  @Bean
  public RequestCache requestCache() {
    return new HttpSessionRequestCache();
  }
    
  @Configuration
  public static class HttpBasicConfigurationAdapter extends WebSecurityConfigurerAdapter {
    
    @Override
    public void configure(WebSecurity web) throws Exception {
        web.ignoring()
          .antMatchers("/assets/**", "/favicon.ico");
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
      http
      .httpBasic()
        .authenticationEntryPoint(new NoWWWAuthenticate401ResponseEntryPoint("lap"))
      .and()
      .authorizeRequests()
        .antMatchers("/features/**", "/", "/login", "/user").permitAll()
        .antMatchers("/admin/**","/history/**","/pipelines/**").authenticated()
      .and()
        .logout()
        .invalidateHttpSession(true)
        .deleteCookies("X-LAP-TENANT")
      .and().csrf().csrfTokenRepository(csrfTokenRepository())
      /**
       * 
       * TODO revisit after updating to Spring Security 4.1 
       * Currently the SessionManagementFilter is added here instead of the CsrfFilter 
       * Two session tokens are generated, one token is created before login and one token is created after.
       * The Csrf doesn't update with the second token. Logout does not work as a side effect.
       * Replacing the CsrfFilter with the SessionManagmenentFilter is the current fix.
       * @link https://github.com/dsyer/spring-security-angular/issues/15
       * 
       * .and().addFilterAfter(csrfHeaderFilter(), CsrfFilter.class);
       * */
      .and().addFilterAfter(csrfHeaderFilter(), SessionManagementFilter.class);
    }
    
    @Override
    public void configure(AuthenticationManagerBuilder auth) throws Exception {
      auth
        .inMemoryAuthentication()
          .withUser("admin").password("admin").roles("ADMIN");
    }
    
    @Primary
    @Bean
    public AuthenticationManager authManager() throws Exception {
      return super.authenticationManagerBean();
    }
    
    private Filter csrfHeaderFilter() {
      return new OncePerRequestFilter() {      
        @Override
        protected void doFilterInternal(HttpServletRequest request,
            HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
          CsrfToken csrf = (CsrfToken) request.getAttribute(CsrfToken.class.getName());
          
          if (csrf != null) {
            Cookie cookie = WebUtils.getCookie(request, "XSRF-TOKEN");
            String token = csrf.getToken();
            if (cookie == null || token != null
                && !token.equals(cookie.getValue())) {
              cookie = new Cookie("XSRF-TOKEN", token);
              cookie.setPath("/");
              response.addCookie(cookie);
            }
          }
          filterChain.doFilter(request, response);
        }
      };
    }

    private CsrfTokenRepository csrfTokenRepository() {
      HttpSessionCsrfTokenRepository repository = new HttpSessionCsrfTokenRepository();
      repository.setHeaderName("X-XSRF-TOKEN");
      return repository;
    }
    
    class NoWWWAuthenticate401ResponseEntryPoint extends BasicAuthenticationEntryPoint {
      
      public NoWWWAuthenticate401ResponseEntryPoint(String realm) {
        setRealmName(realm);
      }
      
      @Override
      public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException)
          throws IOException, ServletException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.sendRedirect("/login");
      }
    }

  }
  
  @Order(1)
  @Configuration
  @EnableResourceServer
  protected static class ResourceServer extends ResourceServerConfigurerAdapter {

    @Override
    public void configure(HttpSecurity http) throws Exception {
      // @formatter:off
      http
        .requestMatchers().antMatchers("/api/**")
      .and()
        .authorizeRequests()
          .anyRequest().access("#oauth2.hasScope('read')");
      // @formatter:on
    }

    @Override
    public void configure(ResourceServerSecurityConfigurer resources) throws Exception {
      resources.resourceId("api");
    }

  }
  
  @Configuration
  @EnableAuthorizationServer
  protected static class OAuth2Config extends AuthorizationServerConfigurerAdapter {

    @Override
    public void configure(AuthorizationServerSecurityConfigurer oauthServer) throws Exception {
      oauthServer.checkTokenAccess("hasRole('ROLE_TRUSTED_CLIENT')");
    }

    @Override
    public void configure(ClientDetailsServiceConfigurer clients) throws Exception {
      // @formatter:off
      clients.inMemory()
            .withClient("clientid")
                .authorizedGrantTypes("client_credentials")
                .authorities("ROLE_TRUSTED_CLIENT")
                .scopes("read")
                .resourceIds("api")
                .secret("secret");
    // @formatter:on
    }
  }
  
  
  class RequestAwareAuthenticationHander extends
      SavedRequestAwareAuthenticationSuccessHandler {

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
        HttpServletResponse response, Authentication authentication)
        throws ServletException, IOException {
      DefaultSavedRequest defaultSavedRequest = (DefaultSavedRequest) request
          .getSession().getAttribute("SPRING_SECURITY_SAVED_REQUEST_KEY");

      if (defaultSavedRequest != null) {
        log.debug("saved url: " + defaultSavedRequest.getRedirectUrl());
        getRedirectStrategy().sendRedirect(request, response,
            defaultSavedRequest.getRedirectUrl());
      } else {
        super.onAuthenticationSuccess(request, response, authentication);
      }
    }

  }

}

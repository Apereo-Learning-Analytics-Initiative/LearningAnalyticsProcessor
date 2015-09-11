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
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.SavedRequestAwareAuthenticationSuccessHandler;
import org.springframework.security.web.authentication.www.BasicAuthenticationEntryPoint;
import org.springframework.security.web.csrf.CsrfFilter;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.security.web.csrf.CsrfTokenRepository;
import org.springframework.security.web.csrf.HttpSessionCsrfTokenRepository;
import org.springframework.security.web.savedrequest.DefaultSavedRequest;
import org.springframework.security.web.savedrequest.HttpSessionRequestCache;
import org.springframework.security.web.savedrequest.RequestCache;
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
        .antMatchers("/features/**", "/", "/login").permitAll()
        .anyRequest().authenticated()
      .and().csrf().csrfTokenRepository(csrfTokenRepository())
      .and().addFilterAfter(csrfHeaderFilter(), CsrfFilter.class);
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

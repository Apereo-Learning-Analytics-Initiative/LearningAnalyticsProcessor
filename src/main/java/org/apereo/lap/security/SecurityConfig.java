/**
 * 
 */
package org.apereo.lap.security;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.SavedRequestAwareAuthenticationSuccessHandler;
import org.springframework.security.web.savedrequest.DefaultSavedRequest;
import org.springframework.security.web.savedrequest.HttpSessionRequestCache;
import org.springframework.security.web.savedrequest.RequestCache;

/**
 * @author ggilbert
 *
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig extends WebSecurityConfigurerAdapter {

  private Logger log = Logger.getLogger(SecurityConfig.class);

  @Autowired
  LapWebAuthenticationDetailsSource customWebAuthenticationDetailsSource;

  @Bean
  public RequestCache requestCache() {
    return new HttpSessionRequestCache();
  }

  @Bean
  public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder(11);
  }

  @Override
  protected void configure(HttpSecurity http) throws Exception {
    http
    // TODO
    // https://spring.io/blog/2015/01/12/the-login-page-angular-js-and-spring-security-part-ii
      .csrf()
        .disable()
       .formLogin()
           .loginPage("/login")
           .authenticationDetailsSource(customWebAuthenticationDetailsSource)
           .permitAll()
       .and()
         .logout()
           .permitAll()
       .and()
         .authorizeRequests()
           .antMatchers("/api/**")
             .permitAll()
                     .anyRequest()
          .authenticated();
  }

  @Autowired
    public void configureGlobal(AuthenticationManagerBuilder auth) throws Exception {
    auth
    .inMemoryAuthentication()
        .withUser("user").password("password").roles("USER");
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

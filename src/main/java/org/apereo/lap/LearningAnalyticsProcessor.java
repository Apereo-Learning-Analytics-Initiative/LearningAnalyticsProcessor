/**
 * 
 */
package org.apereo.lap;

import java.security.Principal;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.web.config.EnableSpringDataWebSupport;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author ggilbert
 *
 */
@ComponentScan("org.apereo.lap")
@Configuration
@EnableAutoConfiguration
@EnableSpringDataWebSupport
public class LearningAnalyticsProcessor {
  final static Logger log = LoggerFactory.getLogger(LearningAnalyticsProcessor.class);
  
  public static void main(String[] args) {
      SpringApplication.run(LearningAnalyticsProcessor.class, args);
  }

  @Controller
  public static class LAPController {
    @RequestMapping(value = { "/", "/*"}, method = RequestMethod.GET)
    public String defaultHandler() {
      return "index";
    }
  }
  
  @RestController
  public static class AuthenticatedCheckController {
    @RequestMapping("/user")
    public Principal user(Principal user) {
      return user;
    }
  }

}

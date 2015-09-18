/**
 * 
 */
package org.apereo.lap;

import java.security.Principal;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.web.ErrorAttributes;
import org.springframework.boot.autoconfigure.web.ErrorController;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.web.config.EnableSpringDataWebSupport;
import org.springframework.security.access.annotation.Secured;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.ServletRequestAttributes;

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
    @Secured({ "ROLE_ADMIN"})
    @RequestMapping(value = { "/admin/**" }, method = RequestMethod.GET)
    public String secureRoutes() {
      return "index";
    }
    
    @RequestMapping(value = { "/", "/login" }, method = RequestMethod.GET)
    public String openRoutes() {
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
  
  @RestController
  public static class CustomErrorController implements ErrorController {

    private static final String PATH = "/error";

    @Autowired private ErrorAttributes errorAttributes;

    @RequestMapping(value = PATH)
    ErrorJson error(HttpServletRequest request, HttpServletResponse response) {
        // Appropriate HTTP response code (e.g. 404 or 500) is automatically set by Spring. 
        // Here we just define response body.
        return new ErrorJson(response.getStatus(), getErrorAttributes(request, false));
    }

    @Override
    public String getErrorPath() {
        return PATH;
    }

    private Map<String, Object> getErrorAttributes(HttpServletRequest request, boolean includeStackTrace) {
        RequestAttributes requestAttributes = new ServletRequestAttributes(request);
        return errorAttributes.getErrorAttributes(requestAttributes, includeStackTrace);
    }
    
    class ErrorJson {

      public Integer status;
      public String error;
      public String message;
      public String timeStamp;
      public String trace;

      public ErrorJson(int status, Map<String, Object> errorAttributes) {
          this.status = status;
          this.error = (String) errorAttributes.get("error");
          this.message = (String) errorAttributes.get("message");
          this.timeStamp = errorAttributes.get("timestamp").toString();
          this.trace = (String) errorAttributes.get("trace");
      }
    }
    
  }

}

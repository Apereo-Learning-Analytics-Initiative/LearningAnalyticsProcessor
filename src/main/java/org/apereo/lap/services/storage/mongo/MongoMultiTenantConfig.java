package org.apereo.lap.services.storage.mongo;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

import javax.servlet.DispatcherType;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.embedded.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.web.util.CookieGenerator;

/**
 * @author jbrown
 *
 */
@Profile("mongo-multitenant")
@Configuration
public class MongoMultiTenantConfig {
  private static final Logger logger = LoggerFactory.getLogger(MongoMultiTenantConfig.class);
  
  @Autowired private MongoMultiTenantFilter mongoFilter;

  @Bean
  public FilterRegistrationBean mongoFilterBean() {
    FilterRegistrationBean registrationBean = new FilterRegistrationBean();
    registrationBean.setFilter(mongoFilter);
    List<String> urls = new ArrayList<String>(1);
    urls.add("/api/*");  //authenticated
    registrationBean.setUrlPatterns(urls);
    registrationBean.setOrder(3);
    registrationBean.setDispatcherTypes(EnumSet.allOf(DispatcherType.class));
    return registrationBean;
  }

}

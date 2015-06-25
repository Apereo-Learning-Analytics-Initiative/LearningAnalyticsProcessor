/**
 * 
 */
package org.apereo.lap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

/**
 * @author ggilbert
 *
 */
@ComponentScan("org.apereo.lap")
@Configuration
@EnableAutoConfiguration
public class LearningAnalyticsProcessor {
  final static Logger log = LoggerFactory.getLogger(LearningAnalyticsProcessor.class);
  
  public static void main(String[] args) {
      SpringApplication.run(LearningAnalyticsProcessor.class, args);
  }

}

/**
 * 
 */
package org.apereo.lap.dao;

import javax.sql.DataSource;

import org.springframework.boot.autoconfigure.jdbc.DataSourceBuilder;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.datasource.init.DatabasePopulator;
import org.springframework.jdbc.datasource.init.DatabasePopulatorUtils;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;

/**
 * @author ggilbert
 *
 */
@Configuration
public class H2Config {
  @Primary
  @Bean(name="tempDataSource")
  @ConfigurationProperties(prefix="datasource.temp")
  public DataSource tempDataSource() {
     DataSource ds = DataSourceBuilder.create().build();
     return ds;
  }
  
  @Bean(name="persistentDataSource")
  @ConfigurationProperties(prefix="datasource.persistent")
  public DataSource persistentDataSource() {
      return DataSourceBuilder.create().build();
  }

}

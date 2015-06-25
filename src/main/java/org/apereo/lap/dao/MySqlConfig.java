package org.apereo.lap.dao;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowire;
import org.springframework.boot.autoconfigure.jdbc.DataSourceBuilder;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;

/**
 * @author ggilbert
 *
 */
@Profile("mysql")
@Configuration
public class MySqlConfig {
  @Primary
  @Bean(name="persistentDataSource")
  @ConfigurationProperties(prefix="datasource.persistent")
  public DataSource persistentDataSource() {
      return DataSourceBuilder.create().build();
  }

}

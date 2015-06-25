package org.apereo.lap.dao;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;

@Configuration
public class JpaConfig {
  
  @Autowired
  @Qualifier("persistentDataSource")
  private DataSource dataSource;
  
//  @Bean
//  public LocalContainerEntityManagerFactoryBean entityManagerFactory() {
//    LocalContainerEntityManagerFactoryBean localContainerEntityManagerFactoryBean = new LocalContainerEntityManagerFactoryBean();
//    localContainerEntityManagerFactoryBean.setDataSource(dataSource);
//    localContainerEntityManagerFactoryBean.setPackagesToScan("org.apereo.lap.dao.model");
//    HibernateJpaVendorAdapter hibernateJpaVendorAdapter = new HibernateJpaVendorAdapter();
//    hibernateJpaVendorAdapter.s
//    localContainerEntityManagerFactoryBean.setJpaVendorAdapter();
//    return localContainerEntityManagerFactoryBean;
//  }

}

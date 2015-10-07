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
package org.apereo.lap.services.storage.h2;

import javax.sql.DataSource;

import org.apereo.lap.services.storage.DatasourceProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jdbc.DataSourceBuilder;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.PlatformTransactionManager;

/**
 * @author ggilbert
 */
@Configuration
public class H2TempConfig {
    @Autowired
    private DatasourceProperties datasourceProperties;

    @Primary
    @Bean(name = "tempJdbcTemplate")
    public JdbcTemplate jdbcTemplate() {
        return new JdbcTemplate(tempDataSource());
    }

    @Primary
    @Bean(name = "tempDataSource")
    public DataSource tempDataSource() {
        DatasourceProperties.JdbcInfo p = datasourceProperties.getTemp();
        return DataSourceBuilder
                .create()
                .url(p.getUrl())
                .username(p.getUsername())
                .password(p.getPassword())
                .driverClassName(p.getDriverClassName())
                .build();
    }

    @Bean(name = "transactionManager")
    @Primary
    PlatformTransactionManager transactionManager() {
        return new JpaTransactionManager(entityManagerFactory().getObject());
    }

    @Primary
    @Bean(name = "entityManagerFactory")
    LocalContainerEntityManagerFactoryBean entityManagerFactory() {

        HibernateJpaVendorAdapter jpaVendorAdapter = new HibernateJpaVendorAdapter();
        jpaVendorAdapter.setGenerateDdl(true);

        LocalContainerEntityManagerFactoryBean factoryBean = new LocalContainerEntityManagerFactoryBean();

        factoryBean.setDataSource(tempDataSource());
        factoryBean.setJpaVendorAdapter(jpaVendorAdapter);
        factoryBean.setPackagesToScan("");

        return factoryBean;
    }

}

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

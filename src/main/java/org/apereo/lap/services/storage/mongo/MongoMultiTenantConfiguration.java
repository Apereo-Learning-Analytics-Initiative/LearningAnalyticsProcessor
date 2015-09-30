package org.apereo.lap.services.storage.mongo;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.embedded.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.data.mongodb.config.AbstractMongoConfiguration;
import org.springframework.data.mongodb.config.EnableMongoAuditing;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

import com.mongodb.Mongo;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;

/**
 * @author jbrown
 *
 */
@Profile("mongo-multitenant")
@Configuration
@EnableMongoAuditing
@EnableMongoRepositories({"org.apereo.lap.services.storage.mongo.model"})
public class MongoMultiTenantConfiguration extends AbstractMongoConfiguration {
  private static final Logger logger = LoggerFactory.getLogger(MongoMultiTenantConfiguration.class);
  
  @Autowired private MongoMultiTenantFilter mongoFilter;
  
  @Value("${lap.defaultDatabaseName:lap_default}")
  private String dbName;
  
  @Value("${spring.data.mongodb.uri:mongodb://localhost/lap_default}")
  private String dbUri;
  
  @Override
  @Bean
  public Mongo mongo() throws Exception {
      logger.warn("Mongo Db URI is set to: {}", dbUri);
      return new MongoClient(new MongoClientURI(dbUri));
  }

  @Override
  protected String getDatabaseName() {
      return dbName;
  }

  @Bean
  public MongoTemplate mongoTemplate(final Mongo mongo, MultiTenantMongoDbFactory dbFactory) throws Exception {
    MongoTemplate template = new MongoTemplate(mongoDbFactory(mongo));
    dbFactory.setMongoTemplate(template);
    return template;
  }

  @Bean
  public MultiTenantMongoDbFactory mongoDbFactory(final Mongo mongo) throws Exception {
    return new MultiTenantMongoDbFactory(mongo, dbName);
  }

  @Bean
  public FilterRegistrationBean mongoFilterBean() {
    FilterRegistrationBean registrationBean = new FilterRegistrationBean();
    registrationBean.setFilter(mongoFilter);
    List<String> urls = new ArrayList<String>();
    
    urls.add("/");
    urls.add("/user");
    urls.add("/login");
    urls.add("/history/*");
    urls.add("/features/*");
    urls.add("/admin/*");
    urls.add("/pipelines/*");
    urls.add("/api/output/*");
    
    registrationBean.setUrlPatterns(urls);
    return registrationBean;
  }

}

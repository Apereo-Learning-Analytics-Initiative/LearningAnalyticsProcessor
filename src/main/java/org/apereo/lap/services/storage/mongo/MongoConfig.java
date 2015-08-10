/**
 * 
 */
package org.apereo.lap.services.storage.mongo;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.data.mongodb.config.EnableMongoAuditing;

/**
 * @author ggilbert
 *
 */
@Profile("mongo")
@Configuration
@EnableMongoAuditing
public class MongoConfig {
}

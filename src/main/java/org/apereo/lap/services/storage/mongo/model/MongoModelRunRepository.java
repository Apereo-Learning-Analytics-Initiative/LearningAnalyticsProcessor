/**
 * 
 */
package org.apereo.lap.services.storage.mongo.model;

import org.apereo.lap.services.storage.ModelRun;
import org.springframework.context.annotation.Profile;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

/**
 * @author ggilbert
 *
 */
@Repository
@Profile("mongo")
public interface MongoModelRunRepository extends MongoRepository<ModelRun, String> {
  Page<ModelRun> findAllByOrderByCreatedDateDesc(Pageable pageable);
}

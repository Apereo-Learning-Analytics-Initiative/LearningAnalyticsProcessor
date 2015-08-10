/**
 * 
 */
package org.apereo.lap.services.storage.mongo;

import org.apereo.lap.services.storage.ModelRun;
import org.apereo.lap.services.storage.ModelRunPersistentStorage;
import org.apereo.lap.services.storage.mongo.model.MongoModelRunRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

/**
 * @author ggilbert
 *
 */
@Component("MongoDB-ModelRunPersistentStorage")
@Profile("mongo")
public class MongoModelRunPersistentStorage implements ModelRunPersistentStorage {
  
  @Autowired private MongoModelRunRepository mongoModelRunRepository;

  @Override
  public ModelRun save(ModelRun modelRun) {
    return mongoModelRunRepository.save(modelRun);
  }

  @Override
  public Page<ModelRun> findAll(Pageable pageable) {
    return mongoModelRunRepository.findAllByOrderByCreatedDateDesc(pageable);
  }

}

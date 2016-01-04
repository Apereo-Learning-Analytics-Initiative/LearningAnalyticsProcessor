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
@Profile({"mongo", "mongo-multitenant"})
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

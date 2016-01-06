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
package org.apereo.lap.services.storage.mongo;

import java.util.List;

import org.apereo.lap.model.SSPConfig;
import org.apereo.lap.services.storage.SSPConfigPersistentStorage;
import org.apereo.lap.services.storage.mongo.model.MongoSSPConfigRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/**
 * @author ggilbert
 *
 */
@Component("MongoDB-SSPConfigPersistentStorage")
@ConditionalOnProperty(name="lap.persistentStorage", havingValue="MongoDB")
public class MongoSSPConfigPersistentStorage implements SSPConfigPersistentStorage {
  
  @Autowired private MongoSSPConfigRepository mongoSSPConfigRepository;

  @Override
  public SSPConfig save(SSPConfig sspConfig) {
    return mongoSSPConfigRepository.save(sspConfig);
  }

  @Override
  public SSPConfig get() {
    List<SSPConfig> configList = mongoSSPConfigRepository.findAll();
    if (configList != null && !configList.isEmpty()) {
      return configList.get(0);
    }
    return null;
  }

}

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
package org.apereo.lap.services.storage.h2;

import java.util.List;

import org.apereo.lap.model.SSPConfig;
import org.apereo.lap.services.storage.SSPConfigPersistentStorage;
import org.apereo.lap.services.storage.h2.model.JpaSSPConfig;
import org.apereo.lap.services.storage.h2.model.SSPConfigRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author ggilbert
 *
 */
@Component("H2-SSPConfigPersistentStorage")
public class H2SSPConfigPersistentStorage implements SSPConfigPersistentStorage {
  
  @Autowired private SSPConfigRepository sspConfigRepository;

  @Override
  public SSPConfig save(SSPConfig sspConfig) {
    JpaSSPConfig jpaSSPConfig = new JpaSSPConfig(sspConfig);

    JpaSSPConfig saved = sspConfigRepository.save(jpaSSPConfig);
    
    sspConfig.setId(saved.getId().toString());
    return sspConfig;
  }

   @Override
  public SSPConfig get() {
    List<JpaSSPConfig> saved = sspConfigRepository.findAll();
    
    if (saved != null && !saved.isEmpty()) {
      JpaSSPConfig jpaSSPConfig = saved.get(0);
      SSPConfig sspConfig = new SSPConfig();
      sspConfig.setId(jpaSSPConfig.getId().toString());
      sspConfig.setKey(jpaSSPConfig.getKey());
      sspConfig.setSecret(jpaSSPConfig.getSecret());
      sspConfig.setUrl(jpaSSPConfig.getUrl());
      sspConfig.setRiskRule(jpaSSPConfig.getRiskRule());
      
      return sspConfig;
    }
    
    return null;
  }

}

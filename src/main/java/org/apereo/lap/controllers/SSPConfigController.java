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
package org.apereo.lap.controllers;

import org.apereo.lap.model.SSPConfig;
import org.apereo.lap.services.storage.SSPConfigPersistentStorage;
import org.apereo.lap.services.storage.StorageFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * @author ggilbert
 *
 */
@Controller
public class SSPConfigController {
  @Autowired private StorageFactory storageFactory;
  
  @RequestMapping(method = RequestMethod.GET, produces = {"application/json"}, value="/api/sspconfig")
  public @ResponseBody SSPConfig sspConfigForAPI() {
    
    return sspConfig();
  }

  @RequestMapping(method = RequestMethod.GET, produces = {"application/json"}, value="/sspconfig")
  public @ResponseBody SSPConfig sspConfig() {
    
    SSPConfigPersistentStorage persistentStorage = storageFactory.getSSPConfigPersistentStorage();
    return persistentStorage.get();
  }
  
  @RequestMapping(value = "/sspconfig", method = RequestMethod.POST, 
      produces = "application/json;charset=utf-8", consumes = "application/json")
  public @ResponseBody  SSPConfig save(@RequestBody SSPConfig sspConfig) {
    
    SSPConfigPersistentStorage persistentStorage = storageFactory.getSSPConfigPersistentStorage();
    return persistentStorage.save(sspConfig);
  }

}

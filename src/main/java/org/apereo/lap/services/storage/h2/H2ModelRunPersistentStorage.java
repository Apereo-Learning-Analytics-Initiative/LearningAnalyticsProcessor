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

import java.util.ArrayList;
import java.util.List;

import org.apereo.lap.services.storage.ModelRun;
import org.apereo.lap.services.storage.ModelRunPersistentStorage;
import org.apereo.lap.services.storage.h2.model.Run;
import org.apereo.lap.services.storage.h2.model.RunRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

/**
 * @author ggilbert
 *
 */
@Component("H2-ModelRunPersistentStorage")
public class H2ModelRunPersistentStorage implements ModelRunPersistentStorage {
  
  @Autowired private RunRepository runRepository;

  @Override
  public ModelRun save(ModelRun modelRun) {
    
    Run run = toRun(modelRun);
    
    Run savedRun = runRepository.save(run);
    modelRun.setCreatedDate(savedRun.getDateCreated());
    modelRun.setId(savedRun.getId().toString());
    
    return modelRun;
  }

  @Override
  public Page<ModelRun> findAll(Pageable pageable) {
    return convert(runRepository.findAllByOrderByDateCreatedDesc(pageable), pageable);
  }
  
  private Page<ModelRun> convert(Page<Run> runEntities, Pageable pageable) {
    List<Run> runList = runEntities.getContent();
    Page<ModelRun> modelRunPage = null;
    if (runList != null && !runList.isEmpty()) {
      List<ModelRun> modelRunEntites = new ArrayList<ModelRun>();
      for (Run run : runList) {
        modelRunEntites.add(fromRun(run));
      }
      modelRunPage = new PageImpl<ModelRun>(modelRunEntites, pageable, modelRunEntites.size());
    }
    return modelRunPage;
  }
  
  private Run toRun(ModelRun modelRun) {
    Run run = new Run();
    run.setModelCount(modelRun.getModelCount());
    run.setModelName(modelRun.getModelName());
    run.setModelType(modelRun.getModelType());
    run.setModelRunId(modelRun.getModel_run_id());
    run.setSuccess(modelRun.isSuccess());
    
    return run;
  }
  
  private ModelRun fromRun(Run run) {
    ModelRun modelRun = new ModelRun();
    modelRun.setId(String.valueOf(run.getId()));
    modelRun.setCreatedDate(run.getDateCreated());
    modelRun.setModel_run_id(run.getModelRunId());
    modelRun.setModelCount(run.getModelCount());
    modelRun.setModelName(run.getModelName());
    modelRun.setModelType(run.getModelType());
    modelRun.setSuccess(run.isSuccess());
    
    return modelRun;
  }
  

}

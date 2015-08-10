/**
 * 
 */
package org.apereo.lap.controllers.api;

import java.util.ArrayList;

import org.apereo.lap.model.api.ModelOutputRecord;
import org.apereo.lap.model.api.ModelRunRecord;
import org.apereo.lap.model.api.ModelRunResourceAssembler;
import org.apereo.lap.services.storage.ModelOutput;
import org.apereo.lap.services.storage.ModelRun;
import org.apereo.lap.services.storage.ModelRunPersistentStorage;
import org.apereo.lap.services.storage.PersistentStorage;
import org.apereo.lap.services.storage.StorageFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.hateoas.PagedResources;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author ggilbert
 *
 */
@RestController
public class ModelRunController {
  @Autowired private StorageFactory storageFactory;
  @Autowired private ModelRunResourceAssembler modelRunResourceAssembler;
  
  @RequestMapping(method = RequestMethod.GET, produces = {"application/json"}, value="/api/runs")
  public PagedResources<ModelRunRecord> runs(@PageableDefault(size = 10, page = 0) Pageable pageable, 
      PagedResourcesAssembler<ModelRun> assembler) {
    
    ModelRunPersistentStorage persistentStorage = storageFactory.getModelRunPersistentStorage();
    Page<ModelRun> output = persistentStorage.findAll(pageable);
    if (output == null) {
      output = new PageImpl<ModelRun>(new ArrayList<ModelRun>(), pageable, 0);
    }
    return assembler.toResource(output, modelRunResourceAssembler);
  }

}

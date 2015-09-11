/**
 * 
 */
package org.apereo.lap.model.api;

import java.util.ArrayList;
import java.util.List;

import org.apereo.lap.controllers.ModelRunController;
import org.apereo.lap.services.storage.ModelRun;
import org.springframework.hateoas.mvc.ResourceAssemblerSupport;
import org.springframework.stereotype.Component;

/**
 * @author ggilbert
 *
 */
@Component
public class ModelRunResourceAssembler extends ResourceAssemblerSupport<ModelRun, ModelRunRecord> {
  public ModelRunResourceAssembler() {
    super(ModelRunController.class, ModelRunRecord.class);
  }

  @Override
  public ModelRunRecord toResource(ModelRun modelRun) {
    return new ModelRunRecord(modelRun);
  }

  @Override
  public List<ModelRunRecord> toResources(Iterable<? extends ModelRun> modelRunEntities) {
    List<ModelRunRecord> modelRunRecords = new ArrayList<ModelRunRecord>();
    for (ModelRun modelRun : modelRunEntities) {
      modelRunRecords.add(new ModelRunRecord(modelRun));
    }
    return modelRunRecords;
  }
}

package org.apereo.lap.model.api;

import java.util.ArrayList;
import java.util.List;

import org.apereo.lap.controllers.api.ModelOutputController;
import org.apereo.lap.services.storage.ModelOutput;
import org.springframework.hateoas.mvc.ResourceAssemblerSupport;
import org.springframework.stereotype.Component;

@Component
public class ModelOutputResourceAssembler extends ResourceAssemblerSupport<ModelOutput, ModelOutputRecord> {

  public ModelOutputResourceAssembler() {
    super(ModelOutputController.class, ModelOutputRecord.class);
  }

  @Override
  public ModelOutputRecord toResource(ModelOutput modelOutput) {
    return new ModelOutputRecord(modelOutput);
  }

  @Override
  public List<ModelOutputRecord> toResources(Iterable<? extends ModelOutput> modelOutputEntities) {
    List<ModelOutputRecord> modelOutputRecords = new ArrayList<ModelOutputRecord>();
    for (ModelOutput modelOutput : modelOutputEntities) {
      modelOutputRecords.add(new ModelOutputRecord(modelOutput));
    }
    return modelOutputRecords;
  }
}

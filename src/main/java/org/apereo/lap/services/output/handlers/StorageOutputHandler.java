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
package org.apereo.lap.services.output.handlers;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

//import org.apereo.lap.dao.RiskConfidenceRepository;
//import org.apereo.lap.dao.model.RiskConfidence;
import org.apereo.lap.model.Output;
import org.apereo.lap.services.storage.ModelOutput;
import org.apereo.lap.services.storage.PersistentStorage;
import org.apereo.lap.services.storage.StorageFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.stereotype.Component;

/**
 * Handles the output processing for a single target output type
 *
 * @author Aaron Zeckoski (azeckoski @ unicon.net) (azeckoski @ vt.edu)
 */
@Component
public class StorageOutputHandler extends BaseOutputHandler implements OutputHandler {

  
  @Autowired
  private StorageFactory storageFactory;

  @Override
  public Output.OutputType getHandledType() {
    return Output.OutputType.STORAGE;
  }

  @Override
  public OutputResult writeOutput(Output output) {
    
    PersistentStorage<ModelOutput> persistentStorage = storageFactory.getPersistentStorage();
    
    OutputResult result = new OutputResult(output);

    Map<String, String> sourceToHeaderMap = output.makeSourceTargetMap();
    String selectSQL = output.makeTempDBSelectSQL();

    SqlRowSet rowSet;
    try {
      rowSet = storage.getTempJdbcTemplate().queryForRowSet(selectSQL);
      
    } catch (Exception e) {
      throw new RuntimeException("Failure while trying to retrieve the output data set: " + selectSQL);
    }

    List<ModelOutput> modelOutputEntities = new ArrayList<ModelOutput>();
    String modelRunId = UUID.randomUUID().toString();

    while (rowSet.next()) {
      ModelOutput modelOutput = new ModelOutput();

      if (!rowSet.wasNull()) {
        modelOutput.setModelRunId(modelRunId);

        String[] rowVals = new String[sourceToHeaderMap.size()];

        if (rowVals.length > 0)
          modelOutput.setStudentId(rowSet.getString(1));

        if (rowVals.length > 1)
          modelOutput.setCourseId(rowSet.getString(2));

        if (rowVals.length > 2)
          modelOutput.setRisk_score(rowSet.getString(3));

        modelOutputEntities.add(modelOutput);
      }
    }
    
    persistentStorage.saveAll(modelOutputEntities);
    result.done(modelRunId, modelOutputEntities.size());
    
    return result;
  }

}

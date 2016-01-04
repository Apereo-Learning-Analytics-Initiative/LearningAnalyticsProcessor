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
import java.util.HashMap;
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
import org.springframework.jdbc.InvalidResultSetAccessException;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.jdbc.support.rowset.SqlRowSetMetaData;
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

    String selectSQL = output.makeTempDBSelectStarSQL();

    SqlRowSet rowSet = null;
    SqlRowSetMetaData metadata = null;
    try {
      rowSet = storage.getTempJdbcTemplate().queryForRowSet(selectSQL);
      metadata = rowSet.getMetaData();
    } catch (Exception e) {
      throw new RuntimeException("Failure while trying to retrieve the output data set: " + selectSQL);
    }

    List<ModelOutput> modelOutputEntities = new ArrayList<ModelOutput>();
    String modelRunId = UUID.randomUUID().toString();
    while (rowSet.next()) {      
      ModelOutput modelOutput = new ModelOutput();
      Map<String, Object> data = new HashMap<String, Object>();
      for (String column : metadata.getColumnNames()) {
        data.put(column, rowSet.getObject(column));
      }
      modelOutput.setOutput(data);
      modelOutput.setModelRunId(modelRunId);
      modelOutputEntities.add(modelOutput);
    }
    
    persistentStorage.saveAll(modelOutputEntities);
    result.done(modelRunId, modelOutputEntities.size());
    
    return result;
  }

}

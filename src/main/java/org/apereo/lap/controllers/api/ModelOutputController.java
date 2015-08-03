/**
 * 
 */
package org.apereo.lap.controllers.api;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apereo.lap.dao.model.RiskConfidence;
import org.apereo.lap.dao.riskconfidence.RiskConfidenceRepository;
import org.apereo.lap.model.api.ModelOutputRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author ggilbert
 *
 */
@RestController
public class ModelOutputController {
  
  @Autowired
  private RiskConfidenceRepository riskConfidenceRepository;

  
  @RequestMapping("/api/output")
  @ResponseBody
  public HttpEntity<List<ModelOutputRecord>> output() {
    
    List<RiskConfidence> riskConfidenceRecords = riskConfidenceRepository.findAll();
    List<ModelOutputRecord> output = null;
    
    if (riskConfidenceRecords != null && !riskConfidenceRecords.isEmpty()) {
      output = new ArrayList<ModelOutputRecord>();
      for (RiskConfidence riskConfidence : riskConfidenceRecords) {
        Map<String, Object> data = new HashMap<String,Object>();
        data.put("id", riskConfidence.getId());
        data.put("userId", riskConfidence.getAlternativeId());
        data.put("courseId", riskConfidence.getCourseId());
        data.put("createdDate", riskConfidence.getDateCreated());
        data.put("risk", riskConfidence.getModelRiskConfidence());
        ModelOutputRecord modelOutputRecord = new ModelOutputRecord(data);
        output.add(modelOutputRecord);
      }
    }


    return new ResponseEntity<List<ModelOutputRecord>>(output, HttpStatus.OK);
  }
  
}

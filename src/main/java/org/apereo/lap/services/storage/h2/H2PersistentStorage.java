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
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apereo.lap.services.storage.ModelOutput;
import org.apereo.lap.services.storage.PersistentLAPEntity;
import org.apereo.lap.services.storage.PersistentStorage;
import org.apereo.lap.services.storage.h2.model.RiskConfidence;
import org.apereo.lap.services.storage.h2.model.RiskConfidenceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

/**
 * @author ggilbert
 *
 */
@Component("H2")
public class H2PersistentStorage implements PersistentStorage<ModelOutput> {
  
  @Autowired
  private RiskConfidenceRepository riskConfidenceRepository;

  @Override
  public ModelOutput save(ModelOutput persistentLAPEntity) {
    RiskConfidence riskConfidence = toRiskConfidence((ModelOutput)persistentLAPEntity);
    RiskConfidence savedRiskConfidence = riskConfidenceRepository.save(riskConfidence);
    // nothing to change
    return fromRiskConfidence(savedRiskConfidence);
  }

  @Override
  public List<ModelOutput> saveAll(Collection<ModelOutput> persistentLAPentities) {
    if (persistentLAPentities != null && !persistentLAPentities.isEmpty()) {
      List<RiskConfidence> riskConfidenceEntities = new ArrayList<RiskConfidence>();
      for(PersistentLAPEntity persistentLAPEntity : persistentLAPentities) {
        RiskConfidence riskConfidence = toRiskConfidence((ModelOutput)persistentLAPEntity);
        riskConfidenceEntities.add(riskConfidence);
      }
      
      List<RiskConfidence> savedRiskConfidenceEntities = riskConfidenceRepository.save(riskConfidenceEntities);
      List<ModelOutput> modelOutputEntities = new ArrayList<ModelOutput>();
      for (RiskConfidence riskConfidence : savedRiskConfidenceEntities) {
        modelOutputEntities.add(fromRiskConfidence(riskConfidence));
      }
      
      return modelOutputEntities;
    }
    return null;
  }
  
  @Override
  public Page<ModelOutput> findAll(Pageable pageable) {
    return convert(riskConfidenceRepository.findAll(pageable), pageable);
  }
  
  @Override
  public Page<ModelOutput> findByStudentId(String studentId, Pageable pageable) {
    return convert(riskConfidenceRepository.findByAlternativeId(studentId, pageable), pageable);
  }
  
  @Override
  public Page<ModelOutput> findByCourseId(String courseId, boolean onlyLastRun, Pageable pageable) {
    
    Page<ModelOutput> page = null;
    if (onlyLastRun) {
      RiskConfidence riskConfidence = riskConfidenceRepository.findTopByCourseIdOrderByDateCreatedDesc(courseId);
      if (riskConfidence != null) {
        page = convert(riskConfidenceRepository.findByGroupIdAndCourseId(riskConfidence.getGroupId(), courseId, pageable),pageable);
      }
    }
    else {
      page = convert(riskConfidenceRepository.findByCourseId(courseId, pageable), pageable);
    }
    
    return page;
  }
  
  @Override
  public Page<ModelOutput> findByStudentIdAndCourseId(String studentId, String courseId, boolean onlyLastRun, Pageable pageable) {
    
    Page<ModelOutput> page = null;
    if (onlyLastRun) {
      RiskConfidence riskConfidence = riskConfidenceRepository.findTopByCourseIdOrderByDateCreatedDesc(courseId);
      if (riskConfidence != null) {
        page = convert(riskConfidenceRepository.findTopByCourseIdAndAlternativeIdOrderByDateCreatedDesc(courseId, studentId, pageable),pageable);
      }
    }
    else {
      page = convert(riskConfidenceRepository.findByAlternativeIdAndCourseId(studentId, courseId, pageable), pageable);
    }
    
    return page;
  }
  
  private Page<ModelOutput> convert(Page<RiskConfidence> riskConfidenceEntities, Pageable pageable) {
    List<RiskConfidence> riskConfidenceList = riskConfidenceEntities.getContent();
    Page<ModelOutput> modelOutputPage = null;
    if (riskConfidenceList != null && !riskConfidenceList.isEmpty()) {
      List<ModelOutput> modelOutputEntites = new ArrayList<ModelOutput>();
      for (RiskConfidence riskConfidence : riskConfidenceList) {
        modelOutputEntites.add(fromRiskConfidence(riskConfidence));
      }
      modelOutputPage = new PageImpl<ModelOutput>(modelOutputEntites, pageable, modelOutputEntites.size());
    }
    return modelOutputPage;
  }
  
  private RiskConfidence toRiskConfidence(ModelOutput modelOutput) {
    RiskConfidence riskConfidence = new RiskConfidence();
    if (modelOutput.getOutput() != null) {
      riskConfidence.setAlternativeId((String)modelOutput.getOutput().get("ALTERNATIVE_ID"));
      riskConfidence.setCourseId((String)modelOutput.getOutput().get("COURSE_ID"));
      riskConfidence.setModelRiskConfidence((String)modelOutput.getOutput().get("MODEL_RISK_CONFIDENCE"));
    }
    riskConfidence.setDateCreated(new Date());
    riskConfidence.setGroupId(modelOutput.getModelRunId());

    return riskConfidence;
  }
  
  private ModelOutput fromRiskConfidence(RiskConfidence riskConfidence) {
    ModelOutput modelOutput = new ModelOutput();
    modelOutput.setId(String.valueOf(riskConfidence.getId()));
    modelOutput.setCreatedDate(riskConfidence.getDateCreated());
    modelOutput.setModelRunId(riskConfidence.getGroupId());
    
    Map<String, Object> output = new HashMap<String, Object>();
    output.put("ALTERNATIVE_ID", riskConfidence.getAlternativeId());
    output.put("COURSE_ID", riskConfidence.getCourseId());
    output.put("MODEL_RISK_CONFIDENCE", riskConfidence.getModelRiskConfidence());
    
    modelOutput.setOutput(output);
    return modelOutput;
  }
}

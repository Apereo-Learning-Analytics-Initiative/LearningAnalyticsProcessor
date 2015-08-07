/**
 * 
 */
package org.apereo.lap.services.storage.h2;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

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
  public Page<ModelOutput> findByCourseId(String courseId, Pageable pageable) {
    return convert(riskConfidenceRepository.findByCourseId(courseId, pageable), pageable);
  }
  
  @Override
  public Page<ModelOutput> findByStudentIdAndCourseId(String studentId, String courseId, Pageable pageable) {
    return convert(riskConfidenceRepository.findByAlternativeIdAndCourseId(studentId, courseId, pageable), pageable);
  }
  
  private Page<ModelOutput> convert(Page<RiskConfidence> riskConfidenceEntities, Pageable pageable) {
    List<RiskConfidence> riskConfidenceList = riskConfidenceEntities.getContent();
    Page<ModelOutput> modelOutputPage = null;
    if (riskConfidenceList != null && !riskConfidenceList.isEmpty()) {
      List<ModelOutput> modelOutputEntites = new ArrayList<ModelOutput>();
      for (RiskConfidence riskConfidence : riskConfidenceEntities) {
        modelOutputEntites.add(fromRiskConfidence(riskConfidence));
      }
      modelOutputPage = new PageImpl<ModelOutput>(modelOutputEntites, pageable, modelOutputEntites.size());
    }
    return modelOutputPage;
  }
  
  private RiskConfidence toRiskConfidence(ModelOutput modelOutput) {
    RiskConfidence riskConfidence = new RiskConfidence();
    riskConfidence.setAlternativeId(modelOutput.getStudentId());
    riskConfidence.setCourseId(modelOutput.getCourseId());
    riskConfidence.setGroupId(modelOutput.getModel_run_id());
    riskConfidence.setModelRiskConfidence(modelOutput.getRisk_score());
    riskConfidence.setDateCreated(new Date());
    
    return riskConfidence;
  }
  
  private ModelOutput fromRiskConfidence(RiskConfidence riskConfidence) {
    ModelOutput modelOutput = new ModelOutput();
    modelOutput.setId(String.valueOf(riskConfidence.getId()));
    modelOutput.setCourseId(riskConfidence.getCourseId());
    modelOutput.setCreated_date(riskConfidence.getDateCreated());
    modelOutput.setModel_run_id(riskConfidence.getGroupId());
    modelOutput.setRisk_score(riskConfidence.getModelRiskConfidence());
    modelOutput.setStudentId(riskConfidence.getAlternativeId());
    return modelOutput;
  }
}

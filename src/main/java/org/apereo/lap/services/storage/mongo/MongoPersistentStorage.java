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
package org.apereo.lap.services.storage.mongo;

import java.util.Collection;
import java.util.List;

import org.apereo.lap.services.storage.ModelOutput;
import org.apereo.lap.services.storage.PersistentStorage;
import org.apereo.lap.services.storage.mongo.model.MongoModelOutputRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Component;

import static org.springframework.data.mongodb.core.query.Criteria.where;
import static org.springframework.data.mongodb.core.query.Query.query;

/**
 * @author ggilbert
 *
 */
@Component("MongoDB")
@Profile({"mongo", "mongo-multitenant"})
public class MongoPersistentStorage implements PersistentStorage<ModelOutput> {
  
  @Autowired
  private MongoModelOutputRepository mongoModelOutputRepository;
  
  @Autowired
  private MongoTemplate mongoTemplate;

  @Override
  public ModelOutput save(ModelOutput persistentLAPEntity) {
    return mongoModelOutputRepository.save((ModelOutput)persistentLAPEntity);
  }

  @Override
  public List<ModelOutput> saveAll(Collection<ModelOutput> persistentLAPentities) {
    List<ModelOutput> mo = mongoModelOutputRepository.save(persistentLAPentities);
    return mo;
  }

  @Override
  public Page<ModelOutput> findAll(Pageable pageable) {
    Page<ModelOutput> mo = mongoModelOutputRepository.findAll(pageable);
    return mo;
  }

  @Override
  public Page<ModelOutput> findByStudentId(String studentId, Pageable pageable) {
    
    return mongoModelOutputRepository.findByStudentId(studentId, pageable);
  }

  @Override
  public Page<ModelOutput> findByCourseId(String courseId, boolean onlyLastRun, Pageable pageable) {
    Page<ModelOutput> page = null;
    if (onlyLastRun) {
      ModelOutput modelOutput = mongoModelOutputRepository.findTopByCourseIdOrderByCreatedDateDesc(courseId);
      if (modelOutput != null) {
        page = mongoModelOutputRepository.findByModelRunIdAndCourseId(modelOutput.getModelRunId(), courseId, pageable);
      }
    }
    else {
      page = mongoModelOutputRepository.findByCourseId(courseId, pageable);
    }
    
    return page;
  }

  @Override
  public Page<ModelOutput> findByStudentIdAndCourseId(String studentId, String courseId, boolean onlyLastRun, Pageable pageable) {
    Page<ModelOutput> page = null;
    if (onlyLastRun) {
      ModelOutput modelOutput = mongoModelOutputRepository.findTopByCourseIdOrderByCreatedDateDesc(courseId);
      if (modelOutput != null) {
        page = mongoModelOutputRepository.findTopByCourseIdAndStudentIdOrderByCreatedDateDesc(courseId, studentId, pageable);
      }
    }
    else {
      page = mongoModelOutputRepository.findByStudentIdAndCourseId(studentId, courseId, pageable);
    }
    
    return page;
  }

}

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
package org.apereo.lap.services.storage.mongo.model;

import org.apereo.lap.services.storage.ModelOutput;
import org.springframework.context.annotation.Profile;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

/**
 * @author ggilbert
 *
 */
@Profile({"mongo", "mongo-multitenant"})
public interface MongoModelOutputRepository extends MongoRepository<ModelOutput, String> {
  
  @Query("{ 'output.ALTERNATIVE_ID' : ?0 }")
  Page<ModelOutput> findByStudentId(String student_id, Pageable pageable);
  
  @Query("{ 'output.COURSE_ID' : ?0 }")
  Page<ModelOutput> findByCourseId(String course_id, Pageable pageable);
  
  @Query("{ 'output.ALTERNATIVE_ID' : ?0, 'output.COURSE_ID' : ?1 }")
  Page<ModelOutput> findByStudentIdAndCourseId(String studentId, String courseId, Pageable pageable);
  
  @Query("{ 'output.COURSE_ID' : ?0 }")
  ModelOutput findTopByCourseIdOrderByCreatedDateDesc(String courseId);
  
  @Query("{ 'modelRunId' : ?0, 'output.COURSE_ID' : ?1 }")
  Page<ModelOutput> findByModelRunIdAndCourseId(String modelRunId, String courseId, Pageable pageable);
  
  @Query("{ 'output.COURSE_ID' : ?0, 'output.ALTERNATIVE_ID' : ?1 }")
  Page<ModelOutput> findTopByCourseIdAndStudentIdOrderByCreatedDateDesc(String courseId, String studentId, Pageable pageable);
}

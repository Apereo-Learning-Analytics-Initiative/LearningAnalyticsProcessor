/**
 * 
 */
package org.apereo.lap.services.storage.mongo.model;

import org.apereo.lap.services.storage.ModelOutput;
import org.springframework.context.annotation.Profile;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;

/**
 * @author ggilbert
 *
 */
@Profile({"mongo", "mongo-multitenant"})
public interface MongoModelOutputRepository extends MongoRepository<ModelOutput, String> {
  Page<ModelOutput> findByStudentId(String student_id, Pageable pageable);
  Page<ModelOutput> findByCourseId(String course_id, Pageable pageable);
  Page<ModelOutput> findByStudentIdAndCourseId(String studentId, String courseId, Pageable pageable);
  ModelOutput findTopByCourseIdOrderByCreatedDateDesc(String courseId);
  Page<ModelOutput> findByModelRunIdAndCourseId(String modelRunId, String courseId, Pageable pageable);
  Page<ModelOutput> findTopByCourseIdAndStudentIdOrderByCreatedDateDesc(String courseId, String studentId, Pageable pageable);
}

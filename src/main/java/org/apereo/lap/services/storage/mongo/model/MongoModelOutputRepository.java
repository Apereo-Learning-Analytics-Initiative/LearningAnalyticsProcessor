/**
 * 
 */
package org.apereo.lap.services.storage.mongo.model;

import org.apereo.lap.services.storage.ModelOutput;
import org.springframework.context.annotation.Profile;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

/**
 * @author ggilbert
 *
 */
@Repository
@Profile("mongo")
public interface MongoModelOutputRepository extends MongoRepository<ModelOutput, String> {
  Page<ModelOutput> findByStudentId(String student_id, Pageable pageable);
  Page<ModelOutput> findByCourseId(String course_id, Pageable pageable);
  Page<ModelOutput> findByStudentIdAndCourseId(String studentId, String courseId, Pageable pageable);
}

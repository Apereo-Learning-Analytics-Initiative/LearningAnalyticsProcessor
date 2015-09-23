package org.apereo.lap.services.storage;

import java.util.Collection;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * @author ggilbert
 *
 */
public interface PersistentStorage<ModelOutput> {
  ModelOutput save(ModelOutput persistentLAPEntity);
  List<ModelOutput> saveAll(Collection<ModelOutput> persistentLAPentities);
  Page<ModelOutput> findAll(Pageable pageable);
  Page<ModelOutput> findByStudentId(String studentId, Pageable pageable);
  Page<ModelOutput> findByCourseId(String courseId, boolean onlyLastRun, Pageable pageable);
  Page<ModelOutput> findByStudentIdAndCourseId(String studentId, String courseId, boolean onlyLastRun, Pageable pageable);

}

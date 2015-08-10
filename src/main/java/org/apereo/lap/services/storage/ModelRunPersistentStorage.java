/**
 * 
 */
package org.apereo.lap.services.storage;


import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * @author ggilbert
 *
 */
public interface ModelRunPersistentStorage {
  ModelRun save(ModelRun modelRun);
  Page<ModelRun> findAll(Pageable pageable);
}

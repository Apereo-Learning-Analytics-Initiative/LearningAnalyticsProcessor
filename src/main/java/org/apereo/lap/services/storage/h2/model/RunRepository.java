/**
 * 
 */
package org.apereo.lap.services.storage.h2.model;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


/**
 * @author ggilbert
 *
 */
public interface RunRepository extends JpaRepository<Run, Long> {
  Page<Run> findAllByOrderByDateCreatedDesc(Pageable pageable);
}

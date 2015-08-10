/**
 * 
 */
package org.apereo.lap.model.api;

import org.apereo.lap.services.storage.ModelRun;
import org.springframework.hateoas.Resource;

/**
 * @author ggilbert
 *
 */
public class ModelRunRecord extends Resource<ModelRun> {
  public ModelRunRecord (ModelRun data) {
    super(data);
  }
}

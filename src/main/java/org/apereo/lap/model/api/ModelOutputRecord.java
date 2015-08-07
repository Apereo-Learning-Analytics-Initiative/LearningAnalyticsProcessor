/**
 * 
 */
package org.apereo.lap.model.api;

import org.apereo.lap.services.storage.ModelOutput;
import org.springframework.hateoas.Resource;

/**
 * @author ggilbert
 *
 */
public class ModelOutputRecord extends Resource<ModelOutput> {
  public ModelOutputRecord (ModelOutput data) {
    super(data);
  }
}

/**
 * 
 */
package org.apereo.lap.model.api;

import java.util.Map;

import org.springframework.hateoas.ResourceSupport;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * @author ggilbert
 *
 */
public class ModelOutputRecord extends ResourceSupport {
  private Map<String, ?> data;
  
  @JsonCreator
  public ModelOutputRecord (@JsonProperty("data") Map<String, ?> data) {
    this.data = data;
  }
  
  public Map<String, ?> getData() {
    return this.data;
  }
}

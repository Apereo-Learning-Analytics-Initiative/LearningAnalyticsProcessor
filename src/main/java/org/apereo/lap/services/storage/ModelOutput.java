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
package org.apereo.lap.services.storage;

import java.util.Date;
import java.util.Map;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;


/**
 * @author ggilbert
 *
 */
@JsonPropertyOrder({"id","output","modelRunId","createdDate","modifiedDate"})
public class ModelOutput implements PersistentLAPEntity {

  private static final long serialVersionUID = 1L;
  
  @Id private String id;
  private Map<String, Object> output;
  private String modelRunId;
  @CreatedDate
  private Date createdDate;
  @LastModifiedDate
  private Date modifiedDate;
  
  public String getId() {
    return id;
  }
  public void setId(String id) {
    this.id = id;
  }
  public Map<String, Object> getOutput() {
    return output;
  }
  public void setOutput(Map<String, Object> output) {
    this.output = output;
  }
  public String getModelRunId() {
    return modelRunId;
  }
  public void setModelRunId(String modelRunId) {
    this.modelRunId = modelRunId;
  }
  public Date getCreatedDate() {
    return createdDate;
  }
  public void setCreatedDate(Date createdDate) {
    this.createdDate = createdDate;
  }
  public Date getModifiedDate() {
    return modifiedDate;
  }
  public void setModifiedDate(Date modifiedDate) {
    this.modifiedDate = modifiedDate;
  }
  @Override
  public String toString() {
    return "ModelOutput [id=" + id + ", output=" + output + ", modelRunId=" + modelRunId + ", createdDate=" + createdDate + ", modifiedDate="
        + modifiedDate + "]";
  }

}

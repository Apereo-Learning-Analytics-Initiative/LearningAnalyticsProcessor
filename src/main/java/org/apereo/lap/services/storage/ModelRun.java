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

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * @author ggilbert
 *
 */
public class ModelRun implements PersistentLAPEntity {
  private static final long serialVersionUID = 1L;
  
  @Id private String id;
  
  @CreatedDate
  @JsonProperty("created_date")
  private Date createdDate;
  
  private String model_run_id;
  private String modelType;
  private String modelName;
  private int modelCount;
  private boolean success;
  
  public String getId() {
    return id;
  }
  public void setId(String id) {
    this.id = id;
  }
  public Date getCreatedDate() {
    return createdDate;
  }
  public void setCreatedDate(Date created_date) {
    this.createdDate = created_date;
  }
  public String getModel_run_id() {
    return model_run_id;
  }
  public void setModel_run_id(String model_run_id) {
    this.model_run_id = model_run_id;
  }
  
  public int getModelCount() {
    return modelCount;
  }
  public void setModelCount(int modelCount) {
    this.modelCount = modelCount;
  }
  public boolean isSuccess() {
    return success;
  }
  public void setSuccess(boolean success) {
    this.success = success;
  }
  public String getModelType() {
    return modelType;
  }
  public void setModelType(String modelType) {
    this.modelType = modelType;
  }
  public String getModelName() {
    return modelName;
  }
  public void setModelName(String modelName) {
    this.modelName = modelName;
  }
  @Override
  public String toString() {
    return "ModelRun [id=" + id + ", created_date=" + createdDate + ", model_run_id=" + model_run_id + ", modelType=" + modelType + ", modelName="
        + modelName + ", modelCount=" + modelCount + ", success=" + success + "]";
  }

}

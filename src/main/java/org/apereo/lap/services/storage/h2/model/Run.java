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
package org.apereo.lap.services.storage.h2.model;

import java.util.Calendar;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;

/**
 * @author ggilbert
 *
 */
@Entity(name="model_run")
public class Run extends BaseEntity {
  
  private static final long serialVersionUID = -8050631804690469935L;

  @Column(name="MODEL_RUN_ID")
  private String modelRunId;
  @Column(name="DATE_CREATED")
  private Date dateCreated = Calendar.getInstance().getTime();
  @Column(name="MODEL_TYPE")
  private String modelType;
  @Column(name="MODEL_NAME")
  private String modelName;
  @Column(name="MODEL_COUNT")
  private int modelCount;
  @Column(name="SUCCESS")
  private boolean success;

  @Override
  protected boolean matchesClassAndId(Object other) {
    return Run.class.isInstance(other) ? matchesId((Run)other) : false;
  }

  public String getModelRunId() {
    return modelRunId;
  }

  public void setModelRunId(String modelRunId) {
    this.modelRunId = modelRunId;
  }

  public Date getDateCreated() {
    return dateCreated;
  }

  public void setDateCreated(Date dateCreated) {
    this.dateCreated = dateCreated;
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

  @Override
  public String toString() {
    return "Run [modelRunId=" + modelRunId + ", dateCreated=" + dateCreated + ", modelType=" + modelType + ", modelName=" + modelName
        + ", modelCount=" + modelCount + ", success=" + success + "]";
  }

}

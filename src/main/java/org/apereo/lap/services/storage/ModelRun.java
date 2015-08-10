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

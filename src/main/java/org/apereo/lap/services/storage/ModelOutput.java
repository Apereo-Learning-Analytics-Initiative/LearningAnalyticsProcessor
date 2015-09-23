/**
 * 
 */
package org.apereo.lap.services.storage;

import java.util.Date;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;


/**
 * @author ggilbert
 *
 */
@JsonPropertyOrder({"id","risk_score","created_date","model_run_id","student_id","course_id"})
public class ModelOutput implements PersistentLAPEntity {

  private static final long serialVersionUID = 1L;
  
  @Id private String id;
  
  @JsonProperty("student_id")
  private String studentId;
  @JsonProperty("course_id")
  private String courseId;
  private String risk_score;
  @CreatedDate
  @JsonProperty("created_date")
  private Date createdDate;
  @JsonProperty("model_run_id")
  private String modelRunId;
  
  public String getId() {
    return id;
  }
  public void setId(String id) {
    this.id = id;
  }
  public String getStudentId() {
    return studentId;
  }
  public void setStudentId(String student_id) {
    this.studentId = student_id;
  }
  public String getCourseId() {
    return courseId;
  }
  public void setCourseId(String course_id) {
    this.courseId = course_id;
  }
  public String getRisk_score() {
    return risk_score;
  }
  public void setRisk_score(String risk_score) {
    this.risk_score = risk_score;
  }
  public Date getCreatedDate() {
    return createdDate;
  }
  public void setCreatedDate(Date created_date) {
    this.createdDate = created_date;
  }
  public String getModelRunId() {
    return modelRunId;
  }
  public void setModelRunId(String modelRunId) {
    this.modelRunId = modelRunId;
  }
  @Override
  public String toString() {
    return "ModelOutput [id=" + id + ", studentId=" + studentId + ", courseId=" + courseId + ", risk_score=" + risk_score + ", created_date="
        + createdDate + ", modelRunId=" + modelRunId + "]";
  }

}

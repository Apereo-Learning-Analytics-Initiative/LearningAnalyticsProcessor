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
public class ModelOutput implements PersistentLAPEntity {

  private static final long serialVersionUID = 1L;
  
  @Id private String id;
  
  @JsonProperty("student_id")
  private String studentId;
  @JsonProperty("course_id")
  private String courseId;
  private String risk_score;
  @CreatedDate
  private Date created_date;
  private String model_run_id;
  
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
  public Date getCreated_date() {
    return created_date;
  }
  public void setCreated_date(Date created_date) {
    this.created_date = created_date;
  }
  public String getModel_run_id() {
    return model_run_id;
  }
  public void setModel_run_id(String model_run_id) {
    this.model_run_id = model_run_id;
  }
  @Override
  public String toString() {
    return "ModelOutput [id=" + id + ", studentId=" + studentId + ", courseId=" + courseId + ", risk_score=" + risk_score + ", created_date="
        + created_date + ", model_run_id=" + model_run_id + "]";
  }

}

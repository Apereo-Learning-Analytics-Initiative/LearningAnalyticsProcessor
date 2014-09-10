/**
 * Copyright 2013 Unicon (R) Licensed under the
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
 */
package org.apereo.lap.dao.model;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;

@Entity(name="risk_confidence")
public class RiskConfidence  extends BaseEntity {
    private static final long serialVersionUID = -8050631804690469935L;

    @Column(name="ALTERNATIVE_ID")
    private String alternativeId;
    @Column(name="COURSE_ID")
    private String courseId;
    @Column(name="MODEL_RISK_CONFIDENCE")
    private String modelRiskConfidence;
    @Column(name="DATE_CREATED")
    private Date dateCreated;
    
    public String getAlternativeId() {
		return alternativeId;
	}

	public void setAlternativeId(String alternativeId) {
		this.alternativeId = alternativeId;
	}

	public String getCourseId() {
		return courseId;
	}

	public void setCourseId(String courseId) {
		this.courseId = courseId;
	}

	public String getModelRiskConfidence() {
		return modelRiskConfidence;
	}

	public void setModelRiskConfidence(String modelRiskConfidence) {
		this.modelRiskConfidence = modelRiskConfidence;
	}

	protected boolean matchesClassAndId(Object other) {
        return RiskConfidence.class.isInstance(other) ? matchesId((RiskConfidence)other) : false;
    }

	public Date getDateCreated() {
		return dateCreated;
	}

	public void setDateCreated(Date dateCreated) {
		this.dateCreated = dateCreated;
	}
}

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
package org.apereo.lap.services.storage.h2.model;

import javax.persistence.Column;
import javax.persistence.Entity;

import org.apereo.lap.model.SSPConfig;

/**
 * @author ggilbert
 *
 */
@Entity(name="ssp_config")
public class JpaSSPConfig extends BaseEntity {

  private static final long serialVersionUID = 1L;
  
  public JpaSSPConfig() {}
  
  public JpaSSPConfig(SSPConfig sspConfig) {
    if (sspConfig.getId() != null) {
      this.setId(Long.getLong(sspConfig.getId()));
    }
    
    this.key = sspConfig.getKey();
    this.secret = sspConfig.getSecret();
    this.url = sspConfig.getUrl();
    this.riskRule = sspConfig.getRiskRule();
  }
  
  @Column(name="KEY")
  private String key;
  @Column(name="SECRET")
  private String secret;
  @Column(name="URL")
  private String url;
  @Column(name="RISK_RULE")
  private String riskRule;
  
  public String getKey() {
    return key;
  }
  public void setKey(String key) {
    this.key = key;
  }
  public String getSecret() {
    return secret;
  }
  public void setSecret(String secret) {
    this.secret = secret;
  }
  public String getUrl() {
    return url;
  }
  public void setUrl(String url) {
    this.url = url;
  }
  public String getRiskRule() {
    return riskRule;
  }
  public void setRiskRule(String riskRule) {
    this.riskRule = riskRule;
  }
  @Override
  protected boolean matchesClassAndId(Object other) {
    // TODO Auto-generated method stub
    return JpaSSPConfig.class.isInstance(other) ? matchesId((JpaSSPConfig)other) : false;
  }

}

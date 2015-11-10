/**
 * 
 */
package org.apereo.lap.model;

import java.io.Serializable;

import javax.persistence.Id;

/**
 * @author ggilbert
 *
 */
public class SSPConfig implements Serializable {

  private static final long serialVersionUID = 1L;
  
  @Id private String id;
  private String key;
  private String secret;
  private String url;
  private String riskRule;
  
  public String getId() {
    return id;
  }
  public void setId(String id) {
    this.id = id;
  }
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

}

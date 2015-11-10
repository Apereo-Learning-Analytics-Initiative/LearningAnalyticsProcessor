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
package org.apereo.lap.services.output.handlers;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apereo.lap.model.Output;
import org.apereo.lap.model.Output.OutputType;
import org.apereo.lap.model.SSPConfig;
import org.apereo.lap.services.storage.SSPConfigPersistentStorage;
import org.apereo.lap.services.storage.StorageFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.security.oauth2.client.DefaultOAuth2ClientContext;
import org.springframework.security.oauth2.client.OAuth2RestTemplate;
import org.springframework.security.oauth2.client.token.grant.client.ClientCredentialsResourceDetails;
import org.springframework.stereotype.Component;

/**
 * @author ggilbert
 *
 */
@Component
public class SSPEarlyAlertOutputHandler extends BaseOutputHandler {

  static final Logger logger = LoggerFactory.getLogger(SSPEarlyAlertOutputHandler.class);
  
  @Autowired
  private StorageFactory storageFactory;

  @Override
  public OutputType getHandledType() {
    return OutputType.SSPEARLYALERT;
  }
  
  class EarlyAlert implements Serializable {
    private static final long serialVersionUID = 1L;
    private String externalCourseId;
    private String externalStudentId;
    private String comment;
    private String riskCategory;
    private String jobId;
    
    public EarlyAlert(String externalCourseId, String externalStudentId, String comment, String riskCategory, String jobId) {
      super();
      this.externalCourseId = externalCourseId;
      this.externalStudentId = externalStudentId;
      this.comment = comment;
      this.riskCategory = riskCategory;
      this.jobId = jobId;
    }

    public String getExternalCourseId() {
      return externalCourseId;
    }

    public String getExternalStudentId() {
      return externalStudentId;
    }

    public String getComment() {
      return comment;
    }

    public String getRiskCategory() {
      return riskCategory;
    }
    
    public String getJobId() {
      return jobId;
    }
  }
  
  class EarlyAlertMessage implements Serializable {

    private static final long serialVersionUID = 1L;
    private String responseUrl;
    private String responseId;
    private List<EarlyAlert> jiscEarlyAlerts;
    
    public EarlyAlertMessage(String responseUrl, String responseId, List<EarlyAlert> jiscEarlyAlerts) {
      super();
      this.responseUrl = responseUrl;
      this.responseId = responseId;
      this.jiscEarlyAlerts = jiscEarlyAlerts;
    }

    public String getResponseUrl() {
      return responseUrl;
    }

    public String getResponseId() {
      return responseId;
    }

    public List<EarlyAlert> getJiscEarlyAlerts() {
      return jiscEarlyAlerts;
    }
    
  }

  @Override
  public OutputResult writeOutput(Output output) {
    logger.debug(output.toString());
    
    SSPConfigPersistentStorage sspConfigPersistentStorage = storageFactory.getSSPConfigPersistentStorage();
    SSPConfig sspConfig = sspConfigPersistentStorage.get();
    
    if (sspConfig == null) {
      throw new RuntimeException("No SSP Configuration");
    }
    
    ClientCredentialsResourceDetails resourceDetails = new ClientCredentialsResourceDetails();
    resourceDetails.setClientId(sspConfig.getKey());
    resourceDetails.setClientSecret(sspConfig.getSecret());
    
    String baseUrl = sspConfig.getUrl();
    if (!baseUrl.endsWith("/")) {
      baseUrl = baseUrl.concat("/");
    }
    
    resourceDetails.setAccessTokenUri(baseUrl + "ssp/api/1/oauth2/token");
    DefaultOAuth2ClientContext clientContext = new DefaultOAuth2ClientContext();

    OAuth2RestTemplate restTemplate = new OAuth2RestTemplate(resourceDetails, clientContext);
    MappingJackson2HttpMessageConverter converter = new MappingJackson2HttpMessageConverter();
    converter.setSupportedMediaTypes(Arrays.asList(MediaType.APPLICATION_JSON,
        MediaType.valueOf("text/javascript")));
    restTemplate.setMessageConverters(Arrays.<HttpMessageConverter<?>> asList(converter));

    OutputResult result = new OutputResult(output);

    String selectSQL = output.makeTempDBSelectSQL();

    SqlRowSet rowSet;
    try {
      rowSet = storage.getTempJdbcTemplate().queryForRowSet(selectSQL);
      
    } catch (Exception e) {
      throw new RuntimeException("Failure while trying to retrieve the output data set: " + selectSQL);
    }
    
    Map<String, Integer> riskMap = new HashMap<String, Integer>();
    riskMap.put("NO RISK",0);
    riskMap.put("LOW RISK",1);
    riskMap.put("MEDIUM RISK",2);
    riskMap.put("HIGH RISK",3);
    
    Integer riskThreshold = riskMap.get(sspConfig.getRiskRule());
    
    List<EarlyAlert> earlyAlertList = new ArrayList<SSPEarlyAlertOutputHandler.EarlyAlert>();
    while (rowSet.next()) {
      if (!rowSet.wasNull()) {
        
        String student = rowSet.getString(1);
        String course = rowSet.getString(2);
        String risk = rowSet.getString(3);
        
        Integer riskScore = riskMap.get(risk);
        
        if (riskScore >= riskThreshold) {
          EarlyAlert earlyAlert = new EarlyAlert(course, student, "Automated early alert due to risk score above acceptable limit", risk, null);
          earlyAlertList.add(earlyAlert);
        }
                
        logger.debug(String.format("student: %s, course: %s, risk:%s", student,course,risk));
      }
    }
    
    if (earlyAlertList.size() > 0) {
      EarlyAlertMessage message = new EarlyAlertMessage("test.com", "test", earlyAlertList);
      restTemplate.postForLocation(baseUrl + "ssp/api/1/bulkEarlyAlerts", message);
    }
    
    return result;
  }

}

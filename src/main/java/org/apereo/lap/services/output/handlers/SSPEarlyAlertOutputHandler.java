/**
 * 
 */
package org.apereo.lap.services.output.handlers;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apereo.lap.model.Output;
import org.apereo.lap.model.Output.OutputType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.security.oauth2.client.DefaultOAuth2ClientContext;
import org.springframework.security.oauth2.client.OAuth2RestTemplate;
import org.springframework.security.oauth2.client.token.grant.client.ClientCredentialsResourceDetails;
import org.springframework.security.oauth2.client.token.grant.password.ResourceOwnerPasswordResourceDetails;
import org.springframework.stereotype.Component;

/**
 * @author ggilbert
 *
 */
@Component
public class SSPEarlyAlertOutputHandler extends BaseOutputHandler {

  static final Logger logger = LoggerFactory.getLogger(SSPEarlyAlertOutputHandler.class);
  
  @Value("${ssp.clientId:@null}")
  private String clientId;
  @Value("${ssp.clientSecret:@null}")
  private String clientSecret;
  @Value("${ssp.accessTokenUri:@null}")
  private String accessTokenUri;
  @Value("${ssp.earlyAlertUrl:@null}")
  private String earlyAlertUrl;

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
    
    public EarlyAlert(String externalCourseId, String externalStudentId, String comment, String riskCategory) {
      super();
      this.externalCourseId = externalCourseId;
      this.externalStudentId = externalStudentId;
      this.comment = comment;
      this.riskCategory = riskCategory;
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
    
    ClientCredentialsResourceDetails resourceDetails = new ClientCredentialsResourceDetails();
    resourceDetails.setClientId(clientId);
    resourceDetails.setClientSecret(clientSecret);
    resourceDetails.setAccessTokenUri(accessTokenUri);
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

    List<EarlyAlert> earlyAlertList = new ArrayList<SSPEarlyAlertOutputHandler.EarlyAlert>();
    while (rowSet.next()) {
      if (!rowSet.wasNull()) {
        
        String student = rowSet.getString(1);
        String course = rowSet.getString(2);
        String risk = rowSet.getString(3);
        
        if (StringUtils.isNotBlank(risk) && "HIGH RISK".equals(risk)) {
          EarlyAlert earlyAlert = new EarlyAlert(course, student, "Automated early alert due to risk score above acceptable limit", risk);
          earlyAlertList.add(earlyAlert);
        }
                
        logger.debug(String.format("student: %s, course: %s, risk:%s", student,course,risk));
      }
    }
    
    if (earlyAlertList.size() > 0) {
      EarlyAlertMessage message = new EarlyAlertMessage("test.com", "test", earlyAlertList);
      restTemplate.postForLocation(earlyAlertUrl, message);
    }
    
    return result;
  }

}

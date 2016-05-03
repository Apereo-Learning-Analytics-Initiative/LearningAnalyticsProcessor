/**
 * 
 */
package org.apereo.lap.services.output.handlers;

import gov.adlnet.xapi.client.StatementClient;
import gov.adlnet.xapi.model.Account;
import gov.adlnet.xapi.model.Activity;
import gov.adlnet.xapi.model.ActivityDefinition;
import gov.adlnet.xapi.model.Agent;
import gov.adlnet.xapi.model.Context;
import gov.adlnet.xapi.model.ContextActivities;
import gov.adlnet.xapi.model.Result;
import gov.adlnet.xapi.model.Statement;
import gov.adlnet.xapi.model.Verb;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

import org.apereo.lap.model.Output;
import org.apereo.lap.model.Output.OutputType;
import org.codehaus.plexus.util.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.stereotype.Component;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

/**
 * @author ggilbert
 *
 */
@Component
public class XApiEarlyAlertOutputHandler extends BaseOutputHandler {

  static final Logger logger = LoggerFactory.getLogger(XApiEarlyAlertOutputHandler.class);
  
  private static final String EN_US = "en-US";
  private static final String EN_GB = "en-GB";
  
  private static final String XAPI_VERSION = "1.0.0";
  
  @Value("${xapi.lrs.endpoint:#{null}}")
  private String lrsEndpoint;
  
  @Value("${xapi.lrs.username:#{null}}")
  private String lrsUsername;
  
  @Value("${xapi.lrs.password:#{null}}")
  private String lrsPassword;

  @Override
  public OutputType getHandledType() {
    return OutputType.XAPIEARLYALERT;  
  }

  @Override
  public OutputResult writeOutput(Output output) {
    OutputResult results = new OutputResult(output);
    
    if (StringUtils.isBlank(lrsEndpoint) || 
        StringUtils.isBlank(lrsUsername) ||
        StringUtils.isBlank(lrsPassword)) {
      return results;
    }

    String selectSQL = output.makeTempDBSelectSQL();
    logger.debug("SQL: {}", selectSQL);
    
    SqlRowSet rowSet;
    try {
      rowSet = storage.getTempJdbcTemplate().queryForRowSet(selectSQL);
    } 
    catch (Exception e) {
      throw new RuntimeException("Failure while trying to retrieve the output data set: " + selectSQL);
    }
    
    while (rowSet.next()) {
      if (!rowSet.wasNull()) {
                
        StatementClient client;
        try {
          String student = rowSet.getString(1);
          String course = rowSet.getString(2);
          String risk = rowSet.getString(3);
          
          Statement statement = new Statement();
          statement.setVersion(XAPI_VERSION);
          statement.setTimestamp(DateTimeFormatter.ISO_OFFSET_DATE_TIME.withZone(ZoneId.of("UTC")).format(Instant.now()));
          
          Account account = new Account();
          account.setName(student);
          
          Agent agent = new Agent();
          account.setHomePage("https://github.com/jiscdev/analytics-udd/blob/master/predictive-core.md#student_id");
          agent.setAccount(account);

          statement.setActor(agent);
          statement.setVerb(new Verb(
                  "http://activitystrea.ms/schema/1.0/receive",
                  new MapBuilder<String>()
                  .put(EN_GB, "receive")
                  .put(EN_US, "receive").build()
          ));
          
          statement.setObject(createActivity(student+course));
          statement.setContext(createContext(course));

          // build just enough of the structure of the result to serve as a placeholder.
          // we'll update the result contents as we iterate over records.
          Result result = new Result();
          result.setExtensions(createResult(risk, 0));
          statement.setResult(result);

          logger.debug(String.format("student: %s, course: %s, risk:%s", student,course,risk));

          client = new StatementClient(lrsEndpoint, lrsUsername, lrsPassword);
          client.postStatement(statement);
        } catch (Exception e) {
          // TODO Auto-generated catch block
          e.printStackTrace();
        } 
      }
    }

    return results;
  }
  
  private JsonObject createResult(String score, float metric) {
    JsonObject obj = new JsonObject();
    obj.add("https://lap.jisc.ac.uk/earlyAlert/score", new JsonPrimitive(score));
    // NOTE: spec indicates metric is a string, *not* a JSON primitive float
    //obj.add("https://lap.jisc.ac.uk/earlyAlert/metricN", new JsonPrimitive(String.valueOf(metric)));
    return obj;
  }

  private Activity createActivity(String alertId) {
    Activity a = new Activity();
    a.setId("https://lap.jisc.ac.uk/earlyAlert/unicon/"+alertId);
    ActivityDefinition ad = new ActivityDefinition();
    ad.setType("http://activitystrea.ms/schema/1.0/alert");
    ad.setName(new MapBuilder<String>().put(EN_GB, "An early alert").put(EN_US, "An early alert").build());
    ad.setDescription(new MapBuilder<String>().put(EN_GB, "An early alert").put(EN_US, "An early alert").build());

    ad.setExtensions(new MapBuilder<JsonElement>().put("https://lap.jisc.ac.uk/earlyAlert/type", new JsonPrimitive("UNICON")).build());
    a.setDefinition(ad);
    return a;
}

private Context createContext(String contextId) {
    ActivityDefinition ad = new ActivityDefinition();
    ad.setType("http://adlnet.gov/expapi/activities/module");
    ad.setDescription(new MapBuilder<String>().put(EN_US, "Context").build());
    ad.setName(new MapBuilder<String>().put(EN_US, "Context").build());
    //ad.setExtensions(new MapBuilder<JsonElement>().put("https://lap.jisc.ac.uk/taxonomy", new JsonPrimitive("MOD_INSTANCE")).build());
    Activity activity = new Activity(contextId, ad);
    ContextActivities ca = new ContextActivities();
    ca.setGrouping(new ArrayList<Activity>(Collections.singletonList(activity)));
    Context context = new Context();
    context.setContextActivities(ca);
    return context;
}
  
  private static class MapBuilder<V> {
    private HashMap<String, V> m = new HashMap<String, V>();

    public MapBuilder<V> put(String key, V value) {
        m.put(key, value);
        return this;
    }

    public HashMap<String, V> build() {
        return m;
    }
}
}

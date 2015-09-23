package org.apereo.lap.controllers;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Date;
import java.util.List;

import org.apereo.lap.services.storage.h2.model.RiskConfidence;
import org.apereo.lap.services.storage.h2.model.RiskConfidenceRepository;
import org.apereo.lap.test.AbstractIntegrationTest;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.web.config.EnableSpringDataWebSupport;
import org.springframework.http.MediaType;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

@WebAppConfiguration
@EnableSpringDataWebSupport
public class ModelOutputControllerTest extends AbstractIntegrationTest{

    @Autowired 
    private WebApplicationContext context;
    @Autowired
    private RiskConfidenceRepository riskConfidenceRepository;
    private  MockMvc mvc;
    private Date testDate = new Date();
    
    @Before
    public void setup(){
        mvc = MockMvcBuilders.webAppContextSetup(context).build();
        List<RiskConfidence> rc = riskConfidenceRepository.findAll();
        if (rc != null) {
          riskConfidenceRepository.deleteAll();
        }
        
        logger.debug("RiskConfidenceRepo should be empty on init of test: " + riskConfidenceRepository.findAll());
    }
    
    //1
    @Test
    public void outputWillUsePageableDefaultWhenGivenNone() throws Exception{
        String DEFAULT_PAGE = "0";
        String DEFAULT_SIZE = "100";
        MvcResult result = mvc.perform(get("/api/output/sometenant").accept(MediaType.parseMediaType("application/json")))
                .andExpect(status().isOk())
                .andReturn();

            String actualResultContent = result.getResponse().getContentAsString();
            //logger.info("json content: {}", actualResultContent);
            String expectedResultContent = "{\"links\":[{\"rel\":\"self\",\"href\":\"http://localhost/api/output/sometenant{?page,size,sort}\"}],"+
                                           "\"content\":[],\"page\":{\"size\":"+DEFAULT_SIZE+",\"totalElements\":0,\"totalPages\":0,\"number\":"+DEFAULT_PAGE+"}}";
            assertEquals( expectedResultContent, actualResultContent);
    }
    
    //2
    //@Test
    public void outputWillUsePageableDefaultWhenGivenInvalidSizes() throws Exception{
      String DEFAULT_PAGE = "0";
      String DEFAULT_SIZE = "100";
      String PAGE = "-1";
      String SIZE = "0";
      MvcResult result = mvc.perform(get("/api/output/sometenant?page="+PAGE+"&size="+SIZE).accept(MediaType.parseMediaType("application/json")))
              .andExpect(status().isOk())
              .andReturn();

          String actualResultContent = result.getResponse().getContentAsString();
          //logger.info("json content: {}", actualResultContent);
          String expectedResultContent = "{\"links\":[{\"rel\":\"self\",\"href\":\"http://localhost/api/output/sometenant{?page,size,sort}\"}],"+
                                         "\"content\":[],\"page\":{\"size\":"+DEFAULT_SIZE+",\"totalElements\":0,\"totalPages\":0,\"number\":"+DEFAULT_PAGE+"}}";
          assertEquals(expectedResultContent, actualResultContent);
  }

    //3
    //@Test
    public void outputWillSetPreviousPageToOneLessThanCurrentPageForValidInput() throws Exception{
        String PAGE = "10";
        String PREV_PAGE = "9";
        String SIZE = "50";
        MvcResult result = mvc.perform(get("/api/output/sometenant?page="+PAGE+"&size="+SIZE).accept(MediaType.parseMediaType("application/json")))
                .andExpect(status().isOk())
                .andReturn();

            String actualResultContent = result.getResponse().getContentAsString();
            //logger.info("json content: {}", actualResultContent);
            String expectedResultContent = "{\"links\":[{\"rel\":\"self\",\"href\":\"http://localhost/api/output/sometenant{?page,size,sort}\"},"+
                                           "{\"rel\":\"prev\",\"href\":\"http://localhost/api/output/sometenant?page="+PREV_PAGE+"&size="+50+"{&sort}\"}],"+
                                           "\"content\":[],\"page\":{\"size\":"+SIZE+",\"totalElements\":0,\"totalPages\":0,\"number\":"+PAGE+"}}";
            assertEquals(expectedResultContent, actualResultContent);
    }


    //4
    @Test
    public void outputWillReturnEmptyContentWhenDataDoesNotExist() throws Exception{
        MvcResult result = mvc.perform(get("/api/output/sometenant").accept(MediaType.parseMediaType("application/json")))
                .andExpect(status().isOk())
                .andReturn();

            String actualResultContent = result.getResponse().getContentAsString();
            //logger.info("json content: {}", actualResultContent);
           assertTrue(actualResultContent.contains("\"content\":[]"));
    }

    //5
    @Test
    public void outputWillReturnExpectedContentWhenDataExists() throws Exception{
        RiskConfidence expected = addTestDataToRiskConfidenceThatWillBeReturnByQueryToRepository();
        String DEFAULT_PAGE = "0";
        String DEFAULT_SIZE = "100";
        MvcResult result = mvc.perform(get("/api/output/sometenant").accept(MediaType.parseMediaType("application/json")))
                .andExpect(status().isOk())
                .andReturn();

        String actualResultContent = result.getResponse().getContentAsString();
        //logger.info("json content: {}", actualResultContent);
        String expectedResultContent = "{\"links\":[{\"rel\":\"self\",\"href\":\"http://localhost/api/output/sometenant{?page,size,sort}\"}],"+
                "\"content\":[{\"id\":\""+expected.getId()+"\",\"risk_score\":\"HIGH\",\"created_date\":"+expected.getDateCreated().getTime()+
                ",\"model_run_id\":\"g1\",\"student_id\":\"a1\",\"course_id\":\"c1\",\"links\":[]}],"+
                "\"page\":{\"size\":"+DEFAULT_SIZE+",\"totalElements\":1,\"totalPages\":1,\"number\":"+DEFAULT_PAGE+"}}";
        assertEquals(expectedResultContent, actualResultContent);
    }
    
    //1
    @Test
    public void outputByStudentWillUsePageableDefaultWhenGivenNone() throws Exception{
        String DEFAULT_PAGE = "0";
        String DEFAULT_SIZE = "100";
        MvcResult result = mvc.perform(get("/api/output/sometenant/student/1").accept(MediaType.parseMediaType("application/json")))
                .andExpect(status().isOk())
                .andReturn();

            String actualResultContent = result.getResponse().getContentAsString();
            //logger.info("json content: {}", actualResultContent);
            String expectedResultContent = "{\"links\":[{\"rel\":\"self\",\"href\":\"http://localhost/api/output/sometenant/student/1{?page,size,sort}\"}],"+
                                           "\"content\":[],\"page\":{\"size\":"+DEFAULT_SIZE+",\"totalElements\":0,\"totalPages\":0,\"number\":"+DEFAULT_PAGE+"}}";
            assertEquals( expectedResultContent, actualResultContent);
    }

    //2
    @Test
    public void outputByStudentWillUsePageableDefaultWhenGivenInvalidSizes() throws Exception{
        String DEFAULT_PAGE = "0";
        String DEFAULT_SIZE = "100";
        String PAGE = "-1";
        String SIZE = "0";
        MvcResult result = mvc.perform(get("/api/output/sometenant/student/1?page="+PAGE+"&size="+SIZE).accept(MediaType.parseMediaType("application/json")))
                .andExpect(status().isOk())
                .andReturn();

            String actualResultContent = result.getResponse().getContentAsString();
            //logger.info("json content: {}", actualResultContent);
            String expectedResultContent = "{\"links\":[{\"rel\":\"self\",\"href\":\"http://localhost/api/output/sometenant/student/1?page="+PAGE+"&size="+SIZE+"{&sort}\"}],"+
                                           "\"content\":[],\"page\":{\"size\":"+DEFAULT_SIZE+",\"totalElements\":0,\"totalPages\":0,\"number\":"+DEFAULT_PAGE+"}}";
            assertEquals(expectedResultContent, actualResultContent);
    }
    
    //3
    @Test
    public void outputByStudentWillSetPreviousPageToOneLessThanCurrentPageForValidInput() throws Exception{
        String PAGE = "10";
        String PREV_PAGE = "9";
        String SIZE = "50";
        MvcResult result = mvc.perform(get("/api/output/sometenant/student/1?page="+PAGE+"&size="+SIZE).accept(MediaType.parseMediaType("application/json")))
                .andExpect(status().isOk())
                .andReturn();

            String actualResultContent = result.getResponse().getContentAsString();
            //logger.info("json content: {}", actualResultContent);
            String expectedResultContent = "{\"links\":[{\"rel\":\"self\",\"href\":\"http://localhost/api/output/sometenant/student/1?page="+PAGE+"&size="+SIZE+"{&sort}\"},"+
                                           "{\"rel\":\"prev\",\"href\":\"http://localhost/api/output/sometenant/student/1?page="+PREV_PAGE+"&size="+50+"{&sort}\"}],"+
                                           "\"content\":[],\"page\":{\"size\":"+SIZE+",\"totalElements\":0,\"totalPages\":0,\"number\":"+PAGE+"}}";
            assertEquals(expectedResultContent, actualResultContent);
    }

    //4
    @Test
    public void outputByStudentWillReturnEmptyContentWhenDataDoesNotExist() throws Exception{
        MvcResult result = mvc.perform(get("/api/output/sometenant/student/1").accept(MediaType.parseMediaType("application/json")))
                .andExpect(status().isOk())
                .andReturn();

            String actualResultContent = result.getResponse().getContentAsString();
            //logger.info("json content: {}", actualResultContent);
           assertTrue(actualResultContent.contains("\"content\":[]"));
    }

    //5
    @Test
    public void outputByStudentWillReturnExpectedContentWhenDataExists() throws Exception{
        RiskConfidence expected = addTestDataToRiskConfidenceThatWillBeReturnByQueryToRepository();
        RiskConfidence shouldNotContainThisRiskConfidence = addTestDataToRiskConfidenceThatWillBeFilteredAndNotReturnedByQueryToRepository();
        String DEFAULT_PAGE = "0";
        String DEFAULT_SIZE = "100";
        MvcResult result = mvc.perform(get("/api/output/sometenant/student/a1").accept(MediaType.parseMediaType("application/json")))
                .andExpect(status().isOk())
                .andReturn();

        String actualResultContent = result.getResponse().getContentAsString();
        //logger.info("json content: {}", actualResultContent);
        String expectedResultContent = "{\"links\":[{\"rel\":\"self\",\"href\":\"http://localhost/api/output/sometenant/student/a1{?page,size,sort}\"}],"+
                "\"content\":[{\"id\":\""+expected.getId()+"\",\"risk_score\":\"HIGH\",\"created_date\":"+expected.getDateCreated().getTime()+
                ",\"model_run_id\":\"g1\",\"student_id\":\"a1\",\"course_id\":\"c1\",\"links\":[]}],"+
                "\"page\":{\"size\":"+DEFAULT_SIZE+",\"totalElements\":1,\"totalPages\":1,\"number\":"+DEFAULT_PAGE+"}}";
        assertEquals(expectedResultContent, actualResultContent);
        //H2 converts student Id to alternative id for query
        assertTrue(!actualResultContent.contains(shouldNotContainThisRiskConfidence.getAlternativeId()));
    }
    
    
    //1
    @Test
    public void outputByCourseWillUsePageableDefaultWhenGivenNone() throws Exception{
        String DEFAULT_PAGE = "0";
        String DEFAULT_SIZE = "100";
        MvcResult result = mvc.perform(get("/api/output/sometenant/course/c1").accept(MediaType.parseMediaType("application/json")))
                .andExpect(status().isOk())
                .andReturn();

            String actualResultContent = result.getResponse().getContentAsString();
            //logger.info("json content: {}", actualResultContent);
            String expectedResultContent = "{\"links\":[{\"rel\":\"self\",\"href\":\"http://localhost/api/output/sometenant/course/c1{?page,size,sort}\"}],"+
                                           "\"content\":[],\"page\":{\"size\":"+DEFAULT_SIZE+",\"totalElements\":0,\"totalPages\":0,\"number\":"+DEFAULT_PAGE+"}}";
            assertEquals( expectedResultContent, actualResultContent);
    }

    //2
    @Test
    public void outputByCourseWillUsePageableDefaultWhenGivenInvalidSizes() throws Exception{
        String DEFAULT_PAGE = "0";
        String DEFAULT_SIZE = "100";
        String PAGE = "-1";
        String SIZE = "0";
        MvcResult result = mvc.perform(get("/api/output/sometenant/course/c1?page="+PAGE+"&size="+SIZE).accept(MediaType.parseMediaType("application/json")))
                .andExpect(status().isOk())
                .andReturn();

            String actualResultContent = result.getResponse().getContentAsString();
            //logger.info("json content: {}", actualResultContent);
            String expectedResultContent = "{\"links\":[{\"rel\":\"self\",\"href\":\"http://localhost/api/output/sometenant/course/c1?page="+PAGE+"&size="+SIZE+"{&sort}\"}],"+
                                           "\"content\":[],\"page\":{\"size\":"+DEFAULT_SIZE+",\"totalElements\":0,\"totalPages\":0,\"number\":"+DEFAULT_PAGE+"}}";
            assertEquals(expectedResultContent, actualResultContent);
    }
    
    //3
    @Test
    public void outputByCourseWillSetPreviousPageToOneLessThanCurrentPageForValidInput() throws Exception{
        String PAGE = "10";
        String PREV_PAGE = "9";
        String SIZE = "50";
        MvcResult result = mvc.perform(get("/api/output/sometenant/course/c1?page="+PAGE+"&size="+SIZE).accept(MediaType.parseMediaType("application/json")))
                .andExpect(status().isOk())
                .andReturn();

            String actualResultContent = result.getResponse().getContentAsString();
            //logger.info("json content: {}", actualResultContent);
            String expectedResultContent = "{\"links\":[{\"rel\":\"self\",\"href\":\"http://localhost/api/output/sometenant/course/c1?page="+PAGE+"&size="+SIZE+"{&sort}\"},"+
                                           "{\"rel\":\"prev\",\"href\":\"http://localhost/api/output/sometenant/course/c1?page="+PREV_PAGE+"&size="+50+"{&sort}\"}],"+
                                           "\"content\":[],\"page\":{\"size\":"+SIZE+",\"totalElements\":0,\"totalPages\":0,\"number\":"+PAGE+"}}";
            assertEquals(expectedResultContent, actualResultContent);
    }
  

    //4
    @Test
    public void outputByCourseWillReturnEmptyContentWhenDataDoesNotExist() throws Exception{
        MvcResult result = mvc.perform(get("/api/output/sometenant/course/c1").accept(MediaType.parseMediaType("application/json")))
                .andExpect(status().isOk())
                .andReturn();

            String actualResultContent = result.getResponse().getContentAsString();
            //logger.info("json content: {}", actualResultContent);
           assertTrue(actualResultContent.contains("\"content\":[]"));
    }

    //5
    @Test
    public void outputByCourseWillReturnExpectedContentWhenDataExists() throws Exception{
        RiskConfidence expected = addTestDataToRiskConfidenceThatWillBeReturnByQueryToRepository();
        RiskConfidence shouldNotContainThisRiskConfidence = addTestDataToRiskConfidenceThatWillBeFilteredAndNotReturnedByQueryToRepository();
        String DEFAULT_PAGE = "0";
        String DEFAULT_SIZE = "100";
        MvcResult result = mvc.perform(get("/api/output/sometenant/course/c1").accept(MediaType.parseMediaType("application/json")))
                .andExpect(status().isOk())
                .andReturn();

        String actualResultContent = result.getResponse().getContentAsString();
        logger.info("json content: {}", actualResultContent);
        String expectedResultContent = "{\"links\":[{\"rel\":\"self\",\"href\":\"http://localhost/api/output/sometenant/course/c1{?page,size,sort}\"}],"+
                "\"content\":[{\"id\":\""+expected.getId()+"\",\"risk_score\":\"HIGH\",\"created_date\":"+expected.getDateCreated().getTime()+
                ",\"model_run_id\":\"g1\",\"student_id\":\"a1\",\"course_id\":\"c1\",\"links\":[]}],"+
                "\"page\":{\"size\":"+DEFAULT_SIZE+",\"totalElements\":1,\"totalPages\":1,\"number\":"+DEFAULT_PAGE+"}}";
        assertEquals(expectedResultContent, actualResultContent);
        //H2 converts student Id to alternative id for query
        assertTrue(!actualResultContent.contains(shouldNotContainThisRiskConfidence.getCourseId()));
    }
    
    //1
    @Test
    public void outputByStudentAndCourseWillUsePageableDefaultWhenGivenNone() throws Exception{
        String DEFAULT_PAGE = "0";
        String DEFAULT_SIZE = "100";
        MvcResult result = mvc.perform(get("/api/output/sometenant/course/c1/student/a1").accept(MediaType.parseMediaType("application/json")))
                .andExpect(status().isOk())
                .andReturn();
        
        String actualResultContent = result.getResponse().getContentAsString();
        //logger.info("json content: {}", actualResultContent);
        String expectedResultContent = "{\"links\":[{\"rel\":\"self\",\"href\":\"http://localhost/api/output/sometenant/course/c1/student/a1{?page,size,sort}\"}],"+
                "\"content\":[],\"page\":{\"size\":"+DEFAULT_SIZE+",\"totalElements\":0,\"totalPages\":0,\"number\":"+DEFAULT_PAGE+"}}";
        assertEquals( expectedResultContent, actualResultContent);
    }
    
    //2
    @Test
    public void outputByStudentAndCourseWillUsePageableDefaultWhenGivenInvalidSizes() throws Exception{
        String DEFAULT_PAGE = "0";
        String DEFAULT_SIZE = "100";
        String PAGE = "-1";
        String SIZE = "0";
        MvcResult result = mvc.perform(get("/api/output/sometenant/course/c1/student/a1?page="+PAGE+"&size="+SIZE).accept(MediaType.parseMediaType("application/json")))
                .andExpect(status().isOk())
                .andReturn();
        
        String actualResultContent = result.getResponse().getContentAsString();
        //logger.info("json content: {}", actualResultContent);
        String expectedResultContent = "{\"links\":[{\"rel\":\"self\",\"href\":\"http://localhost/api/output/sometenant/course/c1/student/a1?page="+PAGE+"&size="+SIZE+"{&sort}\"}],"+
                "\"content\":[],\"page\":{\"size\":"+DEFAULT_SIZE+",\"totalElements\":0,\"totalPages\":0,\"number\":"+DEFAULT_PAGE+"}}";
        assertEquals(expectedResultContent, actualResultContent);
    }
    
    //3
    @Test
    public void outputByStudentAndCourseWillSetPreviousPageToOneLessThanCurrentPageForValidInput() throws Exception{
        String PAGE = "10";
        String PREV_PAGE = "9";
        String SIZE = "50";
        MvcResult result = mvc.perform(get("/api/output/sometenant/course/c1/student/a1?page="+PAGE+"&size="+SIZE).accept(MediaType.parseMediaType("application/json")))
                .andExpect(status().isOk())
                .andReturn();
        
        String actualResultContent = result.getResponse().getContentAsString();
        //logger.info("json content: {}", actualResultContent);
        String expectedResultContent = "{\"links\":[{\"rel\":\"self\",\"href\":\"http://localhost/api/output/sometenant/course/c1/student/a1?page="+PAGE+"&size="+SIZE+"{&sort}\"},"+
                "{\"rel\":\"prev\",\"href\":\"http://localhost/api/output/sometenant/course/c1/student/a1?page="+PREV_PAGE+"&size="+50+"{&sort}\"}],"+
                "\"content\":[],\"page\":{\"size\":"+SIZE+",\"totalElements\":0,\"totalPages\":0,\"number\":"+PAGE+"}}";
        assertEquals(expectedResultContent, actualResultContent);
    }
    
    //4
    @Test
    public void outputByStudentAndCourseWillReturnEmptyContentWhenDataDoesNotExist() throws Exception{
        MvcResult result = mvc.perform(get("/api/output/sometenant/course/c1/student/a1").accept(MediaType.parseMediaType("application/json")))
                .andExpect(status().isOk())
                .andReturn();
        
        String actualResultContent = result.getResponse().getContentAsString();
        //logger.info("json content: {}", actualResultContent);
        assertTrue(actualResultContent.contains("\"content\":[]"));
    }
    
    //5
    @Test
    public void outputByStudentAndCourseWillReturnExpectedContentWhenDataExists() throws Exception{
        RiskConfidence expected = addTestDataToRiskConfidenceThatWillBeReturnByQueryToRepository();
        RiskConfidence shouldNotContainThisRiskConfidence = addTestDataToRiskConfidenceThatWillBeFilteredAndNotReturnedByQueryToRepository();
        RiskConfidence shouldNotContainThisRiskConfidence_NotCorrectCourseId = addTestDataToRiskConfidenceThatWillNotContainCourseButWillContainStudentId();
        RiskConfidence shouldNotContainThisRiskConfidence_NotCorrectStudentId = addTestDataToRiskConfidenceThatWillContainCourseButWillNotContainStudentId();
        
        String DEFAULT_PAGE = "0";
        String DEFAULT_SIZE = "100";
        MvcResult result = mvc.perform(get("/api/output/sometenant/course/c1/student/a1").accept(MediaType.parseMediaType("application/json")))
                .andExpect(status().isOk())
                .andReturn();
        
        String actualResultContent = result.getResponse().getContentAsString();
        //logger.info("json content: {}", actualResultContent);
        String expectedResultContent = "{\"links\":[{\"rel\":\"self\",\"href\":\"http://localhost/api/output/sometenant/course/c1/student/a1{?page,size,sort}\"}],"+
                "\"content\":[{\"id\":\""+expected.getId()+"\",\"risk_score\":\"HIGH\",\"created_date\":"+expected.getDateCreated().getTime()+
                ",\"model_run_id\":\"g1\",\"student_id\":\"a1\",\"course_id\":\"c1\",\"links\":[]}],"+
                "\"page\":{\"size\":"+DEFAULT_SIZE+",\"totalElements\":1,\"totalPages\":1,\"number\":"+DEFAULT_PAGE+"}}";
        assertEquals(expectedResultContent, actualResultContent);
        //H2 converts student Id to alternative id for query
        assertTrue(!actualResultContent.contains(shouldNotContainThisRiskConfidence.getCourseId()));
        assertTrue(!actualResultContent.contains(shouldNotContainThisRiskConfidence.getAlternativeId()));
        assertTrue(!actualResultContent.contains(shouldNotContainThisRiskConfidence_NotCorrectCourseId.getCourseId()));
        assertTrue(!actualResultContent.contains(shouldNotContainThisRiskConfidence_NotCorrectStudentId.getAlternativeId()));
    }

    private RiskConfidence addTestDataToRiskConfidenceThatWillBeReturnByQueryToRepository(){
        RiskConfidence rc = new RiskConfidence();
        rc.setAlternativeId("a1");
        rc.setCourseId("c1");
        rc.setGroupId("g1");
        rc.setModelRiskConfidence("HIGH");
        rc.setDateCreated(testDate);
        riskConfidenceRepository.save(rc);
        return rc;
    }
    
    private RiskConfidence addTestDataToRiskConfidenceThatWillBeFilteredAndNotReturnedByQueryToRepository(){
        RiskConfidence rc = new RiskConfidence();
        rc.setAlternativeId("filter_a_2");
        rc.setCourseId("filter_c_2");
        rc.setGroupId("filter_g_1");
        rc.setModelRiskConfidence("LOW");
        rc.setDateCreated(testDate);
        riskConfidenceRepository.save(rc);
        return rc;
    }
    
    private RiskConfidence addTestDataToRiskConfidenceThatWillNotContainCourseButWillContainStudentId(){
        RiskConfidence rc = new RiskConfidence();
        rc.setAlternativeId("a1");
        rc.setCourseId("notCourse1");
        rc.setGroupId("anyGroupId1");
        rc.setModelRiskConfidence("andRisk1");
        rc.setDateCreated(testDate);
        riskConfidenceRepository.save(rc);
        return rc;
    }
    
    private RiskConfidence addTestDataToRiskConfidenceThatWillContainCourseButWillNotContainStudentId(){
        RiskConfidence rc = new RiskConfidence();
        rc.setAlternativeId("notStudent1");
        rc.setCourseId("c1");
        rc.setGroupId("anyGroupId1");
        rc.setModelRiskConfidence("andRisk1");
        rc.setDateCreated(testDate);
        riskConfidenceRepository.save(rc);
        return rc;
    }
}

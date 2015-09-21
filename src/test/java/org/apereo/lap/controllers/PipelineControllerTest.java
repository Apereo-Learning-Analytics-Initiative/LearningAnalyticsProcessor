package org.apereo.lap.controllers;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apereo.lap.model.PipelineConfig;
import org.apereo.lap.model.PipelineConfig.InputField;
import org.apereo.lap.services.ProcessingManagerService;
import org.apereo.lap.test.AbstractUnitTest;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.ResponseBody;

public class PipelineControllerTest extends AbstractUnitTest{

    private static final Logger logger = LoggerFactory.getLogger(PipelineControllerTest.class);
    @Mock
    ProcessingManagerService processingManagerService;
    @Mock
    PipelineConfig pipelineConfig;
    @Mock
    InputField inputField; 
    @InjectMocks
    PipelineController pipelineController;
    
    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
    }

    @After
    public void validate() {
        Mockito.validateMockitoUsage();
    }

    @Test
    public void rootGetWillReturnMapOfPipeLineConfigsWhenNotNullAndNotEmpty(){
        Map<String, PipelineConfig> testPipelines = new ConcurrentHashMap<String, PipelineConfig>();
        testPipelines.put("test_input", pipelineConfig);
        Map<String, Object> expectedData = new LinkedHashMap<>();
        Map<String, Object> actualData = new LinkedHashMap<>();
        List<PipelineConfig> procs = new ArrayList<>();
        when(processingManagerService.getPipelineConfigs()).thenReturn(testPipelines);
        
        for (PipelineConfig pipelineProcessor : testPipelines.values()) {
            procs.add(pipelineProcessor);
        }
        expectedData = createExpectedResponseBody(procs);
        
        actualData = pipelineController.rootGet();
        
        assertEquals(expectedData, actualData);
    }

    @Test
    public void getRootWillReturnResponseBodyWithEmptyListWhenGetPipelineConfigsReturnsNull(){
        Map<String, PipelineConfig> testPipelines = null;
        Map<String, Object> expectedData = new LinkedHashMap<>();
        Map<String, Object> actualData = new LinkedHashMap<>();
        List<PipelineConfig> emptyList = new ArrayList<>();
        when(processingManagerService.getPipelineConfigs()).thenReturn(testPipelines);
        
        expectedData = createExpectedResponseBody(emptyList);
        
        actualData = pipelineController.rootGet();
        
        assertEquals(expectedData, actualData);
    }

    @Test
    public void getRootWillReturnResponseBodyWithEmptyListWhenGetPipelineConfigsReturnsEmptyMap(){
        Map<String, PipelineConfig> testPipelines = new ConcurrentHashMap<String, PipelineConfig>();
        Map<String, Object> expectedData = new LinkedHashMap<>();
        Map<String, Object> actualData = new LinkedHashMap<>();
        List<PipelineConfig> emptyList = new ArrayList<>();
        when(processingManagerService.getPipelineConfigs()).thenReturn(testPipelines);
        
        expectedData = createExpectedResponseBody(emptyList);
        
        actualData = pipelineController.rootGet();
        
        assertEquals(expectedData, actualData);
    }

    @Test
    public void getTypeWillReturnPipelineConfigWhenGivenKnownType(){
        when(processingManagerService.findPipelineConfig("knownType")).thenReturn(pipelineConfig);
        PipelineConfig actualValue = pipelineController.getType("knownType");

        assertNotNull(actualValue);
    }

    @Test(expected = IllegalArgumentException.class)
    public void getTypeWillThrowIllegalArgumentExceptionWhenGivenAnUnknownType(){
        assertEquals(null, pipelineController.getType("unknown"));
    }

    @Test
    public void startWillCallProcessManagerServiceProcessWithTypeWhenGivenAType(){
        pipelineController.start("type");
        
        verify(processingManagerService, times(1)).process("type", null);
    }

    private @ResponseBody Map<String, Object> createExpectedResponseBody(List<PipelineConfig> procs){
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("processors", procs);
        return data;
    }

}

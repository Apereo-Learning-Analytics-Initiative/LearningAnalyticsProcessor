/* Copyright 2013 Unicon (R) Licensed under the
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
package org.apereo.lap.services;

import static org.mockito.Matchers.anyObject;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.concurrent.ConcurrentHashMap;

import org.apereo.lap.model.PipelineConfig;
import org.apereo.lap.services.configuration.ConfigurationService;
import org.apereo.lap.services.notification.NotificationService;
import org.apereo.lap.services.output.OutputHandlerService;
import org.apereo.lap.services.storage.ModelRun;
import org.apereo.lap.services.storage.ModelRunPersistentStorage;
import org.apereo.lap.services.storage.StorageFactory;
import org.apereo.lap.services.storage.StorageService;
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

public class ProcessingManagerServiceTest extends AbstractUnitTest{
    private static final Logger logger = LoggerFactory.getLogger(ProcessingManagerServiceTest.class);
    @Mock
    ConfigurationService configuration;
    @Mock 
    NotificationService notification;
    @Mock
    PipelineConfig pipelineConfig;
    @Mock
    ModelRunPersistentStorage modelRunPersistentStorage;
    @InjectMocks
    ProcessingManagerService processingManagerService;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
    }

    @After
    public void validate() {
        Mockito.validateMockitoUsage();
    }

    @Test
    public void findPipelineConfigWillCallConfigurationGetPipeLineConfigWithGivenIdWhenGivenId(){
        processingManagerService.findPipelineConfig("pipelineId");
        
        verify(configuration, times(1)).getPipelineConfig("pipelineId");
    }

    @Test
    public void getPipelineConfigsWillCallConfigurationGetPipelineConfigs(){
        ConcurrentHashMap<String, PipelineConfig> concurrentPipes = new ConcurrentHashMap<>();
        when(configuration.getPipelineConfigs()).thenReturn(concurrentPipes);
        
        processingManagerService.getPipelineConfigs();
        
        verify(configuration, times(1)).getPipelineConfigs();
    }

    @Test
    public void testProcessWillSendCriticalNotificationWhenGivenUnknownPipelineId() {
        ModelRun testSavedModelRun = new ModelRun();
        when(configuration.getPipelineConfig("type")).thenReturn(null);
        when(modelRunPersistentStorage.save((ModelRun) anyObject())).thenReturn(testSavedModelRun);
        
        processingManagerService.process("type", null);
        
        verify(notification, times(1)).sendNotification(anyString(), eq(NotificationService.NotificationLevel.CRITICAL));
        verify(modelRunPersistentStorage, times(1)).save((ModelRun) anyObject());
    }
    
    @Test
    public void testProcessWillSendCriticalNotificationWhenGivenPipelineConfigWithNoOutputs() {
        ModelRun testSavedModelRun = new ModelRun();
        when(configuration.getPipelineConfig("type")).thenReturn(pipelineConfig);
        when(pipelineConfig.getOutputs()).thenReturn(null);
        when(modelRunPersistentStorage.save((ModelRun) anyObject())).thenReturn(testSavedModelRun);
        
        processingManagerService.process("type", null);
        
        verify(notification, times(1)).sendNotification(anyString(), eq(NotificationService.NotificationLevel.CRITICAL));
        verify(modelRunPersistentStorage, times(1)).save((ModelRun) anyObject());
    }

    @Test
    public void testProcessWillSendInfoNotificationWhenGivenKnownPipelineId() {
        ModelRun testSavedModelRun = new ModelRun();
        ConcurrentHashMap<String, PipelineConfig> pipelineConfigs = new ConcurrentHashMap<>();
        when(configuration.getPipelineConfig("type")).thenReturn(pipelineConfig);
        when(modelRunPersistentStorage.save((ModelRun) anyObject())).thenReturn(testSavedModelRun);
        when(configuration.getPipelineConfigs()).thenReturn(pipelineConfigs);
        processingManagerService.process("type", null);
        
        verify(notification, times(1)).sendNotification(anyString(), eq(NotificationService.NotificationLevel.INFO));
        verify(modelRunPersistentStorage, times(1)).save((ModelRun) anyObject());
    }
}


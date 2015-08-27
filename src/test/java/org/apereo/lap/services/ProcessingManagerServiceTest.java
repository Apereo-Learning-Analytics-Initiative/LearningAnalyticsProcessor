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
package org.apereo.lap.services;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import javax.annotation.Resource;

import org.apereo.lap.services.configuration.ConfigurationService;
import org.apereo.lap.services.storage.StorageService;
import org.apereo.lap.test.AbstractUnitTest;
import org.junit.Test;
import org.junit.After;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;

public class ProcessingManagerServiceTest extends AbstractUnitTest{
    private static final Logger logger = LoggerFactory.getLogger(ProcessingManagerServiceTest.class);

    @Autowired
    ProcessingManagerService processingManagerService;
    @Autowired
    StorageService storage;
    @Resource
    ConfigurationService configuration;
    @Autowired JdbcTemplate jdbcTemplate;
    
    @After
    public void tearDown(){
        this.jdbcTemplate.execute("SET REFERENTIAL_INTEGRITY FALSE");
        this.jdbcTemplate.execute("TRUNCATE TABLE PERSONAL");
        this.jdbcTemplate.execute("TRUNCATE TABLE COURSE");
        logger.warn("this executed");
    }

    @Test
    public void testProcess() {
        assertNotNull(processingManagerService.getPipelineProcessors());
        assertNotNull(processingManagerService.getPipelineConfigs());
        assertTrue(processingManagerService.process("sample", null));
        logger.info("Test successful in processing 'sample'");

        assertFalse(processingManagerService.process("sample-fail-test", null));
        logger.info("Test successful in not being able to process 'sample-fail-test'");
    }
}

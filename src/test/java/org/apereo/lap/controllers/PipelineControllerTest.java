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
package org.apereo.lap.controllers;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.List;
import java.util.Map;

import org.apereo.lap.model.PipelineConfig;
import org.apereo.lap.test.AbstractUnitTest;
import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

public class PipelineControllerTest extends AbstractUnitTest{

    private static final Logger logger = LoggerFactory.getLogger(PipelineControllerTest.class);

    @Autowired
    PipelineController pipelineController;
    @Autowired
    JdbcTemplate jdbcTemplate;
    @After
    public void tearDown(){
        this.jdbcTemplate.execute("SET REFERENTIAL_INTEGRITY FALSE");
        this.jdbcTemplate.execute("TRUNCATE TABLE PERSONAL");
        this.jdbcTemplate.execute("TRUNCATE TABLE COURSE");
        logger.warn("this executed");
    }
    
    @SuppressWarnings("unchecked")
    @Test
    public void testRootGet() {
        assertNotNull(pipelineController);
        Map<String, Object> map = pipelineController.rootGet();
        assertNotNull(map);

        List<PipelineConfig> processors = (List<PipelineConfig>) map.get("processors");
        assertTrue(processors.size() > 0);
        logger.info("Test successful in loading pipeline processors");
    }

    @Test
    public void testGetType() {
        assertNotNull(pipelineController.getType("sample"));
        logger.info("Test successful in fetching pipeline config for type sample using pipeline controller ");
    }

    /*
     * TODO - this test must be fixed so that it is not writing to the live database
     */
    
    //@Test
    public void testStart() {
        assertTrue(pipelineController.start("sample"));
        logger.info("Test successful in starting pipeline for type sample using pipeline controller ");
    }
}

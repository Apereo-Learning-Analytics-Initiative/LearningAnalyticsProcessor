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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.List;
import java.util.Map;

import org.apereo.lap.services.configuration.ConfigurationService;
import org.apereo.lap.services.storage.StorageService;
import org.apereo.lap.test.AbstractUnitTest;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.transaction.BeforeTransaction;
import org.springframework.test.jdbc.JdbcTestUtils;

/**
 * Integration services test for Storage
 */
@ActiveProfiles("test")
public class StorageServiceTest extends AbstractUnitTest{
    private static final Logger logger = LoggerFactory.getLogger(StorageService.class);
    @Autowired
    ConfigurationService configuration;

    @Autowired
    StorageService storage;

    @javax.annotation.Resource
    ResourceLoader resourceLoader;

    @Before
    @BeforeTransaction
    public void before() {
    	configuration.config().setProperty("test", true);
        assertTrue( configuration.is("test") );
    }

    @Test
    public void testServiceLoad() {
        assertNotNull(storage);
        assertNotNull(storage.getConfiguration());
        assertNotNull(storage.getTempJdbcTemplate());
    }

    @SuppressWarnings("deprecation")
    @Test
    public void testServiceSQL() {
        List<Map<String, Object>> results;
        assertNotNull(storage);

        // check for empty tables
        results = storage.getTempJdbcTemplate().queryForList("SELECT * FROM PERSONAL");
        assertNotNull(results);
        assertTrue(results.isEmpty());
        results = storage.getTempJdbcTemplate().queryForList("SELECT * FROM COURSE");
        assertNotNull(results);
        assertTrue(results.isEmpty() );

        Resource sample = resourceLoader.getResource("sample.sql");
        JdbcTestUtils.executeSqlScript(storage.getTempJdbcTemplate(), sample, true);
        logger.info("Loaded sample SQL script: "+sample.getFilename());

        results = storage.getTempJdbcTemplate().queryForList("SELECT * FROM PERSONAL");
        assertNotNull(results);
        assertTrue(!results.isEmpty());
        assertEquals(2,results.size());

        results = storage.getTempJdbcTemplate().queryForList("SELECT * FROM COURSE");
        assertNotNull(results);
        assertTrue(!results.isEmpty());
        assertEquals(3, results.size());
    }

}

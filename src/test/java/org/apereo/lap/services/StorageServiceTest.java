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

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.transaction.BeforeTransaction;
import org.springframework.test.context.transaction.TransactionConfiguration;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

@TransactionConfiguration(defaultRollback = true)
@ContextConfiguration({ "classpath:test-context.xml" })
@Transactional
@RunWith(SpringJUnit4ClassRunner.class)
public class StorageServiceTest {
    private static final Logger logger = LoggerFactory.getLogger(StorageService.class);

    @Autowired
    ConfigurationService configuration;

    @Autowired
    StorageService storage;

    @Before
    @BeforeTransaction
    public void before() {
        configuration.config().setProperty("test", true);
        assertTrue( configuration.is("test") );
    }

    @Test
    public void testService() {
        assertNotNull(storage);
        assertNotNull(storage.getConfiguration());
        assertNotNull(storage.getTempDataSource());
        assertNotNull(storage.getPersistentDataSource());
        assertNotNull(storage.getTempJdbcTemplate());
        assertNotNull(storage.getPersistentJdbcTemplate());


        // TODO add real tests
    }
}

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

import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.transaction.TransactionConfiguration;
import org.springframework.transaction.annotation.Transactional;

@TransactionConfiguration(defaultRollback = true)
@ContextConfiguration({ "classpath:test-context.xml" })
@RunWith(SpringJUnit4ClassRunner.class)
@Transactional
public class ProcessingManagerServiceTest {
	private static final Logger logger = LoggerFactory
			.getLogger(ProcessingManagerServiceTest.class);

	@Autowired
	ProcessingManagerService processingManagerService;
	
	@Autowired
	StorageService storage;
	
    @Resource
    ConfigurationService configuration;
    
    @Test
    public void testProcess() {
		assertNotNull(processingManagerService);
		assertNotNull(processingManagerService.getPipelineProcessors());
		assertNotNull(processingManagerService.getPipelineConfigs());
		assertTrue(processingManagerService.process("sample", null));
		logger.info("Test successful in processing 'sample'");
		
		assertFalse(processingManagerService.process("sample-fail-test", null));
		logger.info("Test successful in not being able to process 'sample-fail-test'");
	}
}

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

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.apereo.lap.controllers.model.RiskConfidenceRequest;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@ContextConfiguration({ "classpath:test-context.xml" })
@RunWith(SpringJUnit4ClassRunner.class)
public class ApiControllerTest {
	
	private static final Logger logger = LoggerFactory.getLogger(ApiControllerTest.class);
	
	@Autowired
	private ApiController apiController;
	
	@Test
	public void testRiskConfidence() {
		assertNotNull(apiController);
		RiskConfidenceRequest riskConfidenceRequest = new RiskConfidenceRequest();
		String user="STUDENT1";
		String course="MNG_333N_222_08F";
		riskConfidenceRequest.setCourse(course);
		riskConfidenceRequest.setUser(user);
		assertTrue(apiController.riskconfidence(riskConfidenceRequest).size()>0);
        logger.info("Test Successful on fetching risk confidence for existing student:"+user+" in course "+course);

	}
	
	@Test
	public void testRiskConfidenceNotProcessingRunFound() {
		RiskConfidenceRequest riskConfidenceRequest = new RiskConfidenceRequest();
		String user="STUDENT2";
		String course="MNG_333N_222_09F";
		riskConfidenceRequest.setCourse(course);
		riskConfidenceRequest.setUser(user);
		assertFalse(apiController.riskconfidence(riskConfidenceRequest).size()>0);
		logger.info("Test Successful on not able to fetch risk confidence for student:"+user+" in non existing course "+course);
	}
}

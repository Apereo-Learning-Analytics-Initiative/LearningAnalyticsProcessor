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
package org.apereo.lap.dao;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.apereo.lap.dao.model.RiskConfidence;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@ContextConfiguration({ "classpath:test-context.xml" })
@RunWith(SpringJUnit4ClassRunner.class)
public class RiskConfidenceRepositoryImplTest {

	private static final Logger logger = LoggerFactory.getLogger(RiskConfidenceRepositoryImplTest.class);

	@Autowired
	private RiskConfidenceRepository riskConfidenceRepository;
	
	@Test
	public void testFindByUserCourseDate() {
		// Test to find of the risk for the user in the course 
		assertNotNull(riskConfidenceRepository);
		String user="STUDENT1";
		String course="MNG_333N_222_08F";
		List<RiskConfidence> list = riskConfidenceRepository.findByUserCourseDate(user, course);
		assertNotNull(list);
		assertTrue(list.size()>0);
		logger.info("Test successful in getting risk for student:"+user+" in course:"+course);

		// Test to not find risk for the user in the course mentioned		
		user="STUDENT152";
		course="MNG_333N_222_08F";
		list = riskConfidenceRepository.findByUserCourseDate(user, course);
		assertNotNull(list);
		assertFalse(list.size()>0);
		logger.info("Test successful in failing to get any risk for non existent sample student:"+user+" in course:"+course);
	}
	
}

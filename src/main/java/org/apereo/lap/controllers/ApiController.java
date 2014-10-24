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

import java.util.List;

import org.apereo.lap.controllers.model.RiskConfidenceRequest;
import org.apereo.lap.dao.RiskConfidenceRepository;
import org.apereo.lap.dao.model.RiskConfidence;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * Controller for the rest api
 */
@Controller
@RequestMapping(value = {"/api"})
public class ApiController {

	@Autowired
	private RiskConfidenceRepository riskConfidenceRepository;
	
    private static final Logger logger = LoggerFactory.getLogger(ApiController.class);


    /**
     * Risk confidence retrieval
     */
    @RequestMapping(value = {"/riskconfidence"}, method = RequestMethod.GET, produces="application/json;charset=utf-8")
    public @ResponseBody List<RiskConfidence> riskconfidence(@ModelAttribute RiskConfidenceRequest request) {
    	
    	if (logger.isDebugEnabled()) {
    		logger.debug(request.toString());
    	}
    	
    	return riskConfidenceRepository.findByUserCourseDate(request.getUser(), request.getCourse());
    }
}

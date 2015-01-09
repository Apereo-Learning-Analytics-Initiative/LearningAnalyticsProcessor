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

import org.apereo.lap.controllers.model.ConfigurationRequest;
import org.apereo.lap.controllers.model.Response;
import org.apereo.lap.dao.ConfigurationRepository;
import org.apereo.lap.dao.model.Configuration;
import org.apereo.lap.services.ConfigurationService;
import org.apereo.lap.services.ProcessingManagerService;
import org.h2.util.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import javax.annotation.Resource;

/**
 * Sample controller for going to the home page with a message
 */
@Controller
public class ConfigController {

    private static final Logger logger = LoggerFactory.getLogger(ConfigController.class);

    @Resource
    ConfigurationRepository configurationRepository;

    @Resource
    ProcessingManagerService processingManagerService;

    /**
     * Selects the home page and populates the model with a message
     */
    @RequestMapping(value = "/config", method = RequestMethod.GET)
    public String index(Model viewModel) {

    	Configuration configuration = getConfiguration();
    	
    	ConfigurationRequest model = new ConfigurationRequest();
    	model.setSspBaseUrl(configuration.getSspBaseUrl());
    	if(configuration.getRiskConfidenceThreshold() != null)
		{
    		model.setRiskConfidenceThreshold(Float.toString(configuration.getRiskConfidenceThreshold()));
		}
    	model.setActive(configuration.isActive());;
    	
    	viewModel.addAttribute("configuration", model);
    	
        return "config";
    }

    @RequestMapping(value = "/config/save", method = RequestMethod.POST)
    public @ResponseBody Response index(@RequestBody ConfigurationRequest model) {
    
    	Response response = new Response();	

    	try
    	{
    		if(!StringUtils.isNullOrEmpty(model.getRiskConfidenceThreshold()))
    		{
    			Double.parseDouble(model.getRiskConfidenceThreshold());
    		}
    	}
    	catch(NumberFormatException e)
    	{
    		response.getErrors().add("Risk confidence threshold needs to be a number");
    	}
    	
    	if(response.getErrors().size() == 0)
    	{
    		Configuration configuration = getConfiguration();
    		configuration.setSspBaseUrl(model.getSspBaseUrl());
    		if(!StringUtils.isNullOrEmpty(model.getRiskConfidenceThreshold()))
    		{
    			configuration.setRiskConfidenceThreshold(Float.parseFloat(model.getRiskConfidenceThreshold()));
    		}
    		else
    		{
    			configuration.setRiskConfidenceThreshold(null);
    		}
    		configuration.setActive(model.isActive());;
    		
    		configurationRepository.save(configuration);
    	}
    	
        return  response;
    }
    
    private Configuration getConfiguration(){
    	Configuration configuration = new Configuration();
		
		List<Configuration> configurations = configurationRepository.findAll();
		
		if(configurations.size() > 0)
		{
			configuration = configurations.get(0);
		}
		return configuration;
    }
}

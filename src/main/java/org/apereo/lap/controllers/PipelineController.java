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

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apereo.lap.model.PipelineConfig;
import org.apereo.lap.services.ProcessingManagerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * Service to retrieve pipeline processor configuration and initiate a processor run
 */
@Controller
public class PipelineController {

    private static final Logger logger = LoggerFactory.getLogger(PipelineController.class);

    @Autowired
    ProcessingManagerService processingManagerService;

    /**
     * lists out the pipelines available (keys)
     */
    @RequestMapping(value = {"/api/pipelines","/api/pipelines/"}, method = RequestMethod.GET, produces="application/json;charset=utf-8")
    public @ResponseBody Map<String, Object> rootGet() {
    	
    	if (logger.isDebugEnabled()) {
    		logger.debug("Get all pipeline configs");
    	}
        
        Map<String, PipelineConfig> pipelines = processingManagerService.getPipelineConfigs();
        Map<String, Object> data = new LinkedHashMap<>();
        List<PipelineConfig> procs = new ArrayList<>();
        
        if (pipelines != null && !pipelines.isEmpty()) {
            for (PipelineConfig pipelineProcessor : pipelines.values()) {
                procs.add(pipelineProcessor);
            }
        }
        
        data.put("processors", procs);
        return data;
    }

    /**
     * Get one pipeline config
     */
    @RequestMapping(value = {"/api/pipelines/{type}"}, method = RequestMethod.GET, produces="application/json;charset=utf-8")
    public @ResponseBody PipelineConfig getType(@PathVariable("type") String type) {
    	if (logger.isDebugEnabled()) {
    		logger.debug("Get pipeline config for type: "+type);
    	}
    	
        return processingManagerService.findPipelineConfig(type);
    }

    /**
     * Post to start one pipeline
     * TODO probably need to add security to this
     */
    @RequestMapping(value = {"/api/pipelines/start/{type}"}, method = RequestMethod.POST, produces="application/json;charset=utf-8")
    public @ResponseBody boolean start(@PathVariable("type") String type) {
    	if (logger.isDebugEnabled()) {
    		logger.debug("Start pipeline for type: "+type);
    	}
    	
        return processingManagerService.process(type, null);
    }

    /**
     * Post to start one pipeline with JSON data
     * TODO probably need to add security to this
     */
    @RequestMapping(value = {"/json/{type}"}, method = RequestMethod.POST, consumes="application/json", produces="application/json;charset=utf-8")
    public @ResponseBody boolean postJsonType(@PathVariable("type") String type, @RequestBody String json) {
    	if (logger.isDebugEnabled()) {
    		logger.debug("Start pipeline for type: "+type+" with json: "+json);
    	}
    	
        return processingManagerService.process(type, json);
    }
}

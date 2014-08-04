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

import org.apereo.lap.model.PipelineConfig;
import org.apereo.lap.services.ConfigurationService;
import org.apereo.lap.services.ProcessingManagerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Sample controller for going to the home page with a message
 */
@Controller
@RequestMapping("/pipeline")
public class PipelineController {

    private static final Logger logger = LoggerFactory.getLogger(PipelineController.class);

    @Resource
    ConfigurationService configuration;

    @Resource
    ProcessingManagerService processingManagerService;

    /**
     * lists out the pipelines available (keys)
     */
    @RequestMapping(value = {"/",""}, method = RequestMethod.GET, produces="application/json;charset=utf-8")
    public @ResponseBody Map rootGet() {
        logger.info("pipelines");
        Map<String, PipelineConfig> pipelines = processingManagerService.getPipelineConfigs();
        Map<String, Object> data = new LinkedHashMap<>();
        List<PipelineConfig> procs = new ArrayList<>();
        for (PipelineConfig pipelineProcessor : pipelines.values()) {
            procs.add(pipelineProcessor);
        }
        data.put("processors", procs);
        return data;
    }

    /**
     * Get one pipeline config
     */
    @RequestMapping(value = {"/{type}"}, method = RequestMethod.GET, produces="application/json;charset=utf-8")
    public @ResponseBody PipelineConfig getType(@PathVariable("type") String pipelineConfigId) {
        logger.info("pipeline config: "+pipelineConfigId);
        return processingManagerService.findPipelineConfig(pipelineConfigId);
    }

    /**
     * Post to start one pipeline
     * TODO probably need to add security to this
     */
    @RequestMapping(value = {"/{type}"}, method = RequestMethod.POST, produces="application/json;charset=utf-8")
    public @ResponseBody boolean postType(@PathVariable("type") String pipelineConfigId) {
        logger.info("pipeline POST: "+pipelineConfigId);
        return processingManagerService.process(pipelineConfigId, null);
    }

    /**
     * Post to start one pipeline with JSON data
     * TODO probably need to add security to this
     */
    @RequestMapping(value = {"/json/{type}"}, method = RequestMethod.POST, consumes="application/json", produces="application/json;charset=utf-8")
    public @ResponseBody boolean postJsonType(@PathVariable("type") String pipelineConfigId, @RequestBody String inputJson) {
        logger.info("pipeline POST: "+pipelineConfigId+" with JSON data");
        return processingManagerService.process(pipelineConfigId, inputJson);
    }
}

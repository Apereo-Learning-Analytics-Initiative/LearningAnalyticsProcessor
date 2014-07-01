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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.annotation.Resource;

/**
 * This is the trigger and general management point for the entire processing of a pipeline.
 *
 * This starts off by determining the pipeline which is going to be executed and then...
 * - Reads in the system config
 * - Reads in the pipeline config
 * - Handles the inputs
 * - Starts the pipeline processor
 * - Handles the outputs
 * - Sends the notifications
 * 
 * @author Aaron Zeckoski (azeckoski @ unicon.net) (azeckoski @ vt.edu)
 */
@Component
public class ProcessingManagerService {

    private static final Logger logger = LoggerFactory.getLogger(ProcessingManagerService.class);

    @Resource
    ConfigurationService configuration;
    @Resource
    StorageService storage;
    @Resource
    NotificationService notification;
    @Resource
    InputHandlerService inputHandler;
    @Resource
    ProcessorService processor;
    @Resource
    OutputHandlerService outputHandler;


    @PostConstruct
    public void init() {
        logger.info("Processing Initialized");
        // TODO load up config
        // TODO load up pipelines configs
        logger.info("Processing Complete");
    }

    @PreDestroy
    public void destroy() {
        logger.info("DESTROY");
    }

    public void process(String pipelineId) {
        logger.info("Processing Initialized");
        // TODO load up pipeline config (by id)
        // TODO handle the inputs
        // TODO start the pipeline processor
        // TODO handle the outputs
        // TODO send notifications
        logger.info("Processing Complete");
    }

}

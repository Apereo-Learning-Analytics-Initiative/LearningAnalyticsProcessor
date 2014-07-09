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

import org.apereo.lap.model.PipelineConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.annotation.Resource;
import java.util.List;

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
        logger.info("INIT");
        if (configuration.config.getBoolean("process.pipeline.sample", false)) {
            logger.info("Running Sample Pipeline process");
            process("sample");
            logger.info("Sample Pipeline process COMPLETE");
        }
    }

    @PreDestroy
    public void destroy() {
        logger.info("DESTROY");
    }

    public void process(String pipelineId) {
        logger.info("Pipeline Initialized: "+pipelineId);
        // load up pipeline config (by id)
        PipelineConfig config = configuration.getPipelineConfig(pipelineId);
        if (config == null) {
            throw new IllegalArgumentException("No PipelineConfig found for id/type: "+pipelineId);
        }

        // verify the inputs exist
        List<PipelineConfig.InputField> inputs = config.getInputs();
        boolean missingRequiredInput = false;
        for (PipelineConfig.InputField input : inputs) {
            if (!storage.checkTableAndColumnExist(input.getCollection(), input.getName(), true)) {
                logger.warn("Missing input: "+input);
                if (input.required) {
                    missingRequiredInput = true;
                }
            }
        }
        if (missingRequiredInput) {
            throw new IllegalArgumentException("Missing required inputs in the temp data, cannot proceed with this pipeline: "+pipelineId);
        } else {
            logger.info("All required inputs exist for type: "+pipelineId);
        }

        // TODO start the pipeline processor
        List<PipelineConfig.Processor> processors = config.getProcessors();
        for (PipelineConfig.Processor processor : processors) {
            if (PipelineConfig.ProcessorType.KETTLE == processor.type) {
                // TODO process it!

            } else {
                throw new IllegalArgumentException("Cannot handle processor of type: "+processor.type);
            }
        }

        // TODO handle the outputs
        // TODO send notifications
        logger.info("Processing Complete");
    }

}

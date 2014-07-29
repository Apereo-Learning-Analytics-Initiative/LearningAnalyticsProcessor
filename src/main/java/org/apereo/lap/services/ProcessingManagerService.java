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

import org.apereo.lap.model.Output;
import org.apereo.lap.model.PipelineConfig;
import org.apereo.lap.model.Processor;
import org.apereo.lap.services.output.OutputHandler;
import org.apereo.lap.services.pipeline.PipelineProcessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.annotation.Resource;
import java.util.*;

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

    static final Logger logger = LoggerFactory.getLogger(ProcessingManagerService.class);

    @Resource
    ConfigurationService configuration;
    @Resource
    StorageService storage;
    @Resource
    NotificationService notification;
    @Resource
    InputHandlerService inputHandler;
    @Resource
    OutputHandlerService outputHandler;

    @Autowired
    List<PipelineProcessor> pipelineProcessors;

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

    /**
     * @param processorKey the key for this PipelineProcessor
     * @return the PipelineProcessor if found OR null if none exist for this key
     */
    public PipelineProcessor findProcessor(String processorKey) {
        Processor.ProcessorType ptype = Processor.ProcessorType.valueOf(processorKey);
        for (PipelineProcessor pipelineProcessor : pipelineProcessors) {
            if (pipelineProcessor.getProcessorType() == ptype) {
                return pipelineProcessor;
            }
        }
        return null;
    }

    /**
     * @param pipelineId the id for this pipeline config
     * @return the config OR null if not found for this id
     */
    public PipelineConfig findPipelineConfig(String pipelineId) {
        // load up pipeline config (by id)
        return configuration.getPipelineConfig(pipelineId);
    }

    /**
     * @return the map of String config id to PipelineConfig
     */
    public Map<String, PipelineConfig> getPipelineConfigs() {
        return new HashMap<>(configuration.getPipelineConfigs());
    }

    public boolean process(String pipelineId) {
        logger.info("Pipeline Initialized: "+pipelineId);
        boolean processResult = false;
        try {
            // load up pipeline config (by id)
            PipelineConfig pipelineConfig = configuration.getPipelineConfig(pipelineId);
            if (pipelineConfig == null) {
                throw new IllegalArgumentException("No PipelineConfig found for id/type: "+pipelineId);
            }

            // load up the inputs
            List<PipelineConfig.InputField> inputs = pipelineConfig.getInputs();

            // verify the inputs exist
            boolean missingRequiredInput = false;
            for (PipelineConfig.InputField input : inputs) {
                if (!storage.checkTableAndColumnExist(input.getCollection().name(), input.getName(), true)) {
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

            // load in the inputs IF needed
            Set<InputHandlerService.InputCollection> toLoad = new HashSet<>();
            for (PipelineConfig.InputField input : inputs) {
                toLoad.add(input.collection);
            }
            if (toLoad.isEmpty()) {
                toLoad = null; // don't load anything if the model does not indicate it needs it
            }
            inputHandler.loadInputCollections(false, false, toLoad); // load on-demand, do not reset

            // start the pipeline processors
            List<Processor> processors = pipelineConfig.getProcessors();
            logger.info("Pipeline ("+pipelineId+") running "+processors.size()+" processors");
            for (Processor processorConfig : processors) {
                boolean matched = false;
                for (PipelineProcessor pipelineProcessor : pipelineProcessors) {
                    if (pipelineProcessor.getProcessorType() == processorConfig.type) {
                        matched = true;
                        try {
                            PipelineProcessor.ProcessorResult result = pipelineProcessor.process(pipelineConfig, processorConfig);
                            logger.info(pipelineProcessor.getProcessorType()+" pipeline (" + pipelineId + ") processor ("+processorConfig.name+") complete: "+result);
                        } catch (Exception e) {
                            throw new RuntimeException(pipelineProcessor.getProcessorType()+" pipeline (" + pipelineId + ") processor ("+processorConfig.name+") failed: " + e);
                        }
                    }
                }
                if (!matched) {
                    // no processor found for the requested type
                    throw new IllegalArgumentException("Cannot handle processor of type: "+processorConfig.type);
                }
            }

            // handle the outputs
            List<Output> outputs = pipelineConfig.getOutputs();
            boolean outputSuccess = false;
            for (Output output : outputs) {
                try {
                    OutputHandler.OutputResult result = outputHandler.doOutput(output);
                    logger.info("Output complete: "+result);
                    outputSuccess = true;
                } catch (Exception e) {
                    logger.error("Output processor for pipeline (" + pipelineId + ") failure: "+e);
                }
            }
            // as long as at least one output was successful then we will count this pipeline as complete
            if (!outputs.isEmpty() && !outputSuccess) {
                // if no outputs succeeded then the pipeline has failed
                throw new RuntimeException("All outputs failed, pipeline failure in outputs");
            }

            // send success notification
            notification.sendNotification("Pipeline ("+pipelineId+") Complete", NotificationService.NotificationLevel.INFO);
            processResult = true;
        } catch (RuntimeException e) {
            // send failure notification
            String msg = "Pipeline ("+pipelineId+") FAILED: "+e;
            logger.error(msg);
            notification.sendNotification(msg, NotificationService.NotificationLevel.CRITICAL);
        }
        return processResult;
    }

    public List<PipelineProcessor> getPipelineProcessors() {
        return pipelineProcessors;
    }

}

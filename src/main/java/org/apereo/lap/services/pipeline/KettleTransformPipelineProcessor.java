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
package org.apereo.lap.services.pipeline;

import org.apereo.lap.model.PipelineConfig;
import org.apereo.lap.model.Processor;
import org.apereo.lap.services.ConfigurationService;
import org.apereo.lap.services.StorageService;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.io.File;
import java.io.IOException;

/**
 * Handles the pipeline processing for Kettle processors
 *
 * @author Aaron Zeckoski (azeckoski @ unicon.net) (azeckoski @ vt.edu)
 */
@Component
public class KettleTransformPipelineProcessor implements PipelineProcessor {

    @Resource
    ConfigurationService config;

    @Resource
    StorageService storage;

    @Resource
    ResourceLoader resourceLoader;

    @PostConstruct
    public void init() {
        // Do any init here you need to (but note this is for the service and not each run)
    }

    @Override
    public Processor.ProcessorType getProcessorType() {
        return Processor.ProcessorType.KETTLE_TRANSFORM;
    }

    @Override
    public ProcessorResult process(PipelineConfig pipelineConfig, Processor processorConfig) {
        String name = processorConfig.name;
        ProcessorResult result = new ProcessorResult(Processor.ProcessorType.KETTLE_TRANSFORM);
        File kettleXMLFile;
        try {
            // TODO maybe make this read the kettle files from the pipelines dir as well?
            kettleXMLFile = resourceLoader.getResource("classpath:"+processorConfig.filename).getFile();
        } catch (IOException e) {
            throw new RuntimeException("FAIL! (to read kettle file): "+processorConfig.filename+" :"+e, e);
        }

        // TODO do processing here! (Bob)

        result.done(0, null); // TODO populate count and failures
        return result;
    }

}

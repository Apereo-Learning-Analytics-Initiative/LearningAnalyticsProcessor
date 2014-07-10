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

import java.io.File;

/**
 * Handles the pipeline processing for Kettle processors
 *
 * @author Aaron Zeckoski (azeckoski @ unicon.net) (azeckoski @ vt.edu)
 */
public class KettlePipelineProcessor implements PipelineProcessor {

    String name;
    PipelineConfig config;
    PipelineConfig.Processor processorConfig;
    File kettleXMLFile;

    public KettlePipelineProcessor(PipelineConfig config, PipelineConfig.Processor processorConfig, File kettleXMLFile) {
        this.config = config;
        this.processorConfig = processorConfig;
        this.name = processorConfig.name;
        this.kettleXMLFile = kettleXMLFile;
    }

    @Override
    public PipelineConfig.ProcessorType getProcessorType() {
        return PipelineConfig.ProcessorType.KETTLE;
    }

    @Override
    public ProcessorResult process() {
        ProcessorResult result = new ProcessorResult(PipelineConfig.ProcessorType.KETTLE);

        // TODO do processing here! (Bob)

        result.done(null); // TODO populate failures
        return result;
    }

}

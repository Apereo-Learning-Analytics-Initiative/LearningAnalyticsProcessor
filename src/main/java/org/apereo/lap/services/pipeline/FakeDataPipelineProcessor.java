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
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * This processor just produces Fake data in a "FAKE_DATA" table
 * The table has these fields: ID (auto), USERNAME, SCORE, INFO
 *
 * @author Aaron Zeckoski (azeckoski @ unicon.net) (azeckoski @ vt.edu)
 */
@Component
public class FakeDataPipelineProcessor implements PipelineProcessor {

    @Resource
    ConfigurationService config;

    @Resource
    StorageService storage;

    int recordsToFake = 100;

    @Override
    public Processor.ProcessorType getProcessorType() {
        return Processor.ProcessorType.FAKE_DATA;
    }

    @Override
    public ProcessorResult process(PipelineConfig pipelineConfig, Processor processorConfig) {
        ProcessorResult result = new ProcessorResult(Processor.ProcessorType.FAKE_DATA);

        // create the temp table


        result.done(null);
        return result;
    }

}

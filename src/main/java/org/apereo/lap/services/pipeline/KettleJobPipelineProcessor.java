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
import org.pentaho.di.core.KettleEnvironment;
import org.pentaho.di.core.util.EnvUtil;
import org.pentaho.di.job.Job;
import org.pentaho.di.job.JobMeta;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

import java.io.File;

/**
 * Handles the pipeline processing for Kettle processors
 *
 * @author Aaron Zeckoski (azeckoski @ unicon.net) (azeckoski @ vt.edu)
 * @author Robert Long (rlong @ unicon.net)
 */
@Component
public class KettleJobPipelineProcessor extends KettleBasePipelineProcessor {

    @PostConstruct
    public void init() {
        // Do any init here you need to (but note this is for the service and not each run)
    }

    @Override
    public Processor.ProcessorType getProcessorType() {
        return Processor.ProcessorType.KETTLE_JOB;
    }

    @Override
    public ProcessorResult process(PipelineConfig pipelineConfig, Processor processorConfig) {
        String name = processorConfig.name;
        ProcessorResult result = new ProcessorResult(Processor.ProcessorType.KETTLE_JOB);
        File kettleXMLFile = getFile(processorConfig.filename);

        // TODO do processing here! (Bob)
        try {
            KettleEnvironment.init(false);
            EnvUtil.environmentInit();
            JobMeta jobMeta = new JobMeta(kettleXMLFile.getAbsolutePath(), null, null);

            Job job = new Job(null, jobMeta);
            job.start();
            job.waitUntilFinished();
        } catch(Exception e) {
        }

        result.done(0, null); // TODO populate count and failures
        return result;
    }

}

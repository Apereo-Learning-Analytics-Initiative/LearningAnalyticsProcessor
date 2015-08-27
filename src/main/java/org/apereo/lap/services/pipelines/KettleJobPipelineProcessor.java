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
package org.apereo.lap.services.pipelines;

import org.apereo.lap.model.PipelineConfig;
import org.apereo.lap.model.Processor;
import org.pentaho.di.core.Result;
import org.pentaho.di.job.Job;
import org.pentaho.di.job.JobMeta;
import org.springframework.stereotype.Component;

/**
 * Handles the pipeline processing for Kettle Job processors
 *
 * @author Aaron Zeckoski (azeckoski @ unicon.net) (azeckoski @ vt.edu)
 * @author Robert Long (rlong @ unicon.net)
 */
@Component
public class KettleJobPipelineProcessor extends KettleBasePipelineProcessor {

    @Override
    public Processor.ProcessorType getProcessorType() {
        return Processor.ProcessorType.KETTLE_JOB;
    }

    @Override
    public ProcessorResult process(PipelineConfig pipelineConfig, Processor processorConfig, String inputJson) {
        ProcessorResult result = new ProcessorResult(Processor.ProcessorType.KETTLE_JOB);

        try {
            JobMeta jobMeta = new JobMeta(configurationService.getApplicationHomeDirectory().resolve(processorConfig.filename).toString(), null, null);

            // update the shared objects to use the pre-configured shared objects
            jobMeta.setSharedObjects(kettleConfiguration.getSharedObjects());

            // run the job
            Job job = new Job(null, jobMeta);
            job.start();
            job.waitUntilFinished();

            // process the results
            Result jobResult = job.getResult();
            result.done((int) jobResult.getNrErrors(), null);
        } catch(Exception e) {
            logger.error("An error occurred processing the job file: " + processorConfig.filename + ". Error: " + e, e);
        }

        return result;
    }

}

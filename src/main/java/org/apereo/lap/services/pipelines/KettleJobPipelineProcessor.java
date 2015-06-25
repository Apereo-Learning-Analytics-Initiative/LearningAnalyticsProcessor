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

import org.apache.commons.lang.StringUtils;
import org.apereo.lap.model.PipelineConfig;
import org.apereo.lap.model.Processor;
import org.pentaho.di.core.Result;
import org.pentaho.di.job.Job;
import org.pentaho.di.job.JobMeta;
import org.pentaho.di.job.entries.job.JobEntryJob;
import org.pentaho.di.job.entry.JobEntryCopy;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

import java.io.File;
import java.util.Iterator;

/**
 * Handles the pipeline processing for Kettle Job processors
 *
 * @author Aaron Zeckoski (azeckoski @ unicon.net) (azeckoski @ vt.edu)
 * @author Robert Long (rlong @ unicon.net)
 */
@Component
public class KettleJobPipelineProcessor extends KettleBasePipelineProcessor {

    /**
     * Service-level initialization, will not be run every time
     */
    @PostConstruct
    public void init() {
        configureKettle();
    }

    @Override
    public Processor.ProcessorType getProcessorType() {
        return Processor.ProcessorType.KETTLE_JOB;
    }

    @Override
    public ProcessorResult process(PipelineConfig pipelineConfig, Processor processorConfig, String inputJson) {
        ProcessorResult result = new ProcessorResult(Processor.ProcessorType.KETTLE_JOB);
        File kettleXMLFile = getFile(processorConfig.filename);

        try {
            // update the Weka Scoring model file path
            updateWekaScoringModel(getFile(makeFilePath(SCORING_TRANSFORM_FILE)));

            JobMeta jobMeta = new JobMeta(kettleXMLFile.getAbsolutePath(), null, null);

            // update the shared objects to use the pre-configured shared objects
            jobMeta.setSharedObjects(getSharedObjects());

            Iterator<JobEntryCopy> iterator = jobMeta.getJobCopies().iterator();
            while (iterator.hasNext()) {
                JobEntryCopy jobEntryCopy = iterator.next();
                // set the AssignWeights_Grades entry file name to the one on the classpath
                if (StringUtils.equalsIgnoreCase(jobEntryCopy.getName(), ASSIGN_WEIGHTS_ENTRY_NAME)) {
                    JobEntryJob jobEntryJob = (JobEntryJob) jobEntryCopy.getEntry();
                    File file = getFile(makeFilePath(ASSIGN_WEIGHTS_FILE_NAME));
                    jobEntryJob.setFileName(file.getAbsolutePath());

                    logger.info("Setting job entry '" + kettleXMLFile.getName() + " : " + ASSIGN_WEIGHTS_ENTRY_NAME + "' filename to " + file.getAbsolutePath());
                }
            }

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

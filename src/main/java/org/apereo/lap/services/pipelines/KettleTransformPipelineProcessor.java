/*******************************************************************************
 * Copyright (c) 2015 Unicon (R) Licensed under the
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
 *******************************************************************************/
package org.apereo.lap.services.pipelines;

import org.apereo.lap.model.PipelineConfig;
import org.apereo.lap.model.Processor;
import org.apereo.lap.services.configuration.ConfigurationService;
import org.pentaho.di.core.Result;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Handles the pipeline processing for Kettle processors
 *
 * @author Aaron Zeckoski (azeckoski @ unicon.net) (azeckoski @ vt.edu)
 * @author Robert Long (rlong @ unicon.net)
 */
@Component
public class KettleTransformPipelineProcessor extends KettleBasePipelineProcessor {


    @Autowired
    protected ConfigurationService configuration;


    @Override
    public Processor.ProcessorType getProcessorType() {
        return Processor.ProcessorType.KETTLE_TRANSFORM;
    }

    @Override
    public ProcessorResult process(PipelineConfig pipelineConfig, Processor processorConfig, String inputJson) {
        ProcessorResult result = new ProcessorResult(Processor.ProcessorType.KETTLE_TRANSFORM);
        try {
            TransMeta transMeta = new TransMeta(processorConfig.filename);

            // update the shared objects to use the pre-configured shared objects
            transMeta.setSharedObjects(kettleConfiguration.getSharedObjects());

            // run the transformation
            Trans trans = new Trans(transMeta);
            trans.calculateBatchIdAndDateRange();
            trans.beginProcessing();
            trans.execute(new String[]{});
            trans.waitUntilFinished();

            // process the results
            Result transResult = trans.getResult();
            result.done((int) transResult.getNrErrors(), null);
        } catch (Exception e) {
            logger.error("An error occurred processing the transformation file: " + processorConfig.filename + ". Error: " + e, e);
        }
        return result;
    }
}

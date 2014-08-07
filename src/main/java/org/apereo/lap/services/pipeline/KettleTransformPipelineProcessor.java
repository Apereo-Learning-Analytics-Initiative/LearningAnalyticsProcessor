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

import org.apache.commons.lang.StringUtils;
import org.apereo.lap.model.PipelineConfig;
import org.apereo.lap.model.Processor;
import org.pentaho.di.core.Result;
import org.pentaho.di.scoring.WekaScoringMeta;
import org.pentaho.di.scoring.WekaScoringModel;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.steps.jsoninput.JsonInputMeta;
import org.pentaho.di.trans.steps.jsonoutput.JsonOutputMeta;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

import java.io.File;
import java.util.List;
import java.util.UUID;

/**
 * Handles the pipeline processing for Kettle processors
 *
 * @author Aaron Zeckoski (azeckoski @ unicon.net) (azeckoski @ vt.edu)
 * @author Robert Long (rlong @ unicon.net)
 */
@Component
public class KettleTransformPipelineProcessor extends KettleBasePipelineProcessor {

    // TODO these JSON files no longer needed?
    private String jsonInputFilename = "sample1_input.json";
    private String jsonOutputFilename = "sample1_output.json";
    private String scoringModelFilename = SLASH + "kettle" + SLASH + "scoring_sample" + SLASH + "oaai.lap.logistic.pmml.xml";

    /**
     * Service-level initialization, will not be run every time
     */
    @PostConstruct
    public void init() {
        configureKettle();
    }

    @Override
    public Processor.ProcessorType getProcessorType() {
        return Processor.ProcessorType.KETTLE_TRANSFORM;
    }

    @Override
    public ProcessorResult process(PipelineConfig pipelineConfig, Processor processorConfig, String inputJson) {
        ProcessorResult result = new ProcessorResult(Processor.ProcessorType.KETTLE_TRANSFORM);
        File kettleXMLFile = getFile(processorConfig.filename);
        File inputFile = null;

        try {
            TransMeta transMeta = new TransMeta(kettleXMLFile.getAbsolutePath());

            // update the shared objects to use the pre-configured shared objects
            transMeta.setSharedObjects(getSharedObjects());

            List<StepMeta> stepMetaList = transMeta.getSteps();
            for (StepMeta stepMeta : stepMetaList) {
                logger.info("Processing step: '" + stepMeta.getName() + "' in file: " + kettleXMLFile.getAbsolutePath());

                // set the file path to the one necessary, based on step type
                // TODO these JSON type checks no longer needed?
                if (StringUtils.equalsIgnoreCase(stepMeta.getTypeId(), "JsonInput")){
                    String filePath = "";
                    // if no JSON input is given, use the hard-coded JSON input file from the classpath
                    if (StringUtils.isEmpty(inputJson)) {
                        // copy JSON input file from classpath:extracts/ to inputs/
                        inputHandler.copySampleCSV("extracts" + SLASH, jsonInputFilename);
                        filePath = configurationService.getInputDirectory().getAbsolutePath() + SLASH + jsonInputFilename;
                    } else {
                        // get input file contents from JSON string
                        inputFile = createTempInputFile(UUID.randomUUID().toString(), ".json");
                        filePath = inputFile.getAbsolutePath();

                        writeStringToFile(inputFile, inputJson);
                    }

                    JsonInputMeta jsonInputMeta = (JsonInputMeta) stepMeta.getStepMetaInterface();
                    jsonInputMeta.setFileName(new String[]{filePath});
                    logger.info("Setting StepMeta '" + kettleXMLFile.getName() + " : " + stepMeta.getName() + "' JSON input filename to " + filePath);
                } else if (StringUtils.equalsIgnoreCase(stepMeta.getTypeId(), "JsonOutput")) {
                    // set output file to output/<FILENAME>
                    String filePath = configurationService.getOutputDirectory().getAbsolutePath() + SLASH + jsonOutputFilename;
                    JsonOutputMeta jsonOutputMeta = (JsonOutputMeta) stepMeta.getStepMetaInterface();
                    jsonOutputMeta.setFileName(filePath);
                    jsonOutputMeta.setExtension("");
                    logger.info("Setting StepMeta '" + kettleXMLFile.getName() + " : " + stepMeta.getName() + "' JSON output filename to " + filePath);
                } else if (StringUtils.equalsIgnoreCase(stepMeta.getTypeId(), "WekaScoring")) {
                    // set Weka serialized scoring model
                    File file = getFile(scoringModelFilename);
                    WekaScoringMeta wekaScoringMeta = (WekaScoringMeta) stepMeta.getStepMetaInterface();
                    wekaScoringMeta.setSerializedModelFileName(file.getAbsolutePath());
                    wekaScoringMeta.setFileNameFromField(true);
                    logger.info("Setting StepMeta '" + kettleXMLFile.getName() + " : " + stepMeta.getName() + "' Weka scoring model filename to " + file.getAbsolutePath());
                } else if (StringUtils.equalsIgnoreCase(stepMeta.getTypeId(), "TableInput")) {
                    // do stuff for table input
                } else if (StringUtils.equalsIgnoreCase(stepMeta.getTypeId(), "TableOutput")) {
                    // do stuff for table output
                } else {
                    // do stuff for unknown step type IDs
                }
            }

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
        } finally {
            // delete the temporary JSON input file, if it exists
            if (inputFile != null) {
                deleteTempInputFile(inputFile);
            }
        }

        return result;
    }

}

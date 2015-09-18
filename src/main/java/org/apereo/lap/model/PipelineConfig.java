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
package org.apereo.lap.model;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.configuration.HierarchicalConfiguration;
import org.apache.commons.configuration.XMLConfiguration;
import org.apache.commons.lang.StringUtils;
import org.apereo.lap.model.Output.OutputField;
import org.apereo.lap.services.configuration.ConfigurationService;
import org.apereo.lap.services.input.BaseInputHandlerService;
import org.apereo.lap.services.storage.StorageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mongodb.AggregationOptions.OutputMode;

/**
 * This is an object that represents all configuration settings for a specific pipeline
 *
 * Each pipeline will be defined by a set of metadata which includes:
 * - name
 * - description (and recommendations for running the model)
 * - stat indicators (accuracy, confidence interval, etc.)
 * - required input fields
 * - processors (kettle ktr and kjb files, pmml files, etc.)
 * - output result definition
 * 
 * @author Aaron Zeckoski (azeckoski @ unicon.net) (azeckoski @ vt.edu)
 */
//@JsonIgnoreProperties({"configuration"})
public class PipelineConfig {

    private static final Logger logger = LoggerFactory.getLogger(PipelineConfig.class);

    /**
     * The pipeline XML file which was loaded to create this config
     */
    String filename;
    /**
     * the type of pipeline (e.g. marist_student_risk) this is the config for
     * (should be unique and should only use lowercase alphanums)
     */
    String type;
    /**
     * the display name for this pipeline (used in logging as well)
     */
    String name;
    String description;

    Map<String, Float> stats;

    List<BaseInputHandlerService> inputHandlers;
    List<InputField> inputs;
    List<Processor> processors;
    List<Output> outputs;

    /**
     * The list of reasons why the loaded pipeline config is not valid
     */
    List<String> invalidReasons;

    /**
     * The LAP config service
     */

    private PipelineConfig() {
        stats = new ConcurrentHashMap<>();
    }

    /**
     * Add an InputField to this config
     * @param inputField the InputField
     * @return the list of all current InputField
     */
    public List<InputField> addInputField(InputField inputField) {
        if (this.inputs == null) {
            this.inputs = new ArrayList<>();
        }
        for (InputField input : this.inputs) {
            if (inputField.name.equals(input.name)) {
                throw new IllegalArgumentException("Duplicate input field ("+inputField.name+"), input field can only be defined once");
            }
        }
        this.inputs.add(inputField);
        return this.inputs;
    }

    public List<BaseInputHandlerService> addInputHandlerField(String type, HierarchicalConfiguration sourceConfiguration, ConfigurationService configurationService, StorageService storage) {
        if (this.inputHandlers == null) {
            this.inputHandlers = new ArrayList<>();
        }

        this.inputHandlers.add(BaseInputHandlerService.getInputHandler(type, sourceConfiguration, configurationService, storage));
        return this.inputHandlers;
    }
    
    /**
     * Add a Processor to this config
     * @param processor the Processor
     * @return the list of all current Output
     */
    public List<Processor> addProcessor(Processor processor) {
        if (this.processors == null) {
            this.processors = new ArrayList<>();
        }
        this.processors.add(processor);
        return this.processors;
    }

    /**
     * Add an Output to this config
     * @param output the Output
     * @return the list of all current Output
     */
    public List<Output> addOutput(Output output) {
        if (this.outputs == null) {
            this.outputs = new ArrayList<>();
        }
        if (output.fields == null || output.fields.isEmpty()) {
            throw new IllegalArgumentException("Output must contain at LEAST 1 field (is empty or null currently) before it can be added");
        }
        this.outputs.add(output);
        return this.outputs;
    }

    // GETTERS
    public String getType() {
        return type;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public Map<String, ?> getStats() {
        return stats;
    }

    public List<InputField> getInputs() {
        return inputs;
    }

    public List<Processor> getProcessors() {
        return processors;
    }
    
    public List<BaseInputHandlerService> getInputHandlers() {
        return inputHandlers;
    }

    public List<Output> getOutputs() {
        return outputs;
    }

    /**
     * @return true if the pipeline config is valid, false otherwise
     * If not valid, the reasons are indicated in the #invalidReasons variable
     */
    public boolean isValid() {
        boolean valid = true;
        invalidReasons = new ArrayList<>();
        if (StringUtils.isBlank(type)) {
            invalidReasons.add("Missing <type> (must not be blank)");
            valid = false;
        }
        if (StringUtils.isBlank(name)) {
            invalidReasons.add("Missing <name> (must not be blank)");
            valid = false;
        }
        if (inputs == null) {
            invalidReasons.add("Missing <inputs> (must be included and at least one input defined)");
            valid = false;
        } else if (inputs.isEmpty()) {
            invalidReasons.add("No <input> in <inputs> (must have at least 1 input defined)");
            valid = false;
        }
        if (processors == null) {
            invalidReasons.add("Missing <processors> (must be included and at least one processor defined)");
            valid = false;
        } else if (processors.isEmpty()) {
            invalidReasons.add("No <processor> in <processors> (must have at least 1 processor defined)");
            valid = false;
        }
        if (outputs == null) {
            invalidReasons.add("Missing <outputs> (must be included and at least one output defined)");
            valid = false;
        } else if (outputs.isEmpty()) {
            invalidReasons.add("No <output> in <outputs> (must have at least 1 output defined)");
            valid = false;
        }
        return valid;
    }

    public List<String> getInvalidReasons() {
        return invalidReasons;
    }

    // BUILDER

    public static PipelineConfig makeConfigFromXML(ConfigurationService configurationService, StorageService storage, XMLConfiguration xmlConfig) {
        PipelineConfig pc = new PipelineConfig();
        pc.filename = xmlConfig.getFileName();
        pc.name = xmlConfig.getString("name");
        pc.type = xmlConfig.getString("type");
        pc.description = xmlConfig.getString("description");
        // special handling for stats metadata
        HierarchicalConfiguration stats = xmlConfig.configurationAt("stats");
        Iterator<String> statsKeys = stats.getKeys();
        while (statsKeys.hasNext()) {
            String next =  statsKeys.next();
            try {
                Float f = stats.getFloat(next);
                pc.stats.put(next, f);
            } catch (Exception e) {
                // skip this float and warn
                logger.warn("Unable to get float from "+next+" <stats> field (skipping it): "+e);
            }
        }
        
        // load the lists
        // sources
        List<HierarchicalConfiguration> sourceFields = xmlConfig.configurationsAt("sources.source");
        for (HierarchicalConfiguration field : sourceFields) {
            try {
                pc.addInputHandlerField(field.getString("type"), field, configurationService, storage);
            } catch (Exception e) {
                // skip this input and warn
                logger.warn("Unable to load input field ("+field.toString()+") (skipping it): "+e);
            }
        }
        
        // load the lists
        // inputs
        List<HierarchicalConfiguration> inputFields = xmlConfig.configurationsAt("inputs.fields.field");
        for (HierarchicalConfiguration field : inputFields) {
            try {
                pc.addInputField(InputField.make(field.getString("name"), field.getBoolean("required", false)));
            } catch (Exception e) {
                // skip this input and warn
                logger.warn("Unable to load input field ("+field.toString()+") (skipping it): "+e);
            }
        }
        // processors
        List<HierarchicalConfiguration> processors = xmlConfig.configurationsAt("processors.processor");
        for (HierarchicalConfiguration processor : processors) {
            try {
                String pType = processor.getString("type");
                Processor.ProcessorType pt =  Processor.ProcessorType.fromString(pType); // IllegalArgumentException if invalid
                if (pt == Processor.ProcessorType.KETTLE_JOB) {
                    pc.addProcessor(Processor.makeKettleJob(processor.getString("name"), processor.getString("file")));
                } else if (pt == Processor.ProcessorType.KETTLE_TRANSFORM) {
                    pc.addProcessor(Processor.makeKettleTransform(processor.getString("name"), processor.getString("file")));
                } else if (pt == Processor.ProcessorType.KETTLE_DATA) {
                    Processor p = new Processor();
                    p.type = Processor.ProcessorType.KETTLE_DATA;
                    p.name = processor.getString("name");
                    p.count = processor.getInt("count");
                    pc.addProcessor(p);
                    logger.warn("KETTLE DATA processor loaded ("+p.toString()+")");
                } else if (pt == Processor.ProcessorType.FAKE_DATA) {
                    Processor p = new Processor();
                    p.type = Processor.ProcessorType.FAKE_DATA;
                    p.name = processor.getString("name");
                    p.count = processor.getInt("count");
                    pc.addProcessor(p);
                    logger.warn("FAKE DATA processor loaded ("+p.toString()+")");
                } // Add other types here as needed
            } catch (Exception e) {
                // skip this processor and warn
                logger.warn("Unable to load processor ("+processor.toString()+") (skipping it): "+e);
            }
        }
        // outputs
        List<HierarchicalConfiguration> outputs = xmlConfig.configurationsAt("outputs.output");
        for (HierarchicalConfiguration output : outputs) {
          
          // TODO - we need to rethink output handling
          // don't want to add code every time we need to support a new output type
            try {
                String oType = output.getString("type");
                Output.OutputType ot =  Output.OutputType.fromString(oType); // IllegalArgumentException if invalid
                if (ot == Output.OutputType.CSV) {
                    Output o = Output.makeCSV(output.getString("from"), output.getString("filename"));
                    // load the output fields
                    List<HierarchicalConfiguration> outputFields = output.configurationsAt("fields.field");
                    for (HierarchicalConfiguration outputField : outputFields) {
                        o.addFieldCSV(outputField.getString("source"), outputField.getString("header"));
                    }
                    pc.addOutput(o);
                } else if (ot == Output.OutputType.STORAGE) {
                    Output o = Output.makeStorage(output.getString("from"), output.getString("to"));
                    // load the output fields
                    List<HierarchicalConfiguration> outputFields = output.configurationsAt("fields.field");
                    for (HierarchicalConfiguration outputField : outputFields) {
                        o.addFieldStorage(outputField.getString("source"), outputField.getString("target"));
                    }
                    pc.addOutput(o);
                } 
                else if (ot == Output.OutputType.SSPEARLYALERT) {
                  Output o = new Output();
                  o.type = Output.OutputType.SSPEARLYALERT;
                  o.from = output.getString("from");
                  o.to = output.getString("to");

                  List<HierarchicalConfiguration> outputFields = output.configurationsAt("fields.field");
                  for (HierarchicalConfiguration outputField : outputFields) {
                      OutputField field = new OutputField(o.type, outputField.getString("source"), outputField.getString("target"), null);
                      o.fields.add(field);
                  }
                  pc.addOutput(o);
                }
                // Add other types here as needed
            } catch (Exception e) {
                // skip this processor and warn
                logger.warn("Unable to load output ("+output.toString()+") (skipping it): "+e);
            }
        }
        return pc;
    }

    // Objects to hold specialized data

    /**
     * Represents a single field of input for a pipeline
     *
     * All inputs are defined in the resources/extracts/README.md file
     * There are 5 inputs types: PERSONAL, COURSE, ENROLLMENT, GRADE, ACTIVITY
     * A field is specified using a combination of the type and the name, for example: COURSE.COURSE_ID or PERSONAL.AGE
     */
    public static class InputField {
        public BaseInputHandlerService.InputCollection collection;
        public String name;
        public boolean required = false;

        private InputField() {}
        /**
         * For making input fields
         * @param collectionAndName the collection and name (period separated) of the temp storage field (e.g. PERSONAL.AGE)
         * @param required true if this field is required input
         * @return the input field object
         */
        public static InputField make(String collectionAndName, boolean required) {
            assert StringUtils.isNotBlank(collectionAndName);
            String[] parts = StringUtils.split(StringUtils.trim(collectionAndName),'.');
            if (parts == null || parts.length != 2) {
                throw new IllegalArgumentException("Cannot extract collection and name from input field (must follow format {COLL}.{NAME}): "+collectionAndName);
            }
            assert StringUtils.isNotBlank(parts[0]) : "InputField Collection part is blank: "+collectionAndName;
            assert StringUtils.isNotBlank(parts[1]) : "InputField Name part is blank: "+collectionAndName;
            InputField field = new InputField();
            field.collection = BaseInputHandlerService.InputCollection.fromString(parts[0]);
            field.name = parts[1];
            field.required = required;
            return field;
        }

        /**
         * @return the collection for this field (e.g. PERSONAL from PERSONAL.AGE)
         */
        public BaseInputHandlerService.InputCollection getCollection() {
            return collection;
        }

        /**
         * @return the collection for this field (e.g. PERSONAL from PERSONAL.AGE)
         */
        public String getName() {
            return name;
        }

        @Override
        public String toString() {
            return (required?"Required":"optional") + " input (" + collection + "." + name + ")";
        }
    }

}

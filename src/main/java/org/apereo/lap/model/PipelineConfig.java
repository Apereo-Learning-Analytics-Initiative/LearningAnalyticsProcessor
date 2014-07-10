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

import org.apache.commons.configuration.HierarchicalConfiguration;
import org.apache.commons.configuration.XMLConfiguration;
import org.apache.commons.lang.StringUtils;
import org.apereo.lap.services.InputHandlerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

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

    List<InputField> inputs;
    List<Processor> processors;
    List<Output> outputs;

    /**
     * The list of reasons why the loaded pipeline config is not valid
     */
    List<String> invalidReasons;

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

    public static PipelineConfig makeConfigFromXML(XMLConfiguration xmlConfig) {
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
                ProcessorType pt =  ProcessorType.fromString(pType); // IllegalArgumentException if invalid
                if (pt == ProcessorType.KETTLE) {
                    pc.addProcessor(Processor.makeKettle(processor.getString("name"), processor.getString("file")));
                } // Add other types here as needed
            } catch (Exception e) {
                // skip this processor and warn
                logger.warn("Unable to load processor ("+processor.toString()+") (skipping it): "+e);
            }
        }
        // outputs
        List<HierarchicalConfiguration> outputs = xmlConfig.configurationsAt("outputs.output");
        for (HierarchicalConfiguration output : outputs) {
            try {
                String oType = output.getString("type");
                OutputType ot =  OutputType.fromString(oType); // IllegalArgumentException if invalid
                if (ot == OutputType.CSV) {
                    Output o = Output.makeCSV(output.getString("from"), output.getString("filename"));
                    // load the output fields
                    List<HierarchicalConfiguration> outputFields = output.configurationsAt("fields.field");
                    for (HierarchicalConfiguration outputField : outputFields) {
                        o.addFieldCSV(outputField.getString("source"), outputField.getString("header"));
                    }
                    pc.addOutput(o);
                } else if (ot == OutputType.STORAGE) {
                    Output o = Output.makeStorage(output.getString("from"), output.getString("to"));
                    // load the output fields
                    List<HierarchicalConfiguration> outputFields = output.configurationsAt("fields.field");
                    for (HierarchicalConfiguration outputField : outputFields) {
                        o.addFieldStorage(outputField.getString("source"), outputField.getString("target"));
                    }
                    pc.addOutput(o);
                } // Add other types here as needed
            } catch (Exception e) {
                // skip this processor and warn
                logger.warn("Unable to load output ("+output.toString()+") (skipping it): "+e);
            }
        }

        // TODO build the config from the XML data
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
        public InputHandlerService.InputCollection collection;
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
            field.collection = InputHandlerService.InputCollection.fromString(parts[0]);
            field.name = parts[1];
            field.required = required;
            return field;
        }

        /**
         * @return the collection for this field (e.g. PERSONAL from PERSONAL.AGE)
         */
        public InputHandlerService.InputCollection getCollection() {
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

    /**
     * Defines a pipeline processor.
     * This is where all the work in the pipeline happens.
     */
    public static class Processor {
        public String name;
        public ProcessorType type;
        public String filename;

        private Processor() {}

        /**
         * Create a Pentaho Kettle based processor object
         * @param name the name of this part of the processor (mostly for logging and visuals)
         * @param filename the complete path (or relative from the pipelines directory) to the kettle ktr or kjb xml file
         * @return the processor object
         */
        public static Processor makeKettle(String name, String filename) {
            assert StringUtils.isNotBlank(name);
            assert StringUtils.isNotBlank(filename);
            Processor obj = new Processor();
            obj.type = ProcessorType.KETTLE;
            obj.name = name;
            obj.filename = filename;
            return obj;
        }

        @Override
        public String toString() {
            return "Processor{" +
                    type + ", name='" + name + '\'' +
                    ", filename='" + filename + '\'' +
                    '}';
        }
    }

    /**
     * Represents the possible processor types
     */
    public static enum ProcessorType {
        /**
         * A Pentaho Kettle based processor
         */
        KETTLE;
        static ProcessorType fromString(String str) {
            if (StringUtils.equalsIgnoreCase(str, KETTLE.name())) {
                return KETTLE;
            } else {
                throw new IllegalArgumentException("processor type ("+str+") does not match the valid types: KETTLE");
            }
        }
    }

    /**
     * Represents a type of output from a pipeline
     * The processed data from a pipeline is flushed completely after it completes
     * so the outputs allow some data to be saved
     */
    public static class Output {
        public OutputType type;
        public String from;
        public String to;
        public String filename;
        public List<OutputField> fields;

        private Output() {
            fields = new LinkedList<>();
        }

        /**
         * Create a STORAGE based output by copying
         * (copies data from temporary to persistent storage)
         * @param from the name of the Table or Collection in the temporary storage to copy from
         * @param to the name of the Table or Collection in the persistent storage to copy to
         * @return the output object
         */
        public static Output makeStorage(String from, String to) {
            assert StringUtils.isNotBlank(from);
            assert StringUtils.isNotBlank(to);
            Output obj = new Output();
            obj.type = OutputType.STORAGE;
            obj.from = from;
            obj.to = to;
            return obj;
        }

        /**
         * Create a STORAGE based output by copying data from temporary storage to a CSV
         * @param from the name of the Table or Collection in the temporary storage to copy from
         * @param filename the name of the CSV file to copy into
         * @return the output object
         */
        public static Output makeCSV(String from, String filename) {
            assert StringUtils.isNotBlank(from);
            assert StringUtils.isNotBlank(filename);
            Output obj = new Output();
            obj.type = OutputType.CSV;
            obj.from = from;
            obj.filename = filename;
            return obj;
        }


        /**
         * Adds an output field to place in the persistent storage from the temporary storage
         * @param source the name of the temp storage field (e.g. AGE)
         * @param target the name of the persistent storage field (e.g. USER_ID_ALT)
         * @return the output field object
         */
        public OutputField addFieldStorage(String source, String target) {
            if (this.type != OutputType.STORAGE) {
                throw new IllegalStateException("Can only add Storage fields to a STORAGE type object, this type is: "+this.type);
            }
            OutputField field = new OutputField(this.type, source, target, null);
            this.fields.add(field);
            return field;
        }

        /**
         * Adds an output field to place in a CSV file
         * @param source the name of the temp storage field (e.g. PERSONAL.AGE)
         * @param header the name of the CSV header for this field
         * @return the output field object
         */
        public OutputField addFieldCSV(String source, String header) {
            if (this.type != OutputType.CSV) {
                throw new IllegalStateException("Can only add CSV fields to a CSV type object, this type is: "+this.type);
            }
            OutputField field = new OutputField(this.type, source, null, header);
            this.fields.add(field);
            return field;
        }
    }

    /**
     * Represents a single field of output for a pipeline
     *
     * Can output to persistent storage or a CSV file (for now)
     */
    public static class OutputField {
        public OutputType type;
        public String source;
        public String target;
        public String header;

        protected OutputField() {}

        protected OutputField(OutputType type, String source, String target, String header) {
            assert type != null;
            assert source != null;
            this.type = type;
            this.source = source;
            this.target = target;
            this.header = header;
        }
    }

    /**
     * Represents the possible output types
     */
    public static enum OutputType {
        /**
         * Output into the persistent storage
         * (tables/collections must already be defined)
         */
        STORAGE,
        /**
         * Output into a CSV file in the default location
         */
        CSV;
        static OutputType fromString(String str) {
            if (StringUtils.equalsIgnoreCase(str, STORAGE.name())) {
                return STORAGE;
            } else if (StringUtils.equalsIgnoreCase(str, CSV.name())) {
                return CSV;
            } else {
                throw new IllegalArgumentException("Output type ("+str+") does not match the valid types: CSV,STORAGE");
            }
        }
    }
}

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

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;

/**
 * Defines a pipeline processor.
 * This is where all the work in the pipeline happens.
 */
public class Processor {
    public ProcessorType type;
    /**
     * The name of this processor (for printing mostly)
     */
    public String name;
    /**
     * The filename related to this processor (e.g. kettle kjb)
     */
    public String filename;
    /**
     * The configured count (usually for iterations)
     */
    public int count;

    Processor() {}

    /**
     * Create a Pentaho Kettle based processor object
     * @param name the name of this part of the processor (mostly for logging and visuals)
     * @param filename the complete path (or relative from the pipelines directory) to the kettle ktr or kjb xml file
     * @return the processor object
     */
    public static Processor makeKettleJob(String name, String filename) {
        assert StringUtils.isNotBlank(name);
        assert StringUtils.isNotBlank(filename);
        Processor obj = new Processor();
        obj.type = ProcessorType.KETTLE_JOB;
        obj.name = name;
        obj.filename = filename;
        return obj;
    }

    public static Processor makeKettleTransform(String name, String filename) {
        Processor obj = makeKettleJob(name, filename);
        obj.type = ProcessorType.KETTLE_TRANSFORM;
        return obj;
    }

    @Override
    public String toString() {
        return "Processor{" +
                type + ", name='" + name + '\'' +
                ", filename='" + filename + '\'' +
                '}';
    }

    /**
     * Represents the possible processor types
     */
    public static enum ProcessorType {
        /**
         * A Pentaho Kettle Job processor
         */
        KETTLE_JOB,
        /**
         * A Pentaho Kettle transform processor
         */
        KETTLE_TRANSFORM,
        /**
         * Pentaho Kettle output
         */
        KETTLE_DATA,
        /**
         * This processor just produces Fake data in a "FAKE_DATA" table
         */
        FAKE_DATA;
        static ProcessorType fromString(String str) {
            if (StringUtils.equalsIgnoreCase(str, FAKE_DATA.name())) {
                return FAKE_DATA;
            } else if (StringUtils.equalsIgnoreCase(str, KETTLE_JOB.name())) {
                    return KETTLE_JOB;
            } else if (StringUtils.equalsIgnoreCase(str, KETTLE_TRANSFORM.name())) {
                return KETTLE_TRANSFORM;
            } else if (StringUtils.equalsIgnoreCase(str, KETTLE_DATA.name())) {
                return KETTLE_DATA;
            }else {
                throw new IllegalArgumentException("processor type ("+str+") does not match the valid types: "+ ArrayUtils.toString(values()));
            }
        }
    }
}

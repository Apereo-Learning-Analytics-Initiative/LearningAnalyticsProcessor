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
package org.apereo.lap.model;

import org.apache.commons.lang.StringUtils;

import java.util.*;

/**
 * Represents a type of output from a pipeline
 * The processed data from a pipeline is flushed completely after it completes
 * so the outputs allow some data to be saved
 *
 * @author Aaron Zeckoski (azeckoski @ unicon.net) (azeckoski @ vt.edu)
 */
public class Output {
    public OutputType type;
    public String from;
    public String to;
    public String filename;
    public List<OutputField> fields;

    Output() {
        fields = new LinkedList<>();
    }

    public String getName() {
        return type+":"+from+"->"+(to!=null?to:filename);
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
        //assert StringUtils.isNotBlank(to);
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
     * @return SQL to run for this output to count the item to output and verify table is accessible
     */
    public String makeTempDBCheckSQL() {
        return "SELECT COUNT(*) as COUNT FROM "+this.from;
    }

    /**
     * @return SQL to run for this output to retrieve the columns
     */
    public String makeTempDBSelectSQL() {
        List<String> columns = makeSourceColumns();
        for (int i = 0; i < columns.size(); i++) {
            String column = columns.get(i);
            columns.set(i, column+" AS "+StringUtils.upperCase(column));
        }
        String columnsSQL = StringUtils.join(columns, ",");
        return "SELECT "+columnsSQL+" FROM "+this.from;
    }

    /**
     * @return the list of source columns from the temp DB (in defined order)
     */
    public List<String> makeSourceColumns() {
        ArrayList<String> columns = new ArrayList<>();
        for (OutputField field : fields) {
            columns.add(field.source);
        }
        return columns;
    }

    /**
     * @return the list of target names (in defined order) - e.g. table column names or CSV headers
     */
    public List<String> makeTargetNames() {
        ArrayList<String> columns = new ArrayList<>();
        for (OutputField field : fields) {
            columns.add(field.getTarget());
        }
        return columns;
    }

    /**
     * @return the ordered map of source -> target (e.g. source column name to target header name),
     *      the source name is uppercase to match with the select template
     */
    public Map<String, String> makeSourceTargetMap() {
        LinkedHashMap<String, String> sourceTarget = new LinkedHashMap<>();
        for (OutputField field : fields) {
            sourceTarget.put(StringUtils.upperCase(field.source), field.getTarget());
        }
        return sourceTarget;
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

    @Override
    public String toString() {
        return this.getName();
    }

    /**
     * Represents the possible output types
     */
    public static enum OutputType {
        SSPEARLYALERT,
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
            } 
            else if (StringUtils.equalsIgnoreCase(str, CSV.name())) {
                return CSV;
            } 
            else if (StringUtils.equalsIgnoreCase(str, SSPEARLYALERT.name())) {
              return SSPEARLYALERT;
            }
            else {
                throw new IllegalArgumentException("Output type ("+str+") does not match the valid types: CSV,STORAGE");
            }
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

        public String getTarget() {
            return this.target != null ? this.target : this.header;
        }
    }

}

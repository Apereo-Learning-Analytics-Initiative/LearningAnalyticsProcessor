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
package org.apereo.lap.services.output;

import au.com.bytecode.opencsv.CSVWriter;
import org.apache.commons.io.IOUtils;
import org.apereo.lap.model.Output;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.stereotype.Component;

import java.io.*;
import java.util.Map;

/**
 * Handles the output processing for a single target output type
 *
 * @author Aaron Zeckoski (azeckoski @ unicon.net) (azeckoski @ vt.edu)
 */
@Component
public class CSVOutputHandler extends BaseOutputHandler implements OutputHandler {

    static final Logger logger = LoggerFactory.getLogger(CSVOutputHandler.class);

    @Override
    public Output.OutputType getHandledType() {
        return Output.OutputType.CSV;
    }

    @Override
    public OutputResult writeOutput(Output output) {
        OutputResult result = new OutputResult(output);
        // make sure we can write the CSV
        File csv = new File(configuration.getOutputDirectory(), output.filename);
        boolean created;
        try {
            created = csv.createNewFile();
            if (logger.isDebugEnabled()) logger.debug("CSV file created ("+created+"): "+csv.getAbsolutePath());
        } catch (IOException e) {
            throw new IllegalStateException("Exception creating CSV file: "+csv.getAbsolutePath()+": "+e, e);
        }
        if (!created) { // created file is going to be a writeable file so no check needed
            if (csv.isFile() && csv.canRead() && csv.canWrite()) {
                // file exists and we can write to it
                if (logger.isDebugEnabled()) logger.debug("CSV file is writeable: "+csv.getAbsolutePath());
            } else {
                throw new IllegalStateException("Cannot write to the CSV file: " + csv.getAbsolutePath());
            }
        }

        // make sure we can read from the temp data source
        try {
            int rows = storage.getTempJdbcTemplate().queryForObject(output.makeTempDBCheckSQL(), Integer.class);
            logger.info("Preparing to output "+rows+" from temp table "+output.from+" to "+output.filename);
        } catch (Exception e) {
            throw new RuntimeException("Failure while trying to count the output data rows: "+output.makeTempDBCheckSQL());
        }

        Map<String, String> sourceToHeaderMap = output.makeSourceTargetMap();
        String selectSQL = output.makeTempDBSelectSQL();

        // fetch the data to write to CSV
        SqlRowSet rowSet;
        try {
            // for really large data we probably need to use http://docs.spring.io/spring/docs/3.0.x/api/org/springframework/jdbc/core/RowCallbackHandler.html
            rowSet = storage.getTempJdbcTemplate().queryForRowSet(selectSQL);
        } catch (Exception e) {
            throw new RuntimeException("Failure while trying to retrieve the output data set: "+selectSQL);
        }

        // write data to the CSV file
        int lines = 0;
        PrintWriter pw = null;
        try {
            pw = new PrintWriter(new BufferedWriter(new FileWriter(csv, true)));
            CSVWriter writer = new CSVWriter(pw);

            // write out the header
            writer.writeNext( sourceToHeaderMap.values().toArray(new String[sourceToHeaderMap.size()]) );

            // write out the rows
            while (rowSet.next()) {
                String[] rowVals = new String[sourceToHeaderMap.size()];
                for (int i = 0; i < sourceToHeaderMap.size(); i++) {
                    rowVals[i] = (rowSet.wasNull() ? null : rowSet.getString(i+1));
                }
                writer.writeNext(rowVals);
            }

            IOUtils.closeQuietly(writer);
        } catch (Exception e) {
            throw new RuntimeException("Failure writing output to CSV ("+csv.getAbsolutePath()+"): "+e, e);
        } finally {
            IOUtils.closeQuietly(pw);
        }

        result.done(lines, null);
        return result;
    }

}

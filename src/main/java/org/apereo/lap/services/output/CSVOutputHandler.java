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
import org.springframework.stereotype.Component;

import java.io.*;

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
        // TODO

        // write data to the CSV file
        int lines = 0;
        PrintWriter pw = null;
        try {
            pw = new PrintWriter(new BufferedWriter(new FileWriter(csv, true)));
            CSVWriter writer = new CSVWriter(pw);

            // TODO real writing here
            writer.writeNext(new String[] {"AZ","testing","CSV","1"});
            writer.writeNext(new String[] {"AZ","testing","CSV","2"});
            writer.writeNext(new String[] {"AZ","testing","CSV","3"});
            lines = 3;
            // TODO end fake stuff

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

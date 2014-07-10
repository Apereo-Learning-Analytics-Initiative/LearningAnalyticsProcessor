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

import org.apereo.lap.model.Output;

import java.util.ArrayList;
import java.util.Date;

/**
 * Handles the output processing for a single target output type
 *
 * @author Aaron Zeckoski (azeckoski @ unicon.net) (azeckoski @ vt.edu)
 */
public interface OutputHandler {

    /**
     * @return the name of the data type handled by this handler
     */
    Output.OutputType getHandledType();

    /**
     * Process this output request
     * @param output the output config
     * @return the results of the output processing
     */
    OutputResult writeOutput(Output output);

    public static class OutputResult {
        public String name;
        public Output.OutputType handledType;
        public Output output;
        public long totalTimeMS;
        public long startTimeMS;
        public long endTimeMS;
        public int total = 0;
        public int loaded = 0;
        public int failed = 0;
        public ArrayList<String> failures;

        public OutputResult(Output output) {
            this.output = output;
            this.handledType = output.type;
            this.name = output.getName();
            startTimeMS = System.currentTimeMillis();
        }

        public void done(int itemsCount, ArrayList<String> failures) {
            this.total = itemsCount;
            this.failures = failures;
            this.failed = failures != null ? failures.size() : 0;
            this.loaded = total - this.failed;
            this.endTimeMS = System.currentTimeMillis();
            this.totalTimeMS = this.endTimeMS - this.startTimeMS;
        }

        @Override
        public String toString() {
            return "OutputResult:" + name +
                    ", total=" + total +
                    ", loaded=" + loaded +
                    ", failed=" + failed +
                    ", runSecs=" + String.format("%.2f", totalTimeMS/1000f) +
                    ", started=" + new Date(startTimeMS);
        }
    }

}

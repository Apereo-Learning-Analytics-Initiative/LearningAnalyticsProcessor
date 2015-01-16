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
package org.apereo.lap.services.input;

import org.apereo.lap.services.SampleCSVInputHandlerService;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.ArrayList;
import java.util.Date;

/**
 * Handles the input processing for a single input data source
 *
 * @author Aaron Zeckoski (azeckoski @ unicon.net) (azeckoski @ vt.edu)
 */
public interface InputHandler {

    /**
     * @return the data type handled by this handler
     */
    SampleCSVInputHandlerService.InputType getHandledType();

    /**
     * @return the collection type handled by this handler
     */
    SampleCSVInputHandlerService.InputCollection getHandledCollection();

    /**
     * @return the JdbcTemplate for the temp database (get this from the StorageService)
     */
    JdbcTemplate getTempDatabase();

    /**
     * @return the insert SQL statement with ? vars for the data type handled
     */
    String makeInsertSQL();

    /**
     * @return the insert param types - Types.* constants - e.g. Types.VARCHAR (represented by ? in the insert SQL)
     * MUST be in the same order as the insert SQL statement
     */
    int[] makeInsertSQLParams();

    /**
     * @param params array of field parameters for the data type handled
     * @return array of objects (converted and validated) which represent the same object
     * MUST be in the same order as the insert SQL statement (and insert param types)
     */
    Object[] validateAndConvertParams(String[] params);

    /**
     * Read the handler input data completely and load the data into the database
     * @return the results of the processing with details of how many items loaded etc. (item failures are recorded but will not stop the processing)
     */
    ReadResult readInputIntoDB();

    public static class ReadResult {
        public String handledType;
        public long totalTimeMS;
        public long startTimeMS;
        public long endTimeMS;
        public int total = 0;
        public int loaded = 0;
        public int failed = 0;
        public ArrayList<String> failures;

        public ReadResult(String handledType) {
            this.handledType = handledType;
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
            return "ReadResult:" + handledType +
                    ", total=" + total +
                    ", loaded=" + loaded +
                    ", failed=" + failed +
                    ", runSecs=" + String.format("%.2f", totalTimeMS/1000f) +
                    ", started=" + new Date(startTimeMS);
        }
    }

}

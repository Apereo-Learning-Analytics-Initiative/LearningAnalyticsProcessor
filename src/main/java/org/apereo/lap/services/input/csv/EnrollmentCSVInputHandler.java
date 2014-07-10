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
package org.apereo.lap.services.input.csv;

import au.com.bytecode.opencsv.CSVReader;
import org.apereo.lap.services.ConfigurationService;
import org.springframework.jdbc.core.JdbcTemplate;

import java.sql.Types;

public class EnrollmentCSVInputHandler extends BaseCSVInputHandler {

    public static final String FILENAME = "enrollment.csv";

    static final String SQL_INSERT = "INSERT INTO ENROLLMENT (ALTERNATIVE_ID,COURSE_ID,FINAL_GRADE,WITHDRAWL_DATE) VALUES (?,?,?,?)";

    static final int[] SQL_TYPES = new int[] {
            // ALTERNATIVE_ID,COURSE_ID,FINAL_GRADE,WITHDRAWL_DATE
            Types.VARCHAR, Types.VARCHAR, Types.VARCHAR, Types.TIMESTAMP
    };

    public EnrollmentCSVInputHandler(ConfigurationService configuration, JdbcTemplate jdbcTemplate) {
        super(configuration, jdbcTemplate);
    }

    @Override
    public String makeInsertSQL() {
        return SQL_INSERT;
    }

    @Override
    public int[] makeInsertSQLParams() {
        return SQL_TYPES;
    }

    @Override
    public String getCSVFilename() {
        return FILENAME;
    }

    @Override
    public CSVReader readCSV(boolean reRead) {
        return readCSV(4, "ALTERNATIVE_ID", reRead);
    }

    @Override
    public ReadResult readInputIntoDB() {
        CSVReader reader = readCSV(false);
        return readCSVFileIntoDB(reader);
    }

    @Override
    public Object[] validateAndConvertParams(String[] csvLine) {
        assert csvLine != null && csvLine.length > 0;
        Object[] params = new Object[csvLine.length];
        params[0] = parseString(csvLine[0], null, true, "ALTERNATIVE_ID");
        params[1] = parseString(csvLine[1], null, true, "COURSE_ID");
        params[2] = parseString(csvLine[2], new String[] {"A","A-","B+","B","B-","C+","C","C-","D+","D","D-","F","I","W"}, false, "FINAL_GRADE");
        params[3] = parseDateTime(csvLine[3], false, "WITHDRAWL_DATE");
        return params;
    }

}

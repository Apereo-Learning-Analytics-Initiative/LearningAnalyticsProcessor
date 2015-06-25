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
package org.apereo.lap.services.input.handlers.csv;

import java.sql.Types;

import org.apereo.lap.services.configuration.ConfigurationService;
import org.apereo.lap.services.input.handlers.InputHandler.ReadResult;
import org.springframework.jdbc.core.JdbcTemplate;

import au.com.bytecode.opencsv.CSVReader;

public class PersonalCSVInputHandler extends BaseCSVInputHandler {

    static final String SQL_INSERT = "INSERT INTO PERSONAL (ALTERNATIVE_ID,PERCENTILE,SAT_VERBAL,SAT_MATH,ACT_COMPOSITE,AGE,RACE,GENDER,STATUS,EARNED_CREDIT_HOURS,GPA_CUMULATIVE,GPA_SEMESTER,STANDING,PELL_STATUS,CLASS_CODE) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";

    static final int[] SQL_TYPES = new int[] {
            // ALTERNATIVE_ID,PERCENTILE,SAT_VERBAL,SAT_MATH,ACT_COMPOSITE,
            Types.VARCHAR, Types.FLOAT, Types.INTEGER, Types.INTEGER, Types.INTEGER,
            // AGE,RACE,GENDER,STATUS,EARNED_CREDIT_HOURS,
            Types.INTEGER, Types.INTEGER, Types.INTEGER, Types.INTEGER, Types.INTEGER,
            // GPA_CUMULATIVE,GPA_SEMESTER,STANDING,PELL_STATUS,CLASS
            Types.FLOAT, Types.FLOAT, Types.INTEGER, Types.BOOLEAN, Types.VARCHAR
    };

    public PersonalCSVInputHandler(ConfigurationService configuration, JdbcTemplate jdbcTemplate) {
        super(configuration, jdbcTemplate);
    }
    
	@Override
	public int getOrder() {
		return 1;
	}
    @Override
    public String getFileName() {
    	return "personal.csv";
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
    public CSVReader readCSV(boolean reRead) {
        return readCSV(14, "ALTERNATIVE_ID", reRead);
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
        params[1] = parseInt(csvLine[1], 0, 100, false, "PERCENTILE");
        params[2] = parseInt(csvLine[2], 300, 800, false, "SAT_VERBAL");
        params[3] = parseInt(csvLine[3], 300, 800, false, "SAT_MATH");
        params[4] = parseInt(csvLine[4], 11, 36, false, "ACT_COMPOSITE");
        params[5] = parseInt(csvLine[5], 1, 150, true, "AGE");
        params[6] = parseString(csvLine[6], null, false, "RACE"); // RACE
        params[7] = (parseString(csvLine[7], new String[]{"M","F","N"}, false, "GENDER").equalsIgnoreCase("F") ? 1 : 2);
        csvLine[8] = parseString(csvLine[8], new String[]{"FT","F","PT","P"}, false, "ENROLLMENT_STATUS");
        params[8] = ((csvLine[8].equals("PT") || csvLine[8].equals("P")) ? 2 : 1); // ENROLLMENT_STATUS
        params[9] = parseInt(csvLine[9], 0, 600, false, "EARNED_CREDIT_HOURS");
        params[10] = parseFloat(csvLine[10], 0f, 4f, false, "GPA_CUMULATIVE");
        params[11] = parseFloat(csvLine[11], 0f, 4f, false, "GPA_SEMESTER");
        params[12] = parseInt(csvLine[12], 0, 2, false, "STANDING");
        params[13] = parseBoolean(csvLine[13], false, "PELL_STATUS"); // PELL_STATUS
        params[14] = parseString(csvLine[14], new String[]{"FR", "SO", "JR", "SR", "GR"}, true, "CLASS_CODE");
        return params;
    }
}

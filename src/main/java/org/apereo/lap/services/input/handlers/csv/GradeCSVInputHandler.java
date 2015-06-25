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

public class GradeCSVInputHandler extends BaseCSVInputHandler {

    public static final String FILENAME = "grade.csv";

    static final String SQL_INSERT = "INSERT INTO GRADE (ALTERNATIVE_ID,COURSE_ID,GRADABLE_OBJECT,CATEGORY,MAX_POINTS,EARNED_POINTS,WEIGHT,GRADE_DATE) VALUES (?,?,?,?,?,?,?,?)";

    static final int[] SQL_TYPES = new int[] {
            // ALTERNATIVE_ID,COURSE_ID,GRADABLE_OBJECT,CATEGORY,
            Types.VARCHAR, Types.VARCHAR, Types.VARCHAR, Types.VARCHAR,
            // MAX_POINTS,EARNED_POINTS,WEIGHT,GRADE_DATE
            Types.INTEGER, Types.INTEGER, Types.FLOAT, Types.TIMESTAMP
    };

    public GradeCSVInputHandler(ConfigurationService configuration, JdbcTemplate jdbcTemplate) {
        super(configuration, jdbcTemplate);
    }
    
	@Override
	public int getOrder() {
		return 4;
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
    public String getFileName() {
    	return "grade.csv";
    }
    
    @Override
    public CSVReader readCSV(boolean reRead) {
        return readCSV(8, "ALTERNATIVE_ID", reRead);
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
        params[2] = parseString(csvLine[2], null, true, "GRADABLE_OBJECT");
        params[3] = parseString(csvLine[3], null, false, "CATEGORY");
        params[4] = parseFloat(csvLine[4], 0f, 1000f, false, "MAX_POINTS"); // default 0
        params[5] = parseFloat(csvLine[5], 0f, 1000f, false, "EARNED_POINTS"); // default 0
        params[6] = parseFloat(csvLine[6], 0f, 1f, false, "WEIGHT");
        params[7] = parseDateTime(csvLine[7], false, "GRADE_DATE");
        return params;
    }

}

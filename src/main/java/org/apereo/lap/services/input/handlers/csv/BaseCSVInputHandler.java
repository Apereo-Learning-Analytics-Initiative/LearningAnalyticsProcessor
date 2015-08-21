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

import au.com.bytecode.opencsv.CSVReader;
import org.apache.commons.lang.StringUtils;
import org.apereo.lap.services.configuration.ConfigurationService;
import org.apereo.lap.services.input.BaseInputHandlerService;
import org.apereo.lap.services.input.SampleCSVInputHandlerService;
import org.apereo.lap.services.input.handlers.BaseInputHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;


public abstract class BaseCSVInputHandler extends BaseInputHandler implements CSVInputHandler {

	private String path = null;
    private static final Logger logger = LoggerFactory.getLogger(BaseCSVInputHandler.class);
    ConfigurationService config;

    public BaseCSVInputHandler(ConfigurationService configuration, JdbcTemplate jdbcTemplate) {
        super(jdbcTemplate);
        assert configuration != null;
        this.config = configuration;
    }

    /**
     * Make the set of all known CSV handlers (this is expensive so avoid doing it more than once)
     * @param configuration current system config
     * @param jdbcTemplate temp DB jdbc template
     * @return the map of csv filename -> CSVInputHandler (in the correct order for processing and insertion)
     */
    public static Map<String, CSVInputHandler> makeCSVHandlers(ConfigurationService configuration, JdbcTemplate jdbcTemplate) {
        // build up the handlers
    	CSVInputHandler csvih;
        Map<String, CSVInputHandler> handlers = new LinkedHashMap<>(); // maintain order
        csvih = new SamplePersonalCSVInputHandler(configuration, jdbcTemplate);
        handlers.put(csvih.getPath(), csvih);
        csvih = new SampleCourseCSVInputHandler(configuration, jdbcTemplate);
        handlers.put(csvih.getPath(), csvih);
        csvih = new SampleEnrollmentCSVInputHandler(configuration, jdbcTemplate);
        handlers.put(csvih.getPath(), csvih);
        csvih = new SampleGradeCSVInputHandler(configuration, jdbcTemplate);
        handlers.put(csvih.getPath(), csvih);
        csvih = new SampleActivityCSVInputHandler(configuration, jdbcTemplate);
        handlers.put(csvih.getPath(), csvih);
        return handlers;
    }

    @Override
    public SampleCSVInputHandlerService.InputType getHandledType() {
        return BaseInputHandlerService.InputType.CSV;
    }

    // general use functions

    CSVReader reader;

    /**
     * Reads a CSV file and verifies basic infor about it
     * @param minColumns min number of columns
     * @param headerStartsWith expected header value
     * @param reRead force reading the file again (otherwise it will use the existing copy)
     * @return the CSVReader
     * @throws IllegalStateException if we fail to produce the reader
     */
    CSVReader readCSV(int minColumns, String headerStartsWith, boolean reRead) {
        return readCSVResource(minColumns, headerStartsWith, reRead);
    }


    /**
     * Reads a CSV file and verifies basic infor about it
     * @param minColumns min number of columns
     * @param headerStartsWith expected header value
     * @param reRead force reading the file again (otherwise it will use the existing copy)
     * @return the CSVReader
     * @throws IllegalStateException if we fail to produce the reader
     */
    CSVReader readCSVResource(int minColumns, String headerStartsWith, boolean reRead) {
        if (this.reader == null || reRead) {
            assert minColumns > 0 : "minColumns must be > 0: "+minColumns;
            assert StringUtils.isNotBlank(headerStartsWith) : "headerStartsWith must not be blank: "+ getPath();
            CSVReader fileCSV;
            try {
                Path csvPath = config.getApplicationHomeDirectory().resolve(Paths.get(getPath()));
                InputStream fileCSV_IS = Files.newInputStream(csvPath);
                fileCSV = new CSVReader(new InputStreamReader(fileCSV_IS));
                String[] check = fileCSV.readNext();
                if (check != null
                        && check.length >= minColumns
                        && StringUtils.startsWithIgnoreCase(headerStartsWith,StringUtils.trimToEmpty(check[0]))
                        ) {
                    //logger.debug(fileName+" file and header appear valid");
                    this.reader = fileCSV;
                } else {
                    throw new IllegalStateException(getPath()+" file and header do not appear valid (no "+headerStartsWith+" header or less than "+minColumns+" required columns");
                }
            } catch (Exception e) {
                throw new IllegalStateException(getPath()+" CSV is invalid: "+e);
            }
        }
        return this.reader;
    }

    /**
     * @param csvReader the csvReader used to read the file
     * @return the results of the processing (line failures are recorded but will not stop the processing)
     * @throws IllegalArgumentException if the reader cannot be read
     */
    ReadResult readCSVFileIntoDB(CSVReader csvReader) {
        ReadResult result = new ReadResult(getPath());
        String insertSQL = makeInsertSQL();
        int[] insertTypes = makeInsertSQLParams();

        int line = 1;
        String[] csvLine;
        ArrayList<String> failures = new ArrayList<>();
        try {
            while ((csvLine = csvReader.readNext()) != null) { // IOException
                line++;
                try {
                    trimStringArrayToNull(csvLine);
                    Object[] params = validateAndConvertParams(csvLine);
                    getTempDatabase().update(insertSQL, params, insertTypes);
                } catch (Exception e) {
                    String msg = getHandledType()+" line "+line+": "+e.getMessage();
                    if (logger.isDebugEnabled()) logger.debug(msg, e); // to help in fixing the problem
                    failures.add(msg);
                }
            }
        } catch (IOException e) {
            throw new IllegalArgumentException("csvReader cannot read from file: "+e,e);
        }
        result.done(line, failures);
        return result;
    }

    @Override
    public String getPath() {
        return path;
    }

    @Override
    public void setPath(String path) {
        this.path = path;
    }
}

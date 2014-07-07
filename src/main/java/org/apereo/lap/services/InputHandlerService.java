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
package org.apereo.lap.services;

import au.com.bytecode.opencsv.CSVReader;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.annotation.Resource;
import java.io.*;
import java.sql.Types;
import java.util.ArrayList;

/**
 * Handles the inputs by reading the data into the temporary data storage
 * Validates the inputs and ensures the data is available to the pipeline processor
 * 
 * @author Aaron Zeckoski (azeckoski @ unicon.net) (azeckoski @ vt.edu)
 */
@Component
public class InputHandlerService {

    private static final Logger logger = LoggerFactory.getLogger(InputHandlerService.class);

    @Resource
    ConfigurationService configuration;

    @Resource
    StorageService storage;

    @PostConstruct
    public void init() {
        logger.info("INIT");
        if (configuration.config.getBoolean("input.copy.samples", false)) {
            copySampleExtractCSVs();
        }
        if (configuration.config.getBoolean("input.init.load.csv", false)) {
            loadCSVs();
        }
    }

    @PreDestroy
    public void destroy() {
        logger.info("DESTROY");
    }

    /**
     * Copies the 5 sample extract CSVs from the classpath to the inputs directory
     */
    public void copySampleExtractCSVs() {
        logger.info("copySampleExtractCSVs start");
        copySampleCSV("extracts/", "personal.csv");
        copySampleCSV("extracts/", "course.csv");
        copySampleCSV("extracts/", "enrollment.csv");
        copySampleCSV("extracts/", "grade.csv");
        copySampleCSV("extracts/", "activity.csv");
        logger.info("copySampleExtractCSVs to "+configuration.inputDirectory.getAbsolutePath()+" complete");
    }

    /**
     * Loads and verifies the 5 standard CSVs from the inputs directory
     */
    public void loadCSVs() {
        logger.info("load CSV files from: "+configuration.inputDirectory.getAbsolutePath());
        // TODO init CSV files
        try {
            CSVReader personalCSV = loadCSV("personal.csv", 15, "ALTERNATIVE_ID");
            CSVReader courseCSV = loadCSV("course.csv", 4, "COURSE_ID");
            CSVReader enrollmentCSV = loadCSV("enrollment.csv", 4, "ALTERNATIVE_ID");
            CSVReader gradeCSV = loadCSV("grade.csv", 8, "ALTERNATIVE_ID");
            CSVReader activityCSV = loadCSV("activity.csv", 4, "ALTERNATIVE_ID");

            int[] types = new int[] {
                    // ALTERNATIVE_ID,PERCENTILE,SAT_VERBAL,SAT_MATH,ACT_COMPOSITE,
                    Types.VARCHAR, Types.FLOAT, Types.INTEGER, Types.INTEGER, Types.INTEGER,
                    // AGE,RACE,GENDER,STATUS,EARNED_CREDIT_HOURS,
                    Types.INTEGER, Types.INTEGER, Types.INTEGER, Types.INTEGER, Types.INTEGER,
                    // GPA_CUMULATIVE,GPA_SEMESTER,STANDING,PELL_STATUS,CLASS
                    Types.FLOAT, Types.FLOAT, Types.INTEGER, Types.BOOLEAN, Types.VARCHAR
            };

            // load the content into the temp DB schema
            int line = 1;
            int loaded = 0;
            String file = "personal.csv";
            String[] csvLine;
            ArrayList<String> failures = new ArrayList<String>();
            while ((csvLine = personalCSV.readNext()) != null) {
                line++;
                try {
                    for (int i = 0; i < csvLine.length; i++) {
                        csvLine[i] = StringUtils.trimToNull(csvLine[i]);
                    }
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
                    storage.getTempJdbcTemplate().update("INSERT INTO PERSONAL (ALTERNATIVE_ID,PERCENTILE,SAT_VERBAL,SAT_MATH,ACT_COMPOSITE,AGE,RACE,GENDER,STATUS,EARNED_CREDIT_HOURS,GPA_CUMULATIVE,GPA_SEMESTER,STANDING,PELL_STATUS,CLASS_CODE) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)", params, types);
                    loaded++;
                } catch (Exception e) {
                    String msg = file+" line "+line+": "+e.getMessage();
                    logger.warn(msg, e);
                    failures.add(msg);
                }
            }
            if (!failures.isEmpty()) {
                logger.error(failures.size()+" failures while parsing "+file+":\n"+StringUtils.join(failures,"\n")+"\n");
            }
            logger.info(loaded+" lines from "+file+" (out of "+line+" lines) inserted into temp DB");

            logger.info("Loaded initial CSV files");
        } catch (Exception e) {
            String msg = "Failed to load CSVs file(s): "+e;
            logger.error(msg);
            throw new RuntimeException(msg, e);
        }
    }

    /**
     * Attempt to verify integer
     * @param string string to verify as integer
     * @param min
     * @param max
     * @param cannotBeBlank true if this must be a number and cannot be null
     * @param name the name of the string (the field), for error messages
     * @return the integer OR null (if allowed)
     * @throws java.lang.IllegalArgumentException is the string fails validation
     */
    private Integer parseInt(String string, Integer min, Integer max, boolean cannotBeBlank, String name) {
        boolean blank = StringUtils.isBlank(string);
        if (cannotBeBlank && blank) {
            throw new IllegalArgumentException(name+" (" + string + ") cannot be blank");
        } else if (blank) {
            // can be blank, so return null
            return null;
        }
        Integer num;
        try {
            num = Integer.parseInt(string);
            if (min != null && num < min) {
                throw new IllegalArgumentException(name+" integer ("+num+") is less than the minimum ("+min+")");
            } else if (max != null && num > max) {
                throw new IllegalArgumentException(name+" integer ("+num+") is greater than the maximum ("+max+")");
            }
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(name+" ("+string+") must be an integer: "+e);
        }
        return num;
    }

    private Float parseFloat(String string, Float min, Float max, boolean cannotBeBlank, String name) {
        boolean blank = StringUtils.isBlank(string);
        if (cannotBeBlank && blank) {
            throw new IllegalArgumentException(name+" (" + string + ") cannot be blank");
        } else if (blank) {
            // can be blank, so return null
            return null;
        }
        Float num;
        try {
            num = Float.parseFloat(string);
            if (min != null && num < min) {
                throw new IllegalArgumentException(name+" number ("+num+") is less than the minimum ("+min+")");
            } else if (max != null && num > max) {
                throw new IllegalArgumentException(name+" number ("+num+") is greater than the maximum ("+max+")");
            }
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(name+" ("+string+") must be a float: "+e);
        }
        return num;
    }

    private Boolean parseBoolean(String string, boolean cannotBeBlank, String name) {
        boolean blank = StringUtils.isBlank(string);
        if (cannotBeBlank && blank) {
            throw new IllegalArgumentException(name+" (" + string + ") cannot be blank");
        } else if (blank) {
            // can be blank, so return null
            return null;
        }
        Boolean bool;
        if ("T".equalsIgnoreCase(string)
                || "Y".equalsIgnoreCase(string)
                || "YES".equalsIgnoreCase(string)
                ) {
            bool = Boolean.TRUE;
        } else {
            bool = Boolean.parseBoolean(string);
        }
        return bool;
    }

    private String parseString(String string, String[] valid, boolean cannotBeBlank, String name) {
        boolean blank = StringUtils.isBlank(string);
        if (cannotBeBlank && blank) {
            throw new IllegalArgumentException(name + " ("+string+") cannot be blank");
        } else if (!blank) {
            if (valid != null && valid.length > 0) {
                if (!ArrayUtils.contains(valid, string)) {
                    // invalid if not in the valid set
                    throw new IllegalArgumentException(name + " ("+string+") must be in the valid set: "+ArrayUtils.toString(valid));
                }
            }
        }
        return string;
    }

    /**
     * Loads up a CSV file if possible
     * @param fileName the name of the file in the input dir
     * @param minColumns minimum number of columns in this CSV
     * @param headerStartsWith the string of the first header column
     * @return the CSV reader for this file
     * @throws IllegalStateException if the file cannot be loaded
     */
    private CSVReader loadCSV(String fileName, int minColumns, String headerStartsWith) {
        assert StringUtils.isNotBlank(fileName) : "fileName must not be blank: "+fileName;
        assert minColumns > 0 : "minColumns must be > 0: "+minColumns;
        assert StringUtils.isNotBlank(headerStartsWith) : "headerStartsWith must not be blank: "+fileName;
        CSVReader fileCSV;
        try {
            InputStream fileCSV_IS = FileUtils.openInputStream(new File(configuration.inputDirectory, fileName));
            fileCSV = new CSVReader(new InputStreamReader(fileCSV_IS));
            String[] check = fileCSV.readNext();
            if (check != null
                    && check.length >= minColumns
                    && StringUtils.startsWithIgnoreCase(headerStartsWith,StringUtils.trimToEmpty(check[0]))
                    ) {
                logger.info(fileName+" file and header appear valid");
            } else {
                throw new IllegalStateException(fileName+" file and header do not appear valid (no "+headerStartsWith+" header or less than "+minColumns+" required columns");
            }
        } catch (Exception e) {
            throw new IllegalStateException(fileName+" CSV is invalid: "+e);
        }
        return fileCSV;
    }

    /**
     * Copies a file from a classpath dir to the file inputs dir
     * @param classpathDir the dir on the classpath with the same file (include trailing slash)
     * @param filename the csv file to copy from extracts to the inputs location
     * @throws java.lang.RuntimeException if the file cannot copy
     */
    private void copySampleCSV(String classpathDir, String filename) {
        try {
            IOUtils.copy(
                    InputHandlerService.class.getClassLoader().getResourceAsStream(classpathDir + filename),
                    new FileOutputStream(new File(configuration.inputDirectory, filename))
            );
        } catch (IOException e) {
            throw new RuntimeException("Cannot find the sample file to copy: "+filename);
        }
    }

    public void process() {
        logger.info("PROCESS");
        // TODO execute the processor
        // TOOD init CSV files
        try {
            // TODO - allow csv file path override / config
            InputStream studentsCSV_IS = InputHandlerService.class.getClassLoader().getResourceAsStream("extracts/students.csv");
            InputStream coursesCSV_IS = InputHandlerService.class.getClassLoader().getResourceAsStream("extracts/courses.csv");
            InputStream gradesCSV_IS = InputHandlerService.class.getClassLoader().getResourceAsStream("extracts/grade.csv");
            InputStream usageCSV_IS = InputHandlerService.class.getClassLoader().getResourceAsStream("extracts/activity.csv");
            // now check the files by trying to read the header line from each one
            CSVReader studentsCSV = new CSVReader(new InputStreamReader(studentsCSV_IS));
            String[] check = studentsCSV.readNext();
            if (check != null && check.length >= 14 && "ALTERNATIVE_ID".equals(StringUtils.trimToEmpty(check[0]).toUpperCase())) {
                logger.info("Student CSV file and header appear valid");
            } else {
                throw new IllegalStateException("Students CSV file does not appear valid (no header or less than 14 required columns");
            }
            CSVReader coursesCSV = new CSVReader(new InputStreamReader(coursesCSV_IS));
            check = coursesCSV.readNext();
            if (check != null && check.length >= 4 && "COURSE_ID".equals(StringUtils.trimToEmpty(check[0]).toUpperCase())) {
                logger.info("Courses CSV file and header appear valid");
            } else {
                throw new IllegalStateException("Courses CSV file does not appear valid (no header or less than 4 required columns");
            }
            CSVReader gradesCSV = new CSVReader(new InputStreamReader(gradesCSV_IS));
            check = gradesCSV.readNext();
            if (check != null && check.length >= 8 && "ALTERNATIVE_ID".equals(StringUtils.trimToEmpty(check[0]).toUpperCase())) {
                logger.info("Grades CSV file and header appear valid");
            } else {
                throw new IllegalStateException("Grades CSV file does not appear valid (no header or less than 4 required columns");
            }
            CSVReader usageCSV = new CSVReader(new InputStreamReader(usageCSV_IS));
            check = usageCSV.readNext();
            if (check != null && check.length >= 4 && "ALTERNATIVE_ID".equals(StringUtils.trimToEmpty(check[0]).toUpperCase())) {
                logger.info("Usage CSV file and header appear valid");
            } else {
                throw new IllegalStateException("Usage CSV file does not appear valid (no header or less than 4 required columns");
            }
            logger.info("Loaded initial CSV files: ");
        } catch (Exception e) {
            String msg = "Failed to load CSVs file(s) and init the kettle pre-processor: "+e;
            logger.error(msg);
            throw new RuntimeException(msg, e);
        }
    }

}

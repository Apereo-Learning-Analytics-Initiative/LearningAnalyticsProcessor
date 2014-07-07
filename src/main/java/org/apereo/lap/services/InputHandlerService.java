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
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.annotation.Resource;
import java.io.*;

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

    @PostConstruct
    public void init() {
        logger.info("INIT");
        if (configuration.config.getBoolean("input.copy.samples")) {
            copySampleExtractCSVs();
        }
        if (configuration.config.getBoolean("input.init.load.csv")) {
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
            /*
            CSVReader coursesCSV = new CSVReader(new InputStreamReader(coursesCSV_IS));
            check = coursesCSV.readNext();
            if (check != null && check.length >= 4 && "COURSE_ID".equals(StringUtils.trimToEmpty(check[0]).toUpperCase())) {
                logger.info("Courses CSV file and header appear valid");
            } else {
                throw new IllegalStateException("Courses CSV file does not appear valid (no header or less than 4 required columns");
            }*/
            logger.info("Loaded initial CSV files");
        } catch (Exception e) {
            String msg = "Failed to load CSVs file(s): "+e;
            logger.error(msg);
            throw new RuntimeException(msg, e);
        }
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

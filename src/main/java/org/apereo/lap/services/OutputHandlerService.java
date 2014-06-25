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
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * Handles the the data outputs from the pipeline (including the generation of output formats)
 * 
 * @author Aaron Zeckoski (azeckoski @ unicon.net) (azeckoski @ vt.edu)
 */
@Component
public class OutputHandlerService {

    private static final Logger logger = LoggerFactory.getLogger(OutputHandlerService.class);

    @PostConstruct
    public void init() {
        logger.info("INIT started");
        // TODO load up config
        // TOOD init CSV files
        try {
            // TODO - allow csv file path override / config
            InputStream studentsCSV_IS = OutputHandlerService.class.getClassLoader().getResourceAsStream("extracts/students.csv");
            InputStream coursesCSV_IS = OutputHandlerService.class.getClassLoader().getResourceAsStream("extracts/courses.csv");
            InputStream gradesCSV_IS = OutputHandlerService.class.getClassLoader().getResourceAsStream("extracts/grades.csv");
            InputStream usageCSV_IS = OutputHandlerService.class.getClassLoader().getResourceAsStream("extracts/usage.csv");
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


        // TODO init kettle
        logger.info("INIT complete");
    }

    @PreDestroy
    public void destroy() {
        logger.info("DESTROY");
    }

    public void process() {
        logger.info("PROCESS");
        // TODO execute the processor
    }

}

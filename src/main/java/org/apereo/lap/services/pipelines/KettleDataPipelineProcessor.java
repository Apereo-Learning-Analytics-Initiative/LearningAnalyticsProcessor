/*******************************************************************************
 * Copyright (c) 2015 Unicon (R) Licensed under the
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
 *******************************************************************************/
package org.apereo.lap.services.pipelines;

import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;

import org.apereo.lap.model.PipelineConfig;
import org.apereo.lap.model.Processor;
import org.apereo.lap.services.configuration.ConfigurationService;
import org.apereo.lap.services.storage.StorageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * This processor just produces data in a "KETTLE_DATA" table
 * See table columns defined below.
 *
 * @author Robert Long (rlong @ unicon.net)
 */
@Component
public class KettleDataPipelineProcessor implements PipelineProcessor {

    static final Logger logger = LoggerFactory.getLogger(KettleDataPipelineProcessor.class);

    @Autowired ConfigurationService config;
    @Autowired StorageService storage;

    @PostConstruct
    public void init() {
        // create the temp table
        storage.getTempJdbcTemplate().execute(
                "CREATE TABLE IF NOT EXISTS KETTLE_DATA (" +
                "  ID INT(11) NOT NULL AUTO_INCREMENT," +
                "  ALTERNATIVE_ID VARCHAR(255) NOT NULL," +
                "  COURSE_ID VARCHAR(255) NOT NULL," +
                "  MODEL_RISK_CONFIDENCE VARCHAR(255), " +
                "  SUBJECT VARCHAR(255), " +
                "  ONLINE_FLAG BOOLEAN, " +
                
"  ENROLLMENT INT, " +
"  RC_FINAL_GRADE DECIMAL(2,1), " +
"  PERCENTILE INT, " +
"  SAT_VERBAL INT, " +
"  SAT_MATH INT, " +
"  APTITUDE_SCORE INT, " +
"  AGE INT, " +
"  RC_GENDER INT, " +
"  RC_ENROLLMENT_STATUS VARCHAR(255), " +
"  RC_CLASS_CODE VARCHAR(255), " +
"  GPA_CUMULATIVE DECIMAL(5,4), " +

"  GPA_SEMESTER DECIMAL(5,4), " +
"  STANDING VARCHAR(255), " +
"  RMN_SCORE DECIMAL(20,4), " +
"  RMN_SCORE_PARTIAL DECIMAL(20,4), " +
"  R_CONTENT_READ DECIMAL(10,1), " +
"  R_ASSMT_SUB DECIMAL(10,1), " +
"  R_FORUM_POST DECIMAL(10,1), " +

"  R_FORUM_READ DECIMAL(10,1), " +
"  R_LESSONS_VIEW DECIMAL(10,1), " +
"  R_ASSMT_TAKE DECIMAL(10,1), " +
"  R_ASN_SUB DECIMAL(10,1), " +
"  R_ASN_READ DECIMAL(10,1), " +
"  R_SESSIONS DECIMAL(20,4), " +
"  ACADEMIC_RISK INT, " +
"  FAIL_PROBABILITY DECIMAL(4,3), " +
"  PASS_PROBABILITY DECIMAL(4,3), " +

                
                "  PRIMARY KEY (ID)," +
                "  UNIQUE KEY KETTLE_DATA_UNIQUE (ALTERNATIVE_ID, COURSE_ID)" +
                ")"
        );
        logger.info("INIT: created temp table KETTLE_DATA");
    }

    @Override
    public Processor.ProcessorType getProcessorType() {
        return Processor.ProcessorType.KETTLE_DATA;
    }

    @Override
    public ProcessorResult process(PipelineConfig pipelineConfig, Processor processorConfig, String inputJson) {
        ProcessorResult result = new ProcessorResult(Processor.ProcessorType.KETTLE_DATA);

        // clear the temp table
        storage.getTempJdbcTemplate().execute("TRUNCATE TABLE KETTLE_DATA");

        // get the data from the PCSM_SCORING table
        String sql = "SELECT " +
                        "ALTERNATIVE_ID," +
                        "COURSE_ID," +
                        "MODEL_RISK_CONFIDENCE, " +
                        "SUBJECT, " +
                        "ONLINE_FLAG, " +
                        
"ENROLLMENT, " +
"RC_FINAL_GRADE, " +
"PERCENTILE, " +
"SAT_VERBAL, " +
"SAT_MATH, " +
"APTITUDE_SCORE, " +
"AGE, " +
"RC_GENDER, " +
"RC_ENROLLMENT_STATUS, " +
"RC_CLASS_CODE, " +
"GPA_CUMULATIVE, " +

"GPA_SEMESTER, " +
"STANDING, " +
"RMN_SCORE, " +
"RMN_SCORE_PARTIAL, " +
"R_CONTENT_READ, " +
"R_ASSMT_SUB, " +
"R_FORUM_POST, " +

"R_FORUM_READ, " +
"R_LESSONS_VIEW, " +
"R_ASSMT_TAKE, " +
"R_ASN_SUB, " +
"R_ASN_READ, " +
"R_SESSIONS, " +
"ACADEMIC_RISK, " +
"FAIL_PROBABILITY, " +
"PASS_PROBABILITY " +

                     "FROM " +
                        "PCSM_SCORING";
        List<Map<String, Object>> pcsmScoring = storage.getTempJdbcTemplate().queryForList(sql);
        // insert data from PCSM_SCORING into KETTLE_DATA for CSV output
        int rowCount = 0;
        for (Map<String, Object> pcsmScore : pcsmScoring) {
            Object[] values = new Object[]{
                pcsmScore.get("ALTERNATIVE_ID"),
                pcsmScore.get("COURSE_ID"),
                pcsmScore.get("MODEL_RISK_CONFIDENCE"),
                pcsmScore.get("SUBJECT"),
                pcsmScore.get("ONLINE_FLAG"),
                
                pcsmScore.get("ENROLLMENT"),
                pcsmScore.get("RC_FINAL_GRADE"),
                pcsmScore.get("PERCENTILE"),
                pcsmScore.get("SAT_VERBAL"),
                pcsmScore.get("SAT_MATH"),
                pcsmScore.get("APTITUDE_SCORE"),
                pcsmScore.get("AGE"),
                pcsmScore.get("RC_GENDER"),
                pcsmScore.get("RC_ENROLLMENT_STATUS"),
                pcsmScore.get("RC_CLASS_CODE"),
                pcsmScore.get("GPA_CUMULATIVE"),
                
                pcsmScore.get("GPA_SEMESTER"),
                pcsmScore.get("STANDING"),
                pcsmScore.get("RMN_SCORE"),
                pcsmScore.get("RMN_SCORE_PARTIAL"),
                pcsmScore.get("R_CONTENT_READ"),
                pcsmScore.get("R_ASSMT_SUB"),
                pcsmScore.get("R_FORUM_POST"),
                
                pcsmScore.get("R_FORUM_READ"),
                pcsmScore.get("R_LESSONS_VIEW"),
                pcsmScore.get("R_ASSMT_TAKE"),
                pcsmScore.get("R_ASN_SUB"),
                pcsmScore.get("R_ASN_READ"),
                pcsmScore.get("R_SESSIONS"),
                pcsmScore.get("ACADEMIC_RISK"),
                pcsmScore.get("FAIL_PROBABILITY"),
                pcsmScore.get("PASS_PROBABILITY"),
                
            };
            storage.getTempJdbcTemplate().update("INSERT INTO KETTLE_DATA (ALTERNATIVE_ID, COURSE_ID, "
                + "MODEL_RISK_CONFIDENCE, SUBJECT, ONLINE_FLAG, ENROLLMENT, RC_FINAL_GRADE, PERCENTILE, "
                + "SAT_VERBAL, SAT_MATH, APTITUDE_SCORE, AGE, RC_GENDER, RC_ENROLLMENT_STATUS, RC_CLASS_CODE, GPA_CUMULATIVE,"
                + "GPA_SEMESTER, STANDING, RMN_SCORE, RMN_SCORE_PARTIAL, R_CONTENT_READ, R_ASSMT_SUB, R_FORUM_POST,"
                + "R_FORUM_READ, R_LESSONS_VIEW, R_ASSMT_TAKE, R_ASN_SUB, R_ASN_READ, R_SESSIONS, ACADEMIC_RISK, FAIL_PROBABILITY, PASS_PROBABILITY) VALUES (?,?,?,?,"
                + "?,?,?,?,"
                + "?,?,?,?,"
                + "?,?,?,?,"
                + "?,?,?,?,?,"
                + "?,?,"
                + "?,?,?,?,?,?,?,?,?)", values);
            rowCount++;
        }

        result.done(rowCount, null);

        return result;
    }

}

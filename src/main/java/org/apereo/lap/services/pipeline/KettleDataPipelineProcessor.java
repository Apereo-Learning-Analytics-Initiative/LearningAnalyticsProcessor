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
package org.apereo.lap.services.pipeline;

import org.apereo.lap.model.PipelineConfig;
import org.apereo.lap.model.Processor;
import org.apereo.lap.services.ConfigurationService;
import org.apereo.lap.services.StorageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;

import java.util.List;
import java.util.Map;

/**
 * This processor just produces Fake data in a "KETTLE_DATA" table
 * The table has these fields: ID (auto), ALTERNATIVE_ID, ACADEMIC_RISK
 *
 * @author Robert Long (rlong @ unicon.net)
 */
@Component
public class KettleDataPipelineProcessor implements PipelineProcessor {

    static final Logger logger = LoggerFactory.getLogger(KettleDataPipelineProcessor.class);

    @Resource
    ConfigurationService config;

    @Resource
    StorageService storage;

    @PostConstruct
    public void init() {
        // create the temp table
        storage.getTempJdbcTemplate().execute(
                "CREATE TABLE IF NOT EXISTS KETTLE_DATA (" +
                "  ID INT(11) NOT NULL AUTO_INCREMENT," +
                "  ALTERNATIVE_ID VARCHAR(255) NOT NULL," +
                "  ACADEMIC_RISK VARCHAR(11) NOT NULL DEFAULT '0'," +
                "  PRIMARY KEY (ID)," +
                "  UNIQUE KEY KETTLE_DATA_UNIQUE (ALTERNATIVE_ID)" +
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
        List<Map<String, Object>> pcsmScoring = storage.getTempJdbcTemplate().queryForList("SELECT * FROM PCSM_SCORING");

        // insert data from PCSM_SCORING into KETTLE_DATA for CSV output
        int rowCount = 0;
        for (Map<String, Object> pcsmScore : pcsmScoring) {
            Object[] values = new Object[]{pcsmScore.get("ALTERNATIVE_ID"), pcsmScore.get("ACADEMIC_RISK")};
            storage.getTempJdbcTemplate().update("INSERT INTO KETTLE_DATA (ALTERNATIVE_ID,ACADEMIC_RISK) VALUES (?,?)", values);
            rowCount++;
        }

        result.done(rowCount, null);

        return result;
    }

}

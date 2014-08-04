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
import java.util.Random;

/**
 * This processor just produces Fake data in a "KETTLE_DATA" table
 * The table has these fields: ID (auto), USERNAME, SCORE, INFO
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

    Random rand;

    @PostConstruct
    public void init() {
        rand = new Random();
        // create the temp table
        storage.getTempJdbcTemplate().execute(
                "CREATE TABLE IF NOT EXISTS KETTLE_DATA (" +
                "  ID INT(11) NOT NULL AUTO_INCREMENT," +
                "  USERNAME VARCHAR(255) NOT NULL," +
                "  SCORE INT(11) NOT NULL DEFAULT '0'," +
                "  INFO VARCHAR(255) DEFAULT NULL," +
                "  PRIMARY KEY (ID)," +
                "  UNIQUE KEY USERNAME_UNIQUE (USERNAME)" +
                ")"
        );
        logger.info("INIT: created temp table KETTLE_DATA");
    }

    int randInt(int min, int max) {
        // nextInt is normally exclusive of the top value,
        // so add 1 to make it inclusive
        return (rand.nextInt((max - min) + 1) + min);
    }

    @Override
    public Processor.ProcessorType getProcessorType() {
        return Processor.ProcessorType.KETTLE_DATA;
    }

    @Override
    public ProcessorResult process(PipelineConfig pipelineConfig, Processor processorConfig, String inputJson) {
        ProcessorResult result = new ProcessorResult(Processor.ProcessorType.KETTLE_DATA);
        int recordsToFake = processorConfig.count;
        if (recordsToFake < 0) {
            recordsToFake = 100;
        }

        // clear the temp table
        storage.getTempJdbcTemplate().execute("TRUNCATE TABLE KETTLE_DATA");

        // insert fake data into the table
        for (int i = 0; i < recordsToFake; i++) {
            Object[] values = new Object[]{"Bob"+i, randInt(1,100), "Bob info goes here"};
            storage.getTempJdbcTemplate().update("INSERT INTO KETTLE_DATA (USERNAME,SCORE,INFO) VALUES (?,?,?)", values);
        }

        result.done(recordsToFake, null);
        return result;
    }

}

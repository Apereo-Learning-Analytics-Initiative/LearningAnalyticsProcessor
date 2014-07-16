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

import org.apache.commons.configuration.Configuration;
import org.apereo.lap.model.PipelineConfig;
import org.apereo.lap.model.Processor;
import org.apereo.lap.services.ConfigurationService;
import org.apereo.lap.services.StorageService;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Component;
import org.pentaho.di.core.KettleEnvironment;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.util.EnvUtil;
import org.pentaho.di.job.JobMeta;
import org.pentaho.di.job.Job;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Handles the pipeline processing for Kettle processors
 *
 * @author Aaron Zeckoski (azeckoski @ unicon.net) (azeckoski @ vt.edu)
 * @author Robert Long (rlong @ unicon.net)
 */
@Component
public class KettlePipelineProcessor implements PipelineProcessor {

    @Resource
    ConfigurationService configuration;

    @Resource
    StorageService storage;

    @Resource
    ResourceLoader resourceLoader;

    @PostConstruct
    public void init() {
        // Do any init here you need to (but note
    }

    @Override
    public Processor.ProcessorType getProcessorType() {
        return Processor.ProcessorType.KETTLE;
    }

    @Override
    public ProcessorResult process(PipelineConfig pipelineConfig, Processor processorConfig) {
        String name = processorConfig.name;
        ProcessorResult result = new ProcessorResult(Processor.ProcessorType.KETTLE);
        File kettleXMLFile;

        try {
            // TODO maybe make this read the kettle files from the pipelines dir as well?
            kettleXMLFile = resourceLoader.getResource("classpath:"+processorConfig.filename).getFile();
        } catch (IOException e) {
            throw new RuntimeException("FAIL! (to read kettle file): "+processorConfig.filename+" :"+e, e);
        }

        // TODO do processing here! (Bob)
        try {
            KettleEnvironment.init(false);
            EnvUtil.environmentInit();
            // TODO check for transform or job
            JobMeta jobMeta = new JobMeta(kettleXMLFile.getAbsolutePath(), null, null);

            Job job = new Job(null, jobMeta);
            job.start();
            job.waitUntilFinished();
        } catch(Exception e) {
            e.printStackTrace();
        }

        result.done(0, null); // TODO populate count and failures
        return result;
    }

    private void addNewDatabaseConnection(JobMeta jobMeta, String connectionName) {
        Configuration config = configuration.getConfig();

        DatabaseMeta dm = new DatabaseMeta();
        dm.setName(connectionName);
        dm.setHostname(config.getString(connectionName + ".db.hostname"));
        dm.setDatabaseType(config.getString(connectionName + ".db.databasetype"));
        dm.setAccessType(config.getInt(connectionName + ".db.accesstype"));
        dm.setDBName(config.getString(connectionName + ".db.dbname"));
        dm.setDBPort(config.getString(connectionName + ".db.dbport"));
        dm.setUsername(config.getString(connectionName + ".db.username"));
        dm.setPassword(config.getString(connectionName + ".db.password"));
        
        Properties attributes = new Properties();
        attributes.setProperty("FORCE_IDENTIFIERS_TO_LOWERCASE", "N");
        attributes.setProperty("FORCE_IDENTIFIERS_TO_UPPERCASE", "N");
        attributes.setProperty("IS_CLUSTERED", "N");
        attributes.setProperty("PRESERVE_RESERVED_WORD_CASE", "N");
        attributes.setProperty("QUOTE_ALL_FIELDS", "N");
        attributes.setProperty("STREAM_RESULTS", "Y");
        attributes.setProperty("SUPPORTS_BOOLEAN_DATA_TYPE", "N");
        attributes.setProperty("SUPPORTS_TIMESTAMP_DATA_TYPE", "N");
        attributes.setProperty("USE_POOLING", "N");
        dm.setAttributes(attributes);

        //jobMeta.addDatabase(dm);
        jobMeta.addOrReplaceDatabase(dm);
    }
}

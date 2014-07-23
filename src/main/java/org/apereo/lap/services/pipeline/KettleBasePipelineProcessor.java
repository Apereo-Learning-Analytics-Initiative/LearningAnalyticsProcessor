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

import java.io.File;
import java.io.IOException;
import java.util.Properties;

import javax.annotation.Resource;

import org.apache.commons.configuration.Configuration;
import org.apereo.lap.services.ConfigurationService;
import org.apereo.lap.services.InputHandlerService;
import org.apereo.lap.services.StorageService;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.trans.HasDatabasesInterface;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ResourceLoader;

/**
 * Handles the pipeline processing for Kettle processors
 *
 * @author Robert Long (rlong @ unicon.net)
 */
public abstract class KettleBasePipelineProcessor implements PipelineProcessor{

    static final Logger logger = LoggerFactory.getLogger(KettleBasePipelineProcessor.class);

    @Resource
    ConfigurationService configuration;

    @Resource
    InputHandlerService inputHandler;

    @Resource
    StorageService storage;

    @Resource
    ResourceLoader resourceLoader;

    protected File getFile(String filename) {
        try {
            // TODO maybe make this read the kettle files from the pipelines dir as well?
            File file = resourceLoader.getResource("classpath:"+filename).getFile();

            return file;
        } catch (IOException e) {
            throw new RuntimeException("FAIL! (to read kettle file): "+filename+": "+e, e);
        }
    }

    protected void addNewDatabaseConnection(HasDatabasesInterface meta, String connectionName) {
        // TODO make the database config dynamic using a connection from StorageService
    }

    protected void setKettlePluginsDirectory() {
        try {
            String plugins = resourceLoader.getResource("classpath:kettle/plugins").getURI().toString();
            System.setProperty("KETTLE_PLUGIN_BASE_FOLDERS", plugins);
            logger.info("Setting kettle plugins base directory to: "+plugins);
        } catch (IOException e) {
            logger.error("Error setting Kettle plugins directory. Error: " + e, e);
        }
    }

}

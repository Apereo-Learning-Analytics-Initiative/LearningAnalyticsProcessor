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
import java.util.Iterator;

import javax.annotation.Resource;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.lang.StringUtils;
import org.apereo.lap.services.ConfigurationService;
import org.apereo.lap.services.InputHandlerService;
import org.apereo.lap.services.StorageService;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.database.H2DatabaseMeta;
import org.pentaho.di.core.database.MySQLDatabaseMeta;
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
    ConfigurationService configurationService;

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

    protected void updateDatabaseConnection(DatabaseMeta databaseMeta) {
        Configuration configuration = configurationService.getConfig();

        if (StringUtils.equalsIgnoreCase(configuration.getString("db.job.type", ""), "MySQL")) {
            // set MySQL database properties
            MySQLDatabaseMeta mySQLDatabaseMeta = new MySQLDatabaseMeta();
            mySQLDatabaseMeta.setName(databaseMeta.getName());
            mySQLDatabaseMeta.setUsername(configuration.getString("db.job.username", ""));
            mySQLDatabaseMeta.setPassword(configuration.getString("db.job.password", ""));
            mySQLDatabaseMeta.setHostname(configuration.getString("db.job.host", ""));
            mySQLDatabaseMeta.setDatabasePortNumberString(configuration.getString("db.job.port", ""));
            mySQLDatabaseMeta.setDatabaseName(configuration.getString("db.job.dbname", ""));
            mySQLDatabaseMeta.setAccessType(DatabaseMeta.TYPE_ACCESS_NATIVE);
            databaseMeta.setDatabaseInterface(mySQLDatabaseMeta);
        } else if (StringUtils.equalsIgnoreCase(configuration.getString("db.job.type", ""), "H2")) {
            // set H2 database properties
            H2DatabaseMeta h2DatabaseMeta = new H2DatabaseMeta();
            h2DatabaseMeta.setName(databaseMeta.getName());
            h2DatabaseMeta.setUsername(configuration.getString("db.job.username", ""));
            h2DatabaseMeta.setPassword(configuration.getString("db.job.password", ""));
            h2DatabaseMeta.setHostname(configuration.getString("db.job.host", ""));
            h2DatabaseMeta.setDatabasePortNumberString(configuration.getString("db.job.port", "3306"));
            h2DatabaseMeta.setDatabaseName(configuration.getString("db.job.dbname", ""));
            h2DatabaseMeta.setAccessType(DatabaseMeta.TYPE_ACCESS_NATIVE);
            databaseMeta.setDatabaseInterface(h2DatabaseMeta);
        } else {
            throw new IllegalArgumentException("Invalid database type: " + configuration.getString("db.job.type"));
        }
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

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
import org.pentaho.di.job.JobMeta;
import org.pentaho.di.shared.SharedObjectInterface;
import org.pentaho.di.shared.SharedObjects;
import org.pentaho.di.trans.HasDatabasesInterface;
import org.pentaho.di.trans.TransMeta;
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

    /**
     * System defined path separator Windows = "\", Unix = "/"
     */
    protected static final String SLASH = System.getProperty("file.separator");

    /**
     * Shared objects (database connections, etc.) located in .kettle/shared.xml
     */
    private SharedObjects sharedObjects;

    /**
     * Gets a file from the classpath with the given name or path
     * 
     * @param filename the file's name or path
     * @return the object for the file
     */
    protected File getFile(String filename) {
        try {
            File file = resourceLoader.getResource("classpath:"+filename).getFile();

            return file;
        } catch (IOException e) {
            throw new RuntimeException("FAIL! (to read kettle file): "+filename+": "+e, e);
        }
    }

    /**
     * Configures the job or transform to use a pre-defined database connection
     * 
     * @param meta the meta object (JobMeta or TransMeta)
     */
    protected void configureDatabaseConnection(HasDatabasesInterface meta) {
        assert meta != null;

        if (sharedObjects == null) {
            logger.info("SharedObjects is null, configuring pre-defined shared database connections.");
            updateSharedDatabase();
        }

        if (meta instanceof TransMeta) {
            logger.info("Setting shared objects for TransMeta.");
            ((TransMeta) meta).setSharedObjects(sharedObjects);
        } else if (meta instanceof JobMeta) {
            logger.info("Setting shared objects for JobMeta.");
            ((JobMeta) meta).setSharedObjects(sharedObjects);
        } else {
            throw new IllegalArgumentException("Error setting shared objects for non-configurable meta class: " + meta.getClass().getName());
        }
    }

    /**
     * Updates Kettle configuration properties
     */
    protected void configureKettle() {
        setKettleHomeDirectory();
        setKettlePluginsDirectory();
    }

    /**
     * Updates the shared database connections defined in shared.xml
     * 
     */
    private void updateSharedDatabase() {
        Configuration configuration = configurationService.getConfig();

        try {
            sharedObjects = new SharedObjects();
            Iterator<SharedObjectInterface> iterator = sharedObjects.getObjectsMap().values().iterator();
            while (iterator.hasNext()) {
                SharedObjectInterface sharedObjectInterface = iterator.next();
                // only update the database connections
                if (sharedObjectInterface instanceof DatabaseMeta) {
                    logger.info("Updating shared database connection '" + sharedObjectInterface.getName() + "' in file: " + sharedObjects.getFilename());
                    String databaseName = StringUtils.remove(configuration.getString("db.url", ""), "jdbc:h2:");

                    // create a fully-configured H2 database connection
                    H2DatabaseMeta h2DatabaseMeta = new H2DatabaseMeta();
                    h2DatabaseMeta.setName(sharedObjectInterface.getName());
                    h2DatabaseMeta.setUsername(configuration.getString("db.username", ""));
                    h2DatabaseMeta.setPassword(configuration.getString("db.password", ""));
                    h2DatabaseMeta.setDatabaseName(databaseName);
                    h2DatabaseMeta.setAccessType(DatabaseMeta.TYPE_ACCESS_NATIVE);
                    h2DatabaseMeta.setAttributes(((DatabaseMeta) sharedObjectInterface).getAttributes());
                    h2DatabaseMeta.setDatabasePortNumberString(null);
                    h2DatabaseMeta.setPluginId("H2");

                    // set the interface to the H2 configuration
                    ((DatabaseMeta) sharedObjectInterface).setDatabaseInterface(h2DatabaseMeta);
                }
            }

            // save the new configuration to shared.xml
            sharedObjects.saveToFile();
        } catch (Exception e) {
            logger.error("An error occurred dynamically configuring the shared database connection. Error: " + e, e);
        }

    }

    /**
     * Updates the Kettle configuration parameter KETTLE_PLUGIN_BASE_FOLDERS with the external plug-ins directory
     */
    private void setKettlePluginsDirectory() {
        try {
            String plugins = resourceLoader.getResource("classpath:kettle/plugins").getURI().toString();
            System.setProperty("KETTLE_PLUGIN_BASE_FOLDERS", plugins);
            logger.info("Setting kettle plugins base directory to: "+plugins);
        } catch (IOException e) {
            logger.error("Error setting Kettle plugins directory. Error: " + e, e);
        }
    }

    /**
     * Updates the Kettle configuration parameter KETTLE_HOME with the external home directory
     */
    private void setKettleHomeDirectory() {
        try {
            String home = resourceLoader.getResource("classpath:kettle/home").getURI().toString();
            System.setProperty("KETTLE_HOME", home);
            logger.info("Setting kettle home directory to: "+home);
        } catch (IOException e) {
            logger.error("Error setting Kettle home directory. Error: " + e, e);
        }
    }

}

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

import javax.annotation.Resource;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.lang.StringUtils;
import org.apereo.lap.services.ConfigurationService;
import org.apereo.lap.services.InputHandlerService;
import org.apereo.lap.services.StorageService;
import org.pentaho.di.core.KettleEnvironment;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.database.H2DatabaseMeta;
import org.pentaho.di.core.util.EnvUtil;
import org.pentaho.di.shared.SharedObjects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ResourceLoader;

/**
 * Handles the pipeline processing for Kettle processors
 *
 * @author Robert Long (rlong @ unicon.net)
 */
public abstract class KettleBasePipelineProcessor implements PipelineProcessor{

    protected final Logger logger = LoggerFactory.getLogger(getClass());

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
     * Names of the database connections used in the .kjb and .ktr files
     */
    protected String[] databaseConnectionNames;

    /**
     * Has Kettle been configured?
     */
    private boolean isKettleConfigured = false;

    /**
     * Performs Kettle configuration in the following order:
     * 
     * 1. Kettle plug-ins directory
     * 2. KettleEnvironment initialization
     * 3. Environment Util initialization
     * 4. Shared database connections
     */
    protected void configureKettle() {
        if (!isKettleConfigured) {
            try {
                // must set first
                setKettlePluginsDirectory();

                // must set second
                KettleEnvironment.init(false);
                EnvUtil.environmentInit();

                // must set third
                configuredSharedObjects();

                isKettleConfigured = true;

                logger.info("Kettle has been successfully configured.");
            } catch (Exception e) {
                logger.error("Error configuring Kettle environment. Error: " + e, e);
            }
        }
    }

    /**
     * Creates shared connections for use in tranformations and jobs
     * Uses connection properties from db.properties with a prefix of "db."
     * Stores the dynamic configuration in a shared.xml file
     */
    private void createSharedDatabaseConnections() {
        Configuration configuration = configurationService.getConfig();

        try {
            if (databaseConnectionNames == null) {
                setDatabaseConnectionNames();
            }

            // gets existing shared.xml objects, or creates a new object to store properties
            sharedObjects = new SharedObjects();

            // process and store each defined database connection
            for (String databaseConnectionName : databaseConnectionNames) {
                // must remove existing connection, as multiple connection names are allowed
                DatabaseMeta existingDatabaseConnection = sharedObjects.getSharedDatabase(databaseConnectionName);
                if (existingDatabaseConnection != null) {
                    sharedObjects.removeObject(existingDatabaseConnection);
                }

                // remove the prefix from the url property
                String databaseName = StringUtils.remove(configuration.getString("db.url", ""), "jdbc:h2:");

                // create a fully-configured H2 database connection
                H2DatabaseMeta h2DatabaseMeta = new H2DatabaseMeta();
                h2DatabaseMeta.setName(databaseConnectionName);
                h2DatabaseMeta.setUsername(configuration.getString("db.username", ""));
                h2DatabaseMeta.setPassword(configuration.getString("db.password", ""));
                h2DatabaseMeta.setDatabaseName(databaseName);
                h2DatabaseMeta.setAccessType(DatabaseMeta.TYPE_ACCESS_NATIVE);
                //h2DatabaseMeta.setDatabasePortNumberString(null);
                h2DatabaseMeta.setPluginId("H2");

                // create new default database container
                DatabaseMeta databaseMeta = new DatabaseMeta();

                // set the interface to the H2 configuration
                databaseMeta.setDatabaseInterface(h2DatabaseMeta);

                // store the database connection in the shared objects
                sharedObjects.storeObject(databaseMeta);

                logger.info("Created shared database connection '" + databaseConnectionName + "'");
            }

            // save the new configuration to shared.xml
            sharedObjects.saveToFile();
            logger.info("Saved new shared database connections to file.");
        } catch (Exception e) {
            logger.error("An error occurred dynamically configuring the shared database connection. Error: " + e, e);
        }

    }

    /**
     * Updates the Kettle configuration parameter KETTLE_PLUGIN_BASE_FOLDERS with the classpath plug-ins directory
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
     * Get the comma-separated value string of database connection names from app.properties
     * with the key "app.database.connection.names"
     */
    private void setDatabaseConnectionNames() {
        Configuration configuration = configurationService.getConfig();
        databaseConnectionNames = configuration.getStringArray("app.database.connection.names");
    }

    /**
     * Gets a file from the classpath with the given name or path
     * 
     * @param filename the file's name or path
     * @return the object for the file
     */
    protected File getFile(String filename) {
        assert StringUtils.isNotEmpty(filename);

        try {
            File file = resourceLoader.getResource("classpath:"+filename).getFile();

            return file;
        } catch (IOException e) {
            throw new RuntimeException("FAIL! (to read kettle file): "+filename+": "+e, e);
        }
    }

    /**
     * Returns the shared objects for transformations and jobs
     * If SharedObjects is null, configures one to return
     * 
     * @return the SharedObjects object
     */
    protected SharedObjects getSharedObjects() {
        if (sharedObjects == null) {
            logger.info("SharedObjects is null, configuring new shared objects.");
            configuredSharedObjects();
        }

        return sharedObjects;
    }

    /**
     * Configured the shared objects
     * 
     * 1. Shared database connections
     */
    private void configuredSharedObjects() {
        // create shared database connections
        createSharedDatabaseConnections();

        logger.info("Saved new shared.xml file to location: " + sharedObjects.getFilename());
    }
}

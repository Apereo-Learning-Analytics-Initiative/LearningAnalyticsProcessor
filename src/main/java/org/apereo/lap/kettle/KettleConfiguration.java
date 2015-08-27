package org.apereo.lap.kettle;

import org.apache.commons.lang.StringUtils;
import org.apereo.lap.services.configuration.ConfigurationService;
import org.apereo.lap.services.storage.DatasourceProperties;
import org.pentaho.di.core.KettleEnvironment;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.database.H2DatabaseMeta;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.util.EnvUtil;
import org.pentaho.di.shared.SharedObjects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.nio.file.Path;

@Component
public class KettleConfiguration {
    private static final Logger logger = LoggerFactory.getLogger(KettleConfiguration.class);

    @Autowired
    private ConfigurationService config;

    @Autowired
    private KettleProperties kettleProperties;

    @Autowired
    private DatasourceProperties datasourceProperties;

    private SharedObjects sharedObjects;

    @PostConstruct
    public void postConstruct() throws KettleException, IOException {
        // First, set plugin dir
        Path pluginDir = config.getApplicationHomeDirectory().resolve("kettle/plugins");
        System.setProperty("KETTLE_PLUGIN_BASE_FOLDERS", pluginDir.toString());

        // Second, configure Kettle env
        KettleEnvironment.init(false);
        EnvUtil.environmentInit();

        // Lastly, configure shared objects
        configureSharedObjects();
    }

    private void configureSharedObjects() throws KettleException, IOException {
        // gets existing shared.xml objects, or creates a new object to store properties
        sharedObjects = new SharedObjects();

        // process and store each defined database connection
        for (String databaseConnectionName : kettleProperties.getDatabaseConnectionNames()) {
            // must remove existing connection, as multiple connection names are allowed
            DatabaseMeta existingDatabaseConnection = sharedObjects.getSharedDatabase(databaseConnectionName);
            if (existingDatabaseConnection != null) {
                sharedObjects.removeObject(existingDatabaseConnection);
            }

            // remove the prefix from the url property
            String databaseName = StringUtils.remove(datasourceProperties.getTemp().getUrl(), "jdbc:h2:");

            // create a fully-configured H2 database connection
            H2DatabaseMeta h2DatabaseMeta = new H2DatabaseMeta();
            h2DatabaseMeta.setName(databaseConnectionName);
            h2DatabaseMeta.setUsername(datasourceProperties.getTemp().getUsername());
            h2DatabaseMeta.setPassword(datasourceProperties.getTemp().getPassword());
            h2DatabaseMeta.setDatabaseName(databaseName);
            h2DatabaseMeta.setAccessType(DatabaseMeta.TYPE_ACCESS_NATIVE);
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
        // NOTE: since we effectively cache the SharedObjects in memory does it make sense to
        // save them to disk? e.g. does it lead to unnecessary brittleness in tests/deploy
        // environments?
        sharedObjects.saveToFile();
        logger.info("Shared objects saved to: " + sharedObjects.getFilename());
    }

    public SharedObjects getSharedObjects() {
        return sharedObjects;
    }
}

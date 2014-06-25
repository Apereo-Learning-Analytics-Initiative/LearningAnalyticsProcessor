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

import org.apache.commons.configuration.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.io.File;
import java.io.IOException;

/**
 * Loads the application configuration from a series of files
 *
 * Also handles the init of the application home directory (controlled by "LAP_HOME" env or -D variable)
 * 
 * @author Aaron Zeckoski (azeckoski @ unicon.net) (azeckoski @ vt.edu)
 */
@Component("configuration")
public class ConfigurationService {

    private static final Logger logger = LoggerFactory.getLogger(ConfigurationService.class);

    Configuration config;
    File applicationHomeDirectory;

    @javax.annotation.Resource
    ResourceLoader resourceLoader;

    @PostConstruct
    public void init() throws IOException {
        logger.info("INIT started");
        logger.info("App Home: "+appHome().getAbsolutePath());
        CompositeConfiguration config = new CompositeConfiguration();
        // load internal config defaults first
        config.setProperty("app.name","LAP");
        File dbDefaults = resourceLoader.getResource("classpath:db.properties").getFile();
        try {
            config.addConfiguration(new PropertiesConfiguration(dbDefaults));
        } catch (ConfigurationException e) {
            logger.error("Unable to load default db.properties file");
        }
        File appDefaults = resourceLoader.getResource("classpath:app.properties").getFile();
        try {
            config.addConfiguration(new PropertiesConfiguration(appDefaults));
        } catch (ConfigurationException e) {
            logger.error("Unable to load default app.properties file");
        }

        // now try to load external config settings
        config.addConfiguration(new SystemConfiguration());
        try {
            config.addConfiguration(new PropertiesConfiguration(new File(appHome(),"lap.properties")));
        } catch (ConfigurationException e) {
            logger.warn("Unable to load lap.properties file");
        }
        this.config = config;
        logger.info("INIT complete: "+config.getString("app.name")+", home="+applicationHomeDirectory.getAbsolutePath());
    }

    @PreDestroy
    public void destroy() {
        logger.info("DESTROY");
    }

    /**
     * @return the directory which is the application home
     */
    File appHome() {
        if (applicationHomeDirectory == null) {
            try {
                // first see if the app home is set
                String lapHome = (String) System.getProperties().get("LAP_HOME");
                if (lapHome == null) {
                    lapHome = System.getenv("LAP_HOME");
                }
                if (lapHome != null) {
                    // check if the directory is valid
                    File lapHomeDir = new File(lapHome);
                    if (lapHomeDir.exists() || lapHomeDir.canRead() || lapHomeDir.isDirectory()) {
                        applicationHomeDirectory = lapHomeDir;
                    } else {
                        logger.warn("Unable to read the configured LAP_HOME dir: "+lapHomeDir.getAbsolutePath()+", it is probably not readable or not a directory, using the default instead (the classpath)");
                    }
                }
                if (applicationHomeDirectory == null) {
                    // failed to find or successfully load the LAP_HOME
                    Resource appCP = resourceLoader.getResource("classpath:"); // default to using the classpath (usually where the webapp is running)
                    logger.debug("AppCP: "+appCP.getFile().getAbsolutePath());
                    File appRoot = appCP.getFile().getParentFile();
                    logger.debug("Parent: "+appRoot.getAbsolutePath());
                    File appHome = new File(appRoot, "lap");
                    if (!appHome.exists()) {
                        try {
                            //noinspection ResultOfMethodCallIgnored
                            appHome.mkdir();
                        } catch (Exception e) {
                            logger.warn("Could not create app home at: "+appHome.getAbsolutePath()+", using root instead: "+appRoot.getAbsolutePath());
                            appHome = appRoot;
                        }
                    }
                    applicationHomeDirectory = appHome;
                }
            } catch (IOException e) {
                logger.error("IO failure (getting app home): "+e, e);
            }
        }
        return applicationHomeDirectory;
    }

    /**
     * @return the full set of configuration data loaded for the application
     */
    Configuration get() {
        return config;
    }

}

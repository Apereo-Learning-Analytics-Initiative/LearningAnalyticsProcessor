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
import org.apache.commons.lang.StringUtils;
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
    File pipelinesDirectory;
    File inputDirectory;
    File outputDirectory;

    @javax.annotation.Resource
    ResourceLoader resourceLoader;

    @PostConstruct
    public void init() throws IOException {
        logger.info("INIT started");
        logger.info("App Home: " + appHome().getAbsolutePath());
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
        File lapConfigProps = new File(appHome(),"lap.properties");
        if (lapConfigProps.exists() && lapConfigProps.canRead()) {
            try {
                config.addConfiguration(new PropertiesConfiguration(lapConfigProps));
            } catch (ConfigurationException e) {
                logger.warn("Unable to load lap.properties file");
            }
        } else {
            logger.info("No external LAP config found: "+lapConfigProps.getAbsolutePath());
        }
        this.config = config;

        // verify the existence of the various dirs
        pipelinesDirectory = verifyDir("dir.pipelines", "piplines");
        inputDirectory = verifyDir("dir.inputs", "inputs");
        outputDirectory = verifyDir("dir.outputs", "outputs");

        // TODO load up the pipeline config files

        logger.info("INIT complete: "+config.getString("app.name")+", home="+applicationHomeDirectory.getAbsolutePath());
    }

    /**
     * Verifies and creates the dir if needed (OR dies if impossible)
     * @param configKey the configured path
     * @param defaultPath the default path if the configured path is bad
     * @return the directory
     */
    private File verifyDir(String configKey, String defaultPath) {
        String dirStr = this.config.getString(configKey);
        File fileDir;
        if (StringUtils.isBlank(dirStr)) {
            dirStr = defaultPath;
            fileDir = new File(appHome(), dirStr);
        } else {
            // check if relative or absolute path
            dirStr = StringUtils.trim(dirStr);
            if (StringUtils.startsWith(dirStr, "/")) {
                fileDir = new File(dirStr);
            } else {
                // this is a relative path
                fileDir = new File(appHome(), dirStr);
            }
        }
        if (!fileDir.exists()) {
            // try to create it
            try {
                //noinspection ResultOfMethodCallIgnored
                fileDir.mkdirs();
                logger.info("Config created "+configKey+" dir: "+fileDir.getAbsolutePath());
            } catch (Exception e) {
                throw new RuntimeException("Could not create dir at: "+fileDir.getAbsolutePath());
            }
        } else if (!fileDir.isDirectory()) {
            throw new RuntimeException("Configured pipeline path is not a directory: "+fileDir.getAbsolutePath());
        } else if (!fileDir.canRead()) {
            throw new RuntimeException("Configured pipeline path is not readable: "+fileDir.getAbsolutePath());
        }
        // update config with absolute path
        this.config.setProperty(configKey, fileDir.getAbsolutePath());
        return fileDir;
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
                    if (lapHomeDir.exists() && lapHomeDir.canRead() && lapHomeDir.isDirectory()) {
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

    /**
     * @return the directory used for storing pipeline processor config files
     */
    public File getPipelinesDirectory() {
        return pipelinesDirectory;
    }

    /**
     * @return the directory used for storing default pipeline inputs
     */
    public File getInputDirectory() {
        return inputDirectory;
    }

    /**
     * @return the directory used for storing default pipeline outputs
     */
    public File getOutputDirectory() {
        return outputDirectory;
    }

}

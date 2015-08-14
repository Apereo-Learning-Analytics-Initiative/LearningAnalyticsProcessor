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
package org.apereo.lap.services.configuration;

import org.apache.commons.configuration.*;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apereo.lap.model.PipelineConfig;
import org.apereo.lap.services.input.SampleCSVInputHandlerService;
import org.apereo.lap.services.storage.StorageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Loads the application configuration from a series of files
 *
 * Also handles the init of the application home directory (controlled by "LAP_HOME" env or -D variable)
 * 
 * @author Aaron Zeckoski (azeckoski @ unicon.net) (azeckoski @ vt.edu)
 */
@Component("configuration")
public class ConfigurationService {
  
  /**
   * TODO - Refactor this class to:
   * 1. Not rely on File objects (so we can run as jar)
   * 2. Utilize Spring / Spring Boot properties features (specifically see init below directly loading application.properties which won't allow us to use spring profiles)
   */
  
  

    private static final Logger logger = LoggerFactory.getLogger(ConfigurationService.class);

    // Configuration objects
    private Configuration config;
    private ConcurrentHashMap<String, PipelineConfig> pipelineConfigs;

    // working directories
    File applicationHomeDirectory;
    File pipelinesDirectory;
    public File inputDirectory;
    File outputDirectory;

    @javax.annotation.Resource
    ResourceLoader resourceLoader;
    @Autowired StorageService storage;
    /**
     * System defined path separator Windows = "\", Unix = "/"
     */
    public static final String SLASH = System.getProperty("file.separator");

    @PostConstruct
    public void init() throws IOException {
        logger.info("INIT started");
        logger.info("App Home: " + appHome().getAbsolutePath());

        CompositeConfiguration config = new CompositeConfiguration();

        // now try to load external config settings
        config.addConfiguration(new SystemConfiguration());
        File lapConfigProps = resourceLoader.getResource("classpath:application.properties").getFile();
        if (lapConfigProps.exists() && lapConfigProps.canRead()) {
            try {
                config.addConfiguration(new PropertiesConfiguration(lapConfigProps));
            } catch (ConfigurationException e) {
                logger.warn("Unable to load application.properties file");
            }
        }
        this.config = config;

        // verify the existence of the various dirs
        pipelinesDirectory = verifyDir("dir.pipelines", "piplines");
        inputDirectory = verifyDir("dir.inputs", "inputs");
        outputDirectory = verifyDir("dir.outputs", "outputs");

        pipelineConfigs = new ConcurrentHashMap<>();
        // first load the internal ones (must be listed explicitly for now)
        Resource pipelineSample = resourceLoader.getResource("classpath:pipelines" + SLASH + "sample.xml");
        PipelineConfig plcfg = processPipelineConfigFile(pipelineSample.getFile());
        if (plcfg != null) {
            pipelineConfigs.put(plcfg.getType(), plcfg);
        }
        // then try to load the external ones
        File[] pipelineFiles = pipelinesDirectory.listFiles();
        if (pipelineFiles != null && pipelineFiles.length > 0) {
            for (final File fileEntry : pipelineFiles) {
                if (fileEntry.isFile()) {
                    PipelineConfig filePLC = processPipelineConfigFile(pipelineSample.getFile());
                    if (filePLC != null) {
                        pipelineConfigs.put(filePLC.getType(), filePLC);
                    }
                }
            }
        }
    }

    /**
     * @param pipelineConfigFile a File for the XML for a pipeline config
     * @return the pipeline config OR null if the config cannot be loaded
     */
    PipelineConfig processPipelineConfigFile(File pipelineConfigFile) {
        XMLConfiguration xmlcfg = null;
        try {
            xmlcfg = new XMLConfiguration(pipelineConfigFile);
        } catch (ConfigurationException e) {
            logger.error("Invalid XML in pipeline config file ("+pipelineConfigFile.getAbsolutePath()+") (cannot process file): "+e);
        }
        PipelineConfig plcfg = PipelineConfig.makeConfigFromXML(this, storage, xmlcfg);
        if (plcfg.isValid()) {
            logger.info("Pipeline config ("+plcfg.getType()+") loaded from: "+pipelineConfigFile.getAbsolutePath());
        } else {
            logger.warn("Invalid pipeline config file ("+pipelineConfigFile.getAbsolutePath()+"): "+plcfg.getInvalidReasons());
            plcfg = null;
        }
        return plcfg;
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
            if (StringUtils.startsWith(dirStr, SLASH)) {
                fileDir = new File(dirStr);
            } else {
                // this is a relative path
                fileDir = new File(appHome(), dirStr);
            }
        }
        if (!fileDir.exists()) {
            // try to create it
            try {
                if (fileDir.mkdirs()) {
                    logger.info("Config created "+configKey+" dir: "+fileDir.getAbsolutePath());
                } else {
                    throw new RuntimeException("Could not create dir at: "+fileDir.getAbsolutePath());
                }
            } catch (Exception e) {
                throw new RuntimeException("Failure trying to create dir at: "+fileDir.getAbsolutePath()+": "+e);
            }
        } else if (!fileDir.isDirectory()) {
            throw new RuntimeException("Configured pipeline path is not a directory: "+fileDir.getAbsolutePath());
        } else if (!fileDir.canRead()) {
            throw new RuntimeException("Configured pipeline path is not readable: "+fileDir.getAbsolutePath());
        } else {
            logger.info("Config using existing "+configKey+" dir: "+fileDir.getAbsolutePath());
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
    public Configuration config() {
        return config;
    }

    /**
     * @param key the config key
     * @return the value for this config OR null if none exists
     */
    <T> T get(String key, T defaultValue) {
        Object result = defaultValue;
        if (StringUtils.isNotBlank(key) && config.containsKey(key)) {
            if (defaultValue == null) {
                result = config.getProperty(key);
            } else {
                if (defaultValue instanceof Boolean) {
                    result = config.getBoolean(key);
                } else if (defaultValue instanceof Integer) {
                    result = config.getInt(key);
                } else if (defaultValue instanceof Long) {
                    result = config.getLong(key);
                } else if (defaultValue instanceof String) {
                    result = config.getString(key);
                }
            }
        }
        //noinspection unchecked
        return (T) result;
    }

    /**
     * @param key the config key
     * @return the value for this config OR null if none exists
     */
    public boolean is(String key) {
        boolean result = false;
        if (StringUtils.isNotBlank(key)) {
            result = config.getBoolean(key);
        }
        return result;
    }

    /**
     * @return the full Configuration object
     */
    public Configuration getConfig() {
        return config;
    }

    /**
     * @param type the pipeline type (unique id for the pipeline)
     * @return the PipelineConfig for this type
     */
    public PipelineConfig getPipelineConfig(String type) {
        assert StringUtils.isNotBlank(type);
        return pipelineConfigs.get(type);
    }

    /**
     * @return the map of all known pipeline configs
     */
    public ConcurrentHashMap<String, PipelineConfig> getPipelineConfigs() {
        return pipelineConfigs;
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

    /**
     * @return the spring resource loader
     */
    public ResourceLoader getResourceLoader() {
        return resourceLoader;
    }

}

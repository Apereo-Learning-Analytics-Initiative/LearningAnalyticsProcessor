/**
 * Copyright 2013 Unicon (R) Licensed under the
 * Educational Community License, Version 2.0 (the "License"); you may
 * not use this file except in compliance with the License. You may
 * obtain a copy of the License at
 * <p/>
 * http://www.osedu.org/licenses/ECL-2.0
 * <p/>
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an "AS IS"
 * BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing
 * permissions and limitations under the License.
 */
package org.apereo.lap.services.configuration;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.XMLConfiguration;
import org.apache.commons.lang.StringUtils;
import org.apereo.lap.model.PipelineConfig;
import org.apereo.lap.services.storage.StorageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Loads the application configuration from a series of files
 *
 * Also handles the init of the application home directory (controlled by
 * "LAP_HOME" env or -D variable)
 *
 * @author Aaron Zeckoski (azeckoski @ unicon.net) (azeckoski @ vt.edu)
 */
@Component("configuration")
public class ConfigurationService {
  private static final Logger logger = LoggerFactory.getLogger(ConfigurationService.class);

  // Configuration objects
  private ConcurrentHashMap<String, PipelineConfig> pipelineConfigs;
  private Path applicationHomeDirectory;
  private Path pipelinesDirectory;
  private Path inputDirectory;
  private Path outputDirectory;

  // working directories
  @Value("${lap.home:#{null}}")
  private String lapHome;
  @Value("${dir.pipelines:#{null}}")
  private String dirPipelines;
  @Value("${dir.inputs:#{null}}")
  private String dirInputs;
  @Value("${dir.outputs:#{null}}")
  private String dirOutputs;
  @Value("${input.init.load.csv:false}")
  private boolean inputInitLoadCSV;

  @Autowired
  StorageService storage;

  @PostConstruct
  public void init() throws IOException {
    logger.info("INIT started");

    if (StringUtils.isNotBlank(lapHome)) {
      applicationHomeDirectory = Paths.get(lapHome);
    } else {
      // if not configured specifically, use $PWD/lapHome
      applicationHomeDirectory = Paths.get(System.getProperty("user.dir"), "lapHome");
    }

    logger.info("App Home: " + applicationHomeDirectory);

    if (StringUtils.isNotBlank(dirPipelines)) {
      pipelinesDirectory = Paths.get(dirPipelines);
    } else {
      pipelinesDirectory = applicationHomeDirectory.resolve("pipelines");
    }

    if (StringUtils.isNotBlank(dirInputs)) {
      inputDirectory = Paths.get(dirInputs);
    } else {
      inputDirectory = applicationHomeDirectory.resolve("inputs");
    }

    if (StringUtils.isNotBlank(dirOutputs)) {
      outputDirectory = Paths.get(dirOutputs);
    } else {
      outputDirectory = applicationHomeDirectory.resolve("outputs");
    }
    
    logger.info("Pipeline Dir: " + pipelinesDirectory);
    logger.info("Inputs Dir: " + inputDirectory);
    logger.info("Outputs Dir: " + outputDirectory);

    if (!Files.isDirectory(outputDirectory)) {
      Files.createDirectories(outputDirectory);
    }

    pipelineConfigs = new ConcurrentHashMap<>();

    // load the external pipeline configs
    for (Path entry : Files.newDirectoryStream(pipelinesDirectory, new DirectoryStream.Filter<Path>() {
      @Override
      public boolean accept(Path entry) throws IOException {
        return Files.isRegularFile(entry);
      }
    })) {
      PipelineConfig filePLC = buildPipelineConfig(entry);
      if (filePLC != null) {
        pipelineConfigs.put(filePLC.getType(), filePLC);
      }
    }
  }

  private PipelineConfig buildPipelineConfig(Path path) throws IOException {
    XMLConfiguration xml = new XMLConfiguration();
    try (InputStream in = Files.newInputStream(path)) {
      try {
        xml.load(in);
      } catch (ConfigurationException ce) {
        throw new IOException(ce);
      }
    }
    PipelineConfig cfg = PipelineConfig.makeConfigFromXML(this, storage, xml);
    if (cfg.isValid()) {
      logger.info("Pipeline config ({}) loaded from: {}", cfg.getType(), path);
    } else {
      logger.warn("Invalid pipeline config file ({}): {}", cfg.getInvalidReasons(), path);
      cfg = null;
    }
    return cfg;
  }

  @PreDestroy
  public void destroy() {
    logger.info("DESTROY");
  }

  /**
   * @param type
   *          the pipeline type (unique id for the pipeline)
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

  public Path getApplicationHomeDirectory() {
    return applicationHomeDirectory;
  }

  /**
   * @return the directory used for storing pipeline processor config files
   */
  public Path getPipelinesDirectory() {
    return pipelinesDirectory;
  }

  /**
   * @return the directory used for storing default pipeline inputs
   */
  public Path getInputDirectory() {
    return inputDirectory;
  }

  /**
   * @return the directory used for storing default pipeline outputs
   */
  public Path getOutputDirectory() {
    return outputDirectory;
  }

  public boolean isInputInitLoadCSV() {
    return inputInitLoadCSV;
  }
}

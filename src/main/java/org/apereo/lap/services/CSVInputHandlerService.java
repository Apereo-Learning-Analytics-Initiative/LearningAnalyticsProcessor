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

import org.apache.commons.configuration.HierarchicalConfiguration;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.apereo.lap.services.input.InputHandler;
import org.apereo.lap.services.input.csv.BaseCSVInputHandler;
import org.apereo.lap.services.input.csv.CSVInputHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.annotation.Resource;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Handles the inputs by reading the data into the temporary data storage
 * Validates the inputs and ensures the data is available to the pipeline processor
 * 
 * @author Aaron Zeckoski (azeckoski @ unicon.net) (azeckoski @ vt.edu)
 */
public class CSVInputHandlerService extends BaseInputHandlerService {

    private static final Logger logger = LoggerFactory.getLogger(CSVInputHandlerService.class);

    public CSVInputHandlerService()
    {
    	
    }
    
    public CSVInputHandlerService(ConfigurationService configuration, StorageService storage, HierarchicalConfiguration inputConfiguration)
    {
    	super(inputConfiguration);
    	this.configuration = configuration;
    	this.storage = storage;
    	this.init();
    }

	@Override
	public Type getType() {
		return Type.CSV;
	}
    
    public void init() {
        super.init();
        
        if (configuration.config.getBoolean("input.copy.samples", false)) {
            copySampleExtractCSVs();
        }
        if (configuration.config.getBoolean("input.init.load.csv", false)) {
        	loadInputCollection();
        }
    }

    /**
     * @param type the class of input handlers we are looking for
     * @param <T> InputHandler type (e.g. CSVInputHandler)
     * @return all handlers for a given type mapped by their unique handled type of data OR empty map if there are none
     */
    public <T extends InputHandler> Map<String, T> findHandlers(Class<T> type) {
        assert type != null;
        Map<String, T> handlers = new HashMap<>(); // empty set by default
        if (CSVInputHandler.class.isAssignableFrom(type)) {
            //noinspection unchecked
            handlers = (Map<String, T>) BaseCSVInputHandler.makeCSVHandlers(configuration, storage.getTempJdbcTemplate());
        } // add other types here
        return handlers;
    }

    /**
     * Copies the 5 sample extract CSVs from the classpath to the inputs directory
     */
    void copySampleExtractCSVs() {
        String extractsFolder = "extracts" + ConfigurationService.SLASH;
        logger.info("copySampleExtractCSVs start");
        copySampleCSV(extractsFolder, "personal.csv");
        copySampleCSV(extractsFolder, "course.csv");
        copySampleCSV(extractsFolder, "enrollment.csv");
        copySampleCSV(extractsFolder, "grade.csv");
        copySampleCSV(extractsFolder, "activity.csv");
        logger.info("copySampleExtractCSVs to "+configuration.inputDirectory.getAbsolutePath()+" complete");
    }

    /**
     * Loads and verifies the standard CSVs from the inputs directory
     * @param inputCollections all collections to load (empty indicates that all should be loaded, null indicates none should be loaded)
     * @return a map of all loaded collection types -> the results of the load
     */
    public Map<InputCollection, InputHandler.ReadResult> loadInputCollection(InputCollection... inputCollections) {
        Map<InputCollection, InputHandler.ReadResult> loaded = new HashMap<>();
        if (inputCollections == null) {
            logger.info("Not loading any CSV files (empty inputCollections param)");
        } else {
            logger.info("load CSV files from: "+configuration.inputDirectory.getAbsolutePath());
            try {
                // Initialize the CSV handlers
                Map<String, CSVInputHandler> csvInputHandlerMap = findHandlers(CSVInputHandler.class);
                Collection<CSVInputHandler> csvInputHandlers = new ArrayList<>(csvInputHandlerMap.values());
                if (inputCollections.length > 0) { // null or empty means include them all
                    for (CSVInputHandler entry : csvInputHandlers) {
                        if (!ArrayUtils.contains(inputCollections, entry.getHandledCollection())) {
                            // filtering this one out
                            csvInputHandlerMap.remove(entry.getCSVFilename());
                        }
                    }
                    // rebuild it from whatever is left
                    csvInputHandlers = csvInputHandlerMap.values();
                }
                logger.info("Loaded "+csvInputHandlers.size()+" CSV InputHandlers: "+csvInputHandlerMap.keySet());

                // First we verify the CSV files
                for (CSVInputHandler csvInputHandler : csvInputHandlers) {
                    csvInputHandler.readCSV(true); // force it true just in case
                    logger.info(csvInputHandler.getCSVFilename()+" file and header appear valid");
                }
                // Next we load the data into the temp DB
                for (CSVInputHandler csvInputHandler : csvInputHandlers) {
                    InputHandler.ReadResult result = csvInputHandler.readInputIntoDB();
                    if (!result.failures.isEmpty()) {
                        logger.error(result.failures.size()+" failures while parsing "+result.handledType+":\n"+ StringUtils.join(result.failures, "\n")+"\n");
                    }
                    logger.info(result.loaded+" lines from "+result.handledType+" (out of "+result.total+" lines) inserted into temp DB (with "+result.failed+" failures): "+result);
                    loaded.put(csvInputHandler.getHandledCollection(), result);
                    loadedInputCollections.put(csvInputHandler.getHandledCollection(), csvInputHandler);
                }

                logger.info("Loaded CSV files: "+loadedInputCollections.keySet());
            } catch (Exception e) {
                String msg = "Failed to load CSVs file(s): "+e;
                logger.error(msg);
                throw new RuntimeException(msg, e);
            }
        }
        return loaded;
    }
    
    /**
     * Copies a file from a classpath dir to the file inputs dir
     * @param classpathDir the dir on the classpath with the same file (include trailing slash)
     * @param filename the csv file to copy from extracts to the inputs location
     * @throws java.lang.RuntimeException if the file cannot copy
     */
    public void copySampleCSV(String classpathDir, String filename) {
        try {
            IOUtils.copy(
                    CSVInputHandlerService.class.getClassLoader().getResourceAsStream(classpathDir + filename),
                    new FileOutputStream(new File(configuration.inputDirectory, filename))
            );
        } catch (IOException e) {
            throw new RuntimeException("Cannot find the sample file to copy: "+filename);
        }
    }
}

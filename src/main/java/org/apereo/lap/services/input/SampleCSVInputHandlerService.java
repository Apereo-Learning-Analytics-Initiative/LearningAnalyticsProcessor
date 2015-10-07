/*******************************************************************************
 * Copyright (c) 2015 Unicon (R) Licensed under the
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
 *******************************************************************************/
package org.apereo.lap.services.input;

import org.apache.commons.configuration.HierarchicalConfiguration;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.apereo.lap.services.configuration.ConfigurationService;
import org.apereo.lap.services.input.handlers.InputHandler;
import org.apereo.lap.services.input.handlers.csv.BaseCSVInputHandler;
import org.apereo.lap.services.input.handlers.csv.CSVInputHandler;
import org.apereo.lap.services.storage.StorageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * Handles the inputs by reading the data into the temporary data storage
 * Validates the inputs and ensures the data is available to the pipeline processor
 *
 * @author Aaron Zeckoski (azeckoski @ unicon.net) (azeckoski @ vt.edu)
 */
public class SampleCSVInputHandlerService extends BaseInputHandlerService {

    private static final Logger logger = LoggerFactory.getLogger(SampleCSVInputHandlerService.class);

    public SampleCSVInputHandlerService(ConfigurationService configuration, StorageService storage, HierarchicalConfiguration inputConfiguration)
    {
    	super(inputConfiguration);
    	this.configuration = configuration;
    	this.storage = storage;
    	this.init();
    }

    @Override
	public Type getType() {
		return Type.SAMPLECSV;
	}

    public void init() {
        super.init();

        if (configuration.isInputInitLoadCSV()) {
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
     * Loads and verifies the standard CSVs from the inputs directory
     * @param inputCollections all collections to load (empty indicates that all should be loaded, null indicates none should be loaded)
     * @return a map of all loaded collection types -> the results of the load
     */
    public Map<InputCollection, InputHandler.ReadResult> loadInputCollection(InputCollection... inputCollections) {
        Map<InputCollection, InputHandler.ReadResult> loaded = new HashMap<>();
        if (inputCollections == null) {
            logger.info("Not loading any CSV files (empty inputCollections param)");
        } else {
            logger.info("load CSV files from: "+configuration.getInputDirectory());
            try {
                // Initialize the CSV handlers
                Map<String, CSVInputHandler> csvInputHandlerMap = findHandlers(CSVInputHandler.class);
                Collection<CSVInputHandler> csvInputHandlers = new ArrayList<>(csvInputHandlerMap.values());
                if (inputCollections.length > 0) { // null or empty means include them all
                    for (CSVInputHandler entry : csvInputHandlers) {
                        if (!ArrayUtils.contains(inputCollections, entry.getInputCollection())) {
                            // filtering this one out
                            csvInputHandlerMap.remove(entry.getPath());
                        }
                    }
                    // rebuild it from whatever is left
                    csvInputHandlers = csvInputHandlerMap.values();
                }
                logger.info("Loaded "+csvInputHandlers.size()+" CSV InputHandlers: "+csvInputHandlerMap.keySet());

                // First we verify the CSV files
                for (CSVInputHandler csvInputHandler : csvInputHandlers) {
                    csvInputHandler.readCSV(true); // force it true just in case
                    logger.info(csvInputHandler.getPath()+" file and header appear valid");
                }
                // Next we load the data into the temp DB
                for (CSVInputHandler csvInputHandler : csvInputHandlers) {
                    InputHandler.ReadResult result = csvInputHandler.readInputIntoDB();
                    if (!result.failures.isEmpty()) {
                        logger.error(result.failures.size()+" failures while parsing "+result.handledType+":\n"+ StringUtils.join(result.failures, "\n")+"\n");
                    }
                    logger.info(result.loaded+" lines from "+result.handledType+" (out of "+result.total+" lines) inserted into temp DB (with "+result.failed+" failures): "+result);
                    loaded.put(csvInputHandler.getInputCollection(), result);
                    loadedInputCollections.put(csvInputHandler.getInputCollection(), csvInputHandler);
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
}

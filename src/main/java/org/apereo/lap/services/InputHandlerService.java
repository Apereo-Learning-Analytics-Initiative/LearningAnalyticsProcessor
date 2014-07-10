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
@Component
public class InputHandlerService {

    private static final Logger logger = LoggerFactory.getLogger(InputHandlerService.class);

    @Resource
    ConfigurationService configuration;

    @Resource
    StorageService storage;
    /**
     * Defines the valid types of input the system can handle
     */
    public static enum InputType {
        CSV, STORAGE;
        public static InputType fromString(String str) {
            if (StringUtils.equalsIgnoreCase(str, CSV.name())) {
                return CSV;
            } else {
                throw new IllegalArgumentException("input type ("+str+") does not match the valid types: "+ ArrayUtils.toString(InputType.values()));
            }
        }
    }

    /**
     * Defines the data collection sets that
     */
    public static enum InputCollection {
        PERSONAL, COURSE, ENROLLMENT, GRADE, ACTIVITY;
        public static InputCollection fromString(String str) {
            if (StringUtils.equalsIgnoreCase(str, PERSONAL.name())) {
                return PERSONAL;
            } else if (StringUtils.equalsIgnoreCase(str, COURSE.name())) {
                return COURSE;
            } else if (StringUtils.equalsIgnoreCase(str, ENROLLMENT.name())) {
                return ENROLLMENT;
            } else if (StringUtils.equalsIgnoreCase(str, GRADE.name())) {
                return GRADE;
            } else if (StringUtils.equalsIgnoreCase(str, ACTIVITY.name())) {
                return ACTIVITY;
            } else {
                throw new IllegalArgumentException("collection type ("+str+") does not match the valid types: "+ ArrayUtils.toString(InputCollection.values()));
            }
        }
    }

    /**
     * Stores the map of loaded input collection names and their handlers
     * NOTE: these are the things that were already loaded and exist in the temp data stores
     */
    Map<InputCollection, InputHandler> loadedInputCollections;
    /**
     * Stored the set of all loaded input types
     */
    Set<InputType> loadedInputTypes;

    @PostConstruct
    public void init() {
        logger.info("INIT");
        loadedInputCollections = new ConcurrentHashMap<>();
        //noinspection unchecked
        loadedInputTypes = Collections.newSetFromMap(new ConcurrentHashMap());
        if (configuration.config.getBoolean("input.copy.samples", false)) {
            copySampleExtractCSVs();
        }
        if (configuration.config.getBoolean("input.init.load.csv", false)) {
            loadCSVs();
        }
    }

    @PreDestroy
    public void destroy() {
        logger.info("DESTROY");
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
     * Load the inputs by collection type needed for the pipeline
     * By default we should not reload the data or reset the store
     * @param reloadData if true, reload the inputs data even if already loaded
     * @param resetStore if true, reset the data store
     * @param inputCollections these collection types will be loaded (empty indicates that all should be loaded, null indicates none should be loaded)
     * @return the set of all loaded collections (empty if none loaded)
     */
    Set<InputCollection> loadInputCollections(boolean reloadData, boolean resetStore, Set<InputCollection> inputCollections) {
        Set<InputCollection> loaded = new HashSet<>();
        if (resetStore) {
            storage.resetTempStore();
        }
        if (inputCollections == null) {
            logger.info("No collections will be loaded (null inputCollection)");
        } else {
            // set up the set of collections to be loaded
            Set<InputCollection> icToLoad = new LinkedHashSet<>();
            if (inputCollections.isEmpty()) {
                // add the complete known set
                icToLoad.add(InputCollection.PERSONAL);
                icToLoad.add(InputCollection.COURSE);
                icToLoad.add(InputCollection.ENROLLMENT);
                icToLoad.add(InputCollection.GRADE);
                icToLoad.add(InputCollection.ACTIVITY);
                inputCollections.addAll(icToLoad);
            } else {
                icToLoad.addAll(inputCollections);
            }
            if (!reloadData) {
                // only load if not already loaded
                for (InputCollection ic : inputCollections) {
                    if (loadedInputCollections.containsKey(ic)) {
                        // remove the ones already loaded so they are not loaded again
                        icToLoad.remove(ic);
                    }
                }
            }
            // see what we need to load
            if (icToLoad.isEmpty()) {
                logger.info("No input collections to load (already loaded) from set: "+inputCollections);
            } else {
                // for now we only have one source to load from, might need to handle more sources later
                Map<InputCollection, InputHandler.ReadResult> loadedResults = loadCSVs(icToLoad.toArray(new InputCollection[icToLoad.size()]));
                loaded = loadedResults.keySet();
                logger.info("Input collections loaded ("+loaded+") of original: "+inputCollections);
            }
        }
        return loaded;
    }

    /**
     * @param inputType the input type to load
     * @param resetStore if true, clear the current temp database before loading
     * @return the loaded set of collections from this input
     */
    public Set<InputCollection> loadInputType(InputType inputType, boolean resetStore) {
        if (resetStore) {
            storage.resetTempStore();
        }
        Set<InputCollection> loaded = new HashSet<>();
        if (InputType.CSV == inputType) {
            Map<InputCollection, InputHandler.ReadResult> loadedResults = loadCSVs();
            loaded = loadedResults.keySet();
            loadedInputTypes.add(InputType.CSV);
        }
        return loaded;
    }

    /**
     * @return the collection of all InputCollection types which have been loaded in this input handler
     */
    public Collection<InputCollection> getLoadedInputCollections() {
        return loadedInputCollections.keySet();
    }

    /**
     * @return the collection of all input types that have been loaded
     */
    public Set<InputType> getLoadedInputTypes() {
        return loadedInputTypes;
    }

    /**
     * Copies the 5 sample extract CSVs from the classpath to the inputs directory
     */
    void copySampleExtractCSVs() {
        logger.info("copySampleExtractCSVs start");
        copySampleCSV("extracts/", "personal.csv");
        copySampleCSV("extracts/", "course.csv");
        copySampleCSV("extracts/", "enrollment.csv");
        copySampleCSV("extracts/", "grade.csv");
        copySampleCSV("extracts/", "activity.csv");
        logger.info("copySampleExtractCSVs to "+configuration.inputDirectory.getAbsolutePath()+" complete");
    }

    /**
     * Loads and verifies the standard CSVs from the inputs directory
     * @param inputCollections all collections to load (empty indicates that all should be loaded, null indicates none should be loaded)
     * @return a map of all loaded collection types -> the results of the load
     */
    Map<InputCollection, InputHandler.ReadResult> loadCSVs(InputCollection... inputCollections) {
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
    private void copySampleCSV(String classpathDir, String filename) {
        try {
            IOUtils.copy(
                    InputHandlerService.class.getClassLoader().getResourceAsStream(classpathDir + filename),
                    new FileOutputStream(new File(configuration.inputDirectory, filename))
            );
        } catch (IOException e) {
            throw new RuntimeException("Cannot find the sample file to copy: "+filename);
        }
    }

}

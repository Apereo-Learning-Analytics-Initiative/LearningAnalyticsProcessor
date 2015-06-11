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

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.annotation.Resource;

import org.apache.commons.configuration.HierarchicalConfiguration;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.apereo.lap.services.input.InputHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Handles the inputs by reading the data into the temporary data storage
 * Validates the inputs and ensures the data is available to the pipeline processor
 * 
 * @author Aaron Zeckoski (azeckoski @ unicon.net) (azeckoski @ vt.edu)
 */
public abstract class BaseInputHandlerService {

    private static final Logger logger = LoggerFactory.getLogger(SampleCSVInputHandlerService.class);

    public BaseInputHandlerService(HierarchicalConfiguration inputConfiguration)
    {
    	
    }

    protected ConfigurationService configuration;
    protected StorageService storage;
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
    
    public static BaseInputHandlerService getInputHandler(String type, HierarchicalConfiguration sourceConfiguration, ConfigurationService configuration, StorageService storage) {
    	if (StringUtils.equalsIgnoreCase(type, BaseInputHandlerService.Type.SAMPLECSV.name())) {
    			return new SampleCSVInputHandlerService(configuration, storage, sourceConfiguration);
    	}
    	if (StringUtils.equalsIgnoreCase(type, BaseInputHandlerService.Type.CSV.name())) {
			return new CSVInputHandlerService(configuration, storage, sourceConfiguration);
    	}
    	
    	 throw new IllegalArgumentException("collection type ("+type+") does not match the valid types: "+ ArrayUtils.toString(Type.values()));
    }

    /**
     * Defines the data collection sets that
     */
    public static enum Type {
        SAMPLECSV, CSV, DATABASE, HTTP;
        public static Type fromString(String str) {
            if (StringUtils.equalsIgnoreCase(str, SAMPLECSV.name())) {
                return SAMPLECSV;
            } else if (StringUtils.equalsIgnoreCase(str, CSV.name())) {
                return CSV;
            } else if (StringUtils.equalsIgnoreCase(str, DATABASE.name())) {
                return DATABASE;
            } else if (StringUtils.equalsIgnoreCase(str, HTTP.name())) {
                return HTTP;
            } else {
                throw new IllegalArgumentException("collection type ("+str+") does not match the valid types: "+ ArrayUtils.toString(InputCollection.values()));
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
    protected Map<InputCollection, InputHandler> loadedInputCollections;
    
    /**
     * Stored the set of all loaded input types
     */
    protected Set<InputType> loadedInputTypes;

    public void init() {
        logger.info("INIT");
        loadedInputCollections = new ConcurrentHashMap<>();
        //noinspection unchecked
        loadedInputTypes = Collections.newSetFromMap(new ConcurrentHashMap());
    }

    /**
     * @param the type of the input handler
     * @return input type for implemenation
     */
    public abstract Type getType();
    
    /**
     * @param type the class of input handlers we are looking for
     * @param <T> InputHandler type (e.g. CSVInputHandler)
     * @return all handlers for a given type mapped by their unique handled type of data OR empty map if there are none
     */
    public abstract <T extends InputHandler> Map<String, T> findHandlers(Class<T> type);

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
                Map<InputCollection, InputHandler.ReadResult> loadedResults = loadInputCollection(icToLoad.toArray(new InputCollection[icToLoad.size()]));
                loaded = loadedResults.keySet();
                logger.info("Input collections loaded ("+loaded+") of original: "+inputCollections);
            }
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
     * Loads and verifies the standard CSVs from the inputs directory
     * @param inputCollections all collections to load (empty indicates that all should be loaded, null indicates none should be loaded)
     * @return a map of all loaded collection types -> the results of the load
     */
    public abstract Map<InputCollection, InputHandler.ReadResult> loadInputCollection(InputCollection... inputCollections);
}

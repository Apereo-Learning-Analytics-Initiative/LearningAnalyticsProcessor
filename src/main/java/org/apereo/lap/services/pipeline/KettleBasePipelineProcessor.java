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
import java.io.FileWriter;
import java.io.IOException;

import javax.annotation.Resource;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

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
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

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
     * The Kettle root directory
     */
    protected static final String KETTLE_ROOT_DIR = "kettle";

    /**
     * The Kettle pipeline root directory
     */
    protected static final String KETTLE_PIPELINE_ROOT_DIR = "scoring_sample";

    /**
     * Shared objects (database connections, etc.) located in .kettle/shared.xml
     */
    private SharedObjects sharedObjects;

    /**
     * Names of the database connections used in the .kjb and .ktr files
     */
    protected String[] databaseConnectionNames;

    /**
     * The Scoring transform entry name
     */
    protected static final String SCORING_TRANSFORM_ENTRY_NAME = "LAP Scoring Transformation";

    /**
     * The Scoring transform filename
     */
    protected static final String SCORING_TRANSFORM_FILE = "LAP_Scoring_Sample.ktr";

    /**
     * The assign weights job file entry name
     */
    protected static final String WEKA_SCORING_STEP_NAME = "Weka Scoring";

    /**
     * The tag of the Weka Scoring configuartion
     */
    protected static final String WEKA_SCORING_TAG_NAME = "weka_scoring";

    /**
     * The tag specifying the Weka Scoring model file
     */
    protected static final String WEKA_SCORING_MODEL_FILE_TAG_NAME = "model_file_name";

    /**
     * The PMML XML filename
     */
    protected static final String SCORING_MODEL_FILE_NAME = "oaai.lap.logistic.pmml.xml";

    /**
     * The assign weights job file entry name
     */
    protected static final String ASSIGN_WEIGHTS_ENTRY_NAME = "AssignWeights_Grades";

    /**
     * The assign weights job file name
     */
    protected static final String ASSIGN_WEIGHTS_FILE_NAME = "Sample_Pipeline1_ETL_Gradebook_AssignWeightsJob.kjb";

    /**
     * Has the Weks Scoring Model file path been updated?
     */
    protected boolean isWekaScoringFileUpdated = false;

    /**
     * Has Kettle been configured?
     */
    private boolean isKettleConfigured = false;

    /**
     * Make the file path to a pipeline file (e.g. /kettle/scoring_sample/LAP_Scoring_Sample.ktr)
     * 
     * @param filename the name of the file
     * @return the path to the file
     */
    protected String makeFilePath(String filename) {
        return SLASH + KETTLE_ROOT_DIR + SLASH + KETTLE_PIPELINE_ROOT_DIR + SLASH + filename;
    }

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
     * Creates shared connections for use in transformations and jobs
     * Uses connection properties from db.properties with a prefix of "db."
     * Stores the dynamic configuration in a shared.xml file
     * 
     * Currently, only an H2 database is configured
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
            String plugins = resourceLoader.getResource("classpath:kettle" + SLASH + "plugins").getURI().toString();
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
     * Create a temp file for input
     * 
     * @param filename the file name
     * @param extension the file extension
     * @return the newly created temporary file object
     */
    protected File createTempInputFile(String filename, String extension) {
        assert StringUtils.isNotEmpty(filename);
        assert StringUtils.isNotEmpty(extension);

        try {
            File newTempFile = File.createTempFile(filename, extension);
            newTempFile.deleteOnExit();

            return newTempFile;
        } catch (Exception e) {
            throw new RuntimeException("Error creating temporary file: " + filename + "." + extension, e);
        }
    }

    /**
     * Deletes a given file from the file system
     * 
     * @param file the File object to delete
     */
    protected void deleteTempInputFile(File file) {
        assert file != null;

        try {
            file.delete();
        } catch (Exception e) {
            throw new RuntimeException("Error deleting file: " + file.getAbsolutePath(), e);
        }
    }

    /**
     * Writes the contents of a string to the given file
     * 
     * @param file the file to write the contents to
     * @param contents the contents to write in the file
     */
    protected void writeStringToFile(File file, String contents) {
        assert file != null;

        FileWriter fileWriter = null;

        try {
            fileWriter = new FileWriter(file);
            fileWriter.write(contents);
        } catch (Exception e) {
            throw new RuntimeException("Error writing contents to file: " + file.getAbsolutePath(), e);
        } finally {
            try {
                if (fileWriter != null) {
                    fileWriter.close();
                }
            } catch (IOException e) {
                throw new RuntimeException("Error writing contents to file: " + file.getAbsolutePath(), e);
            }
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

    /**
     * Dynamically change the file path to the Weka Scoring model file
     * 
     * @param file the transformation file
     */
    protected void updateWekaScoringModel(File file) {
        assert file != null;

        try {
            // read the XML file
            Document document = parseXmlDocument(file);

            // get the "step" nodes
            NodeList steps = document.getElementsByTagName("step");
            for (int i = 0; i < steps.getLength(); i++) {
                Node node = steps.item(i);
                NodeList stepNodes = node.getChildNodes();

                // iterate over each child node, only if the node is a Weka Scoring node
                for (int j = 0; j < stepNodes.getLength(); j++) {
                    Node stepNode = stepNodes.item(j);
                    if (StringUtils.equalsIgnoreCase(stepNode.getNodeName(), "name")) {
                        if(!StringUtils.equalsIgnoreCase(stepNode.getTextContent(), WEKA_SCORING_STEP_NAME)) {
                            break;
                        } else {
                            continue;
                        }
                    } else if (StringUtils.equalsIgnoreCase(stepNode.getNodeName(), WEKA_SCORING_TAG_NAME)) {
                        NodeList wekaScoringNodeList = stepNode.getChildNodes();

                        // iterate over the Weka Scoring child nodes
                        for (int k = 0; k < wekaScoringNodeList.getLength(); k++) {
                            Node wekaScoringNode = wekaScoringNodeList.item(k);

                            // update the file path to the appropriate file on the classpath
                            if (StringUtils.equalsIgnoreCase(wekaScoringNode.getNodeName(), WEKA_SCORING_MODEL_FILE_TAG_NAME)) {
                                File scoringModelFile = getFile(makeFilePath(SCORING_MODEL_FILE_NAME));
                                logger.info("Updated Weka Scoring Model file path from '" + wekaScoringNode.getTextContent() + "' to '" + scoringModelFile.getAbsolutePath() + "'");
                                wekaScoringNode.setTextContent(scoringModelFile.getAbsolutePath());
                                isWekaScoringFileUpdated = true;

                                break;
                            }
                        }

                        // if Weka Scoring model file updated, no need to continue processing
                        if (isWekaScoringFileUpdated) {
                            break;
                        }
                    }
                }

                // if Weka Scoring model file updated, no need to continue processing
                if (isWekaScoringFileUpdated) {
                    break;
                }
            }

            // write the XML document to a file on the file system
            writeXmlDocument(document, file);
        } catch (Exception e) {
            throw new RuntimeException("Error updating Weka Scoring model file path transformation XML file: " + file.getAbsoluteFile(), e);
        }
    }

    /**
     * Parses an XML file into a Document object
     * 
     * @param file the XML file to parse
     * @return
     */
    private Document parseXmlDocument(File file) {
        assert file != null;

        DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();

        try {
            DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
            Document document = documentBuilder.parse(file);

            return document;
        } catch (Exception e) {
            throw new RuntimeException("Error parsing XML file: " + file.getAbsoluteFile(), e);
        }
    }

    /**
     * Writes an XML document to a file on the file system
     * 
     * @param document the XML Document object
     * @param file the destination File object
     */
    private void writeXmlDocument(Document document, File file) {
        assert document != null;
        assert file != null;

        DOMSource domSource = new DOMSource(document);
        StreamResult result = new StreamResult(file);
        TransformerFactory tf = TransformerFactory.newInstance();

        try {
            Transformer transformer = tf.newTransformer();
            transformer.transform(domSource, result);

            logger.info("XML Document written to filepath: " + file.getAbsolutePath());
        } catch (Exception e) {
            throw new RuntimeException("Error writing XML file: " + file.getAbsoluteFile(), e);
        }
    }
}

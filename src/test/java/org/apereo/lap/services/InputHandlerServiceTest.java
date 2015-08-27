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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.apache.commons.configuration.HierarchicalConfiguration;
import org.apache.commons.configuration.XMLConfiguration;
import org.apereo.lap.model.PipelineConfig;
import org.apereo.lap.services.configuration.ConfigurationService;
import org.apereo.lap.services.input.BaseInputHandlerService;
import org.apereo.lap.services.storage.StorageService;
import org.apereo.lap.test.AbstractUnitTest;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles("test")
public class InputHandlerServiceTest extends AbstractUnitTest {
    private static final Logger logger = LoggerFactory.getLogger(InputHandlerServiceTest.class);

    @Autowired
    ConfigurationService configuration;
    @javax.annotation.Resource
    ResourceLoader resourceLoader;
    @Autowired
    StorageService storage;

    public static final String SLASH = System.getProperty("file.separator");

    @Test
    public void testFindHandlers() {
        assertEquals(4, BaseInputHandlerService.Type.values().length);
    }

    @Rule
    public ExpectedException exception = ExpectedException.none();

    @SuppressWarnings("unchecked")
    @Test
    public void testLoadInputType() {
        assertNotNull(configuration);
        assertNotNull(storage);
        logger.info("Successful Test in intializing the configuration and storage");

        try {
            /* Testing using XMLConfiguration config */
            Resource pipelineSample = resourceLoader.getResource("classpath:pipelines" + SLASH + "sample.xml");
            XMLConfiguration xmlcfg = null;
            xmlcfg = new XMLConfiguration(pipelineSample.getFile());

            List<HierarchicalConfiguration> inputFields = xmlcfg.configurationsAt("inputs.fields.field");
            for (HierarchicalConfiguration field : inputFields) {

                assertNotNull(BaseInputHandlerService.getInputHandler("csv", field, configuration, storage));
                logger.info(
                        "Test Successful in loading Inputhandler of field:" + field + " of type 'csv' from sample.xml");
            }

            /* Testing using pipeline config */
            PipelineConfig pipelineConfig = configuration.getPipelineConfig("sample");
            assertNotNull(pipelineConfig);
            logger.info("Test Successful in loading 'sample' pipeline using configuration object");

            List<BaseInputHandlerService> inputHandlers = pipelineConfig.getInputHandlers();
            assertNotNull(inputHandlers);
            assertTrue(inputHandlers.size() > 0);
            logger.info("Test Successful in loading 'sample' input handlers using pipeline configuration object");

            BaseInputHandlerService inputHandler = inputHandlers.get(0);
            assertNotNull(inputHandler.getLoadedInputCollections());
            assertNotNull(inputHandler.getLoadedInputTypes());
            assertNotNull(inputHandler.getType());
            logger.info("Test Successful in fetching input types and collections from input handler");

        } catch (Exception e) {
            logger.error("Test Failed in loading 'csv' handler from sample.xml");
            e.printStackTrace();
        }

    }
}

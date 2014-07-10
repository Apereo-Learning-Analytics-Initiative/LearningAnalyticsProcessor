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

import org.apereo.lap.model.Output;
import org.apereo.lap.services.output.CSVOutputHandler;
import org.apereo.lap.services.output.OutputHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.annotation.Resource;

/**
 * Handles the the data outputs from the pipeline (including the generation of output formats)
 * 
 * @author Aaron Zeckoski (azeckoski @ unicon.net) (azeckoski @ vt.edu)
 */
@Component
public class OutputHandlerService {

    private static final Logger logger = LoggerFactory.getLogger(OutputHandlerService.class);

    @Resource
    ConfigurationService configuration;

    @Resource
    StorageService storage;

    @PostConstruct
    public void init() {
        logger.info("INIT");
    }

    @PreDestroy
    public void destroy() {
        logger.info("DESTROY");
    }

    /**
     * Process the output for the given Output
     * @param output defines the requested output type
     * @return the result of the output processing
     * @throws java.lang.IllegalArgumentException is the inputs are bad
     * @throws java.lang.RuntimeException if the output handler fails
     */
    public OutputHandler.OutputResult doOutput(Output output) {
        assert output != null;
        OutputHandler.OutputResult result;
        if (Output.OutputType.CSV == output.type) {
            CSVOutputHandler csvOutputHandler = new CSVOutputHandler(configuration, storage);
            result = csvOutputHandler.writeOutput(output);
        } else {
            throw new IllegalArgumentException("No handler for output type: "+output.type);
        }
        return result;
    }

}

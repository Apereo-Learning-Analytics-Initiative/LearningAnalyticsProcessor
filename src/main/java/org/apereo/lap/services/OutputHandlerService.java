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
import org.apereo.lap.services.output.OutputHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.List;

/**
 * Handles the the data outputs from the pipeline (including the generation of output formats)
 * 
 * @author Aaron Zeckoski (azeckoski @ unicon.net) (azeckoski @ vt.edu)
 */
@Component
public class OutputHandlerService {

    private static final Logger logger = LoggerFactory.getLogger(OutputHandlerService.class);

    @Autowired
    List<OutputHandler> outputHandlers;

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
     * @throws java.lang.IllegalStateException is the result is invalid (null)
     * @throws java.lang.RuntimeException if the output handler fails
     */
    public OutputHandler.OutputResult doOutput(Output output) {
        assert output != null;
        OutputHandler.OutputResult result = null;
        boolean found = false;
        for (OutputHandler outputHandler : outputHandlers) {
            // NOTE: more than one output handler is allowed per type but only the last ones results will be passed back
            if (output.type == outputHandler.getHandledType()) {
                try {
                    result = outputHandler.writeOutput(output);
                    found = true;
                } catch (Exception e) {
                    throw new RuntimeException("Handler failed during processing: "+output.type+" :"+e, e);
                }
            }
        }
        if (!found) {
            throw new IllegalArgumentException("No handler for output type: "+output.type);
        } else if (result == null) {
            throw new IllegalStateException("Handler returned null result: "+output.type);
        }
        return result;
    }

}

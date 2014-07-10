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

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
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

    /**
     * Defines the valid types of output the system can handle
     */
    public static enum OutputType {
        CSV, STORAGE;
        public static OutputType fromString(String str) {
            if (StringUtils.equalsIgnoreCase(str, CSV.name())) {
                return CSV;
            } else if (StringUtils.equalsIgnoreCase(str, STORAGE.name())) {
                return STORAGE;
            } else {
                throw new IllegalArgumentException("output type ("+str+") does not match the valid types: "+ ArrayUtils.toString(OutputType.values()));
            }
        }
    }

    @Resource
    ConfigurationService configuration;

    @PostConstruct
    public void init() {
        logger.info("INIT");
    }

    @PreDestroy
    public void destroy() {
        logger.info("DESTROY");
    }

    public void process() {
        logger.info("PROCESS");
        // TODO execute the processor
    }

}

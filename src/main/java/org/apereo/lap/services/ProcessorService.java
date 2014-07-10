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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import weka.classifiers.Classifier;
import weka.classifiers.pmml.consumer.PMMLClassifier;
import weka.core.pmml.PMMLFactory;
import weka.core.pmml.PMMLModel;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.annotation.Resource;
import java.io.InputStream;

/**
 * @deprecated This class will be deleted
 * 
 * @author Aaron Zeckoski (azeckoski @ unicon.net) (azeckoski @ vt.edu)
 */
@Component
public class ProcessorService {

    private static final Logger logger = LoggerFactory.getLogger(ProcessorService.class);

    @Resource
    ConfigurationService configuration;

    Classifier classifier;

    @PostConstruct
    public void init() {
        logger.info("INIT");
    }

    @PreDestroy
    public void destroy() {
        logger.info("DESTROY");
    }

    public void loadPMML() {
        // init weka PMML
        try {
            InputStream pmml = ProcessorService.class.getClassLoader().getResourceAsStream("pmml/oaai.marist.pmml.xml");
            PMMLModel model = PMMLFactory.getPMMLModel(pmml);
            logger.info("Loaded PMML: "+model);
            if (model instanceof PMMLClassifier) {
                classifier = (PMMLClassifier) model;

                /* Since PMMLClassifier is a subclass of weka.classifiers.Classifier,
                 * you can use it just like any other Weka Classifier. The only
                 * exception is that calling buildClassifier() will raise an
                 * Exception because PMML models are pre-built.
                 */
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to load pmml file and init the classifier: "+e, e);
        }
    }

}

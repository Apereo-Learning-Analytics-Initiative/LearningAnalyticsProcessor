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
package org.apereo.lap.controllers;

import org.apereo.lap.services.ConfigurationService;
import org.apereo.lap.services.ProcessingManagerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.annotation.Resource;

/**
 * Sample controller for going to the home page with a message
 */
@Controller
public class HomeController {

    private static final Logger logger = LoggerFactory.getLogger(HomeController.class);

    @Resource
    ConfigurationService configuration;

    @Resource
    ProcessingManagerService processingManagerService;

    /**
     * Selects the home page and populates the model with a message
     */
    @RequestMapping(value = "/", method = RequestMethod.GET)
    public String home(Model model) {
        model.addAttribute("dev", "AZ"); // for testing
        model.addAttribute("processors", processingManagerService.getPipelineProcessors());
        model.addAttribute("pipelines", processingManagerService.getPipelineConfigs().values());
        model.addAttribute("outputDir", configuration.getOutputDirectory().getAbsolutePath());
        model.addAttribute("inputDir", configuration.getInputDirectory().getAbsolutePath());
        model.addAttribute("pipelinesDir", configuration.getPipelinesDirectory().getAbsolutePath());
        model.addAttribute("temporaryDB", configuration.getConfig().getString("db.url"));
        model.addAttribute("persistentDB", configuration.getConfig().getString("edb.url"));
        return "home";
    }

}

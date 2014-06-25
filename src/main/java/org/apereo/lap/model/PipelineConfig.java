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
package org.apereo.lap.model;

/**
 * This is an object that represents all configuration settings for a specific pipeline
 *
 * Each pipeline will be defined by a set of metadata which includes:
 * - name
 * - description (and recommendations for running the model)
 * - stat indicators (accuracy, confidence interval, etc.)
 * - required input fields
 * - processors (kettle ktr and kjb files, pmml files, etc.)
 * - output result definition
 * 
 * @author Aaron Zeckoski (azeckoski @ unicon.net) (azeckoski @ vt.edu)
 */
public class PipelineConfig {

    /**
     * the type of pipeline (e.g. marist_student_risk) this is the config for
     */
    public String pipelineType;

    // TODO inputs
    // TODO pipeline
    // TODO outputs

}

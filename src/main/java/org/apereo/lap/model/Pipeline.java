/*******************************************************************************
 * Copyright (c) 2015 Unicon (R) Licensed under the
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
 *******************************************************************************/
package org.apereo.lap.model;

/**
 * This is an object that represents a pipeline which is being processed.
 * It will serve as the holder for data while the pipeline is being processed
 * and also will keep track of the metadata related to each executing pipeline.
 * 
 * @author Aaron Zeckoski (azeckoski @ unicon.net) (azeckoski @ vt.edu)
 */
public class Pipeline {

    /**
     * unique ID for this instance of the pipeline (should be unique for each run of the pipeline)
     */
    public String pipelineId;
    /**
     * the type of pipeline (e.g. marist_student_risk)
     */
    public String pipelineType;

    /**
     * The UTC timecode at which we initialized this pipeline
     */
    public long timeInit;
    /**
     * The UTC timecode for the start of data loading
     */
    public long timeInput;
    /**
     * The UTC timecode for the start of pipeline processing
     */
    public long timeStart;
    /**
     * The UTC timecode for the end of pipeline processing
     */
    public long timeEnd;
    /**
     * The UTC timecode for the start of output processing
     */
    public long timeOutput;
    /**
     * The UTC timecode for the end of pipeline processing (after notifications)
     */
    public long timeComplete;

    /**
     * The configuration settings for this pipeline
     */
    PipelineConfig config;

    // TODO tracking fields for processing

}

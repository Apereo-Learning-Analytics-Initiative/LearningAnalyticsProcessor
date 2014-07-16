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

import org.apereo.lap.model.PipelineConfig;
import org.apereo.lap.model.Processor;

import java.util.ArrayList;
import java.util.Date;

/**
 * Handles the pipeline processing for a specific type of processor (e.g. Kettle)
 *
 * @author Aaron Zeckoski (azeckoski @ unicon.net) (azeckoski @ vt.edu)
 */
public interface PipelineProcessor {

    /**
     * @return the name of the processor type handled by this processor
     */
    Processor.ProcessorType getProcessorType();

    /**
     * Do the actual processing for this processor (and populate the results data and temp store if needed)
     *
     * If there is a failure and the pipeline should be stopped then throw a RuntimeException
     *
     * @param pipelineConfig the pipeline config
     * @param processorConfig the processor config (within the pipeline config)
     * @return the results of the processor run
     * @throws java.lang.RuntimeException if the processor fails
     */
    ProcessorResult process(PipelineConfig pipelineConfig, Processor processorConfig);

    public static class ProcessorResult {
        public Processor.ProcessorType type;
        public int outputCount;
        public long totalTimeMS;
        public long startTimeMS;
        public long endTimeMS;
        public ArrayList<String> failures;

        public ProcessorResult(Processor.ProcessorType processorType) {
            this.type = processorType;
            startTimeMS = System.currentTimeMillis();
        }

        /**
         * @param outputCount the number of items output (e.g. rows written to DB)
         * @param failures List of String representing all failures that occurred (1 entry per failure)
         */
        public void done(int outputCount, ArrayList<String> failures) {
            this.outputCount = outputCount;
            if (failures == null) {
                this.failures = new ArrayList<>(0);
            } else {
                this.failures = failures;
            }
            this.endTimeMS = System.currentTimeMillis();
            this.totalTimeMS = this.endTimeMS - this.startTimeMS;
        }

        @Override
        public String toString() {
            return "ProcessorResult{" +
                    "type=" + type +
                    ", outputCount=" + outputCount +
                    ", failures=" + failures.size() +
                    ", runSecs=" + String.format("%.2f", totalTimeMS/1000f) +
                    ", started=" + new Date(startTimeMS) +
                    '}';
        }
    }

}

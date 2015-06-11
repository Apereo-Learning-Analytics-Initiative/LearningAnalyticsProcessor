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

import java.io.IOException;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.apereo.lap.dao.ConfigurationRepository;
import org.apereo.lap.dao.model.Configuration;
import org.apereo.lap.model.Output;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.stereotype.Component;

@Component
public class ThresholdTriggerService {

	static final Logger logger = LoggerFactory.getLogger(ProcessingManagerService.class);
	
	@Resource
	StorageService storage;
	
	@Resource
	SSPService sspService;
	
	@Resource
    ConfigurationRepository configurationRepository;

	public boolean triggerSSP(List<Output> outputs) {
		
		List<Configuration> configurations = configurationRepository.findAll();
		
		// Check If Configuration Is Available And Active For SSP
		if(configurations.size() == 0 || !configurations.get(0).isSSPActive())
			return false;
		
		for (Output output : outputs) {
			Map<String, String> sourceToHeaderMap = output
					.makeSourceTargetMap();
			String selectSQL = output.makeTempDBSelectSQL();

			SqlRowSet rowSet;
			try {
				rowSet = storage.getTempJdbcTemplate()
						.queryForRowSet(selectSQL);
			} catch (Exception e) {
				throw new RuntimeException(
						"Failure while trying to retrieve the output data set: "
								+ selectSQL);
			}

			while (rowSet.next()) {

				if (!rowSet.wasNull()) {
					String[] rowVals = new String[sourceToHeaderMap.size()];

					if (rowVals.length > 0)
					{
						String userId = rowSet.getString(1);
						
						if (rowVals.length > 2)
						{
							try
							{
								if(rowSet.getString(3) == "HIGH RISK")
								{
									try {
										sspService.createEarlyAlert(configurations.get(0).getSspBaseUrl(), userId);
									} catch (IOException ex) {
										logger.error(ex.getMessage());
									}
								}
							}
							catch(NumberFormatException ex) {
								logger.error(ex.getMessage());
							}
						}
					}
				}
			}

			return true;
		}

		return false;
	}

}

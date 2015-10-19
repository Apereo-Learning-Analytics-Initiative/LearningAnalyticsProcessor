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
package org.apereo.lap.services.storage;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.apache.commons.lang.StringUtils;
import org.apereo.lap.services.configuration.ConfigurationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Manages the various types of storage (temporary and persistent),
 * probably would be good to have a service for each type of storage (temp and persistent)
 * 
 * @author Aaron Zeckoski (azeckoski @ unicon.net) (azeckoski @ vt.edu)
 */
@Component
@Transactional
public class StorageService {

    private static final Logger logger = LoggerFactory.getLogger(StorageService.class);

    @Autowired ConfigurationService configuration;

    @Autowired JdbcTemplate tempJdbcTemplate;

    @PostConstruct
    public void init() {
        // Initialize the temp database connection
        logger.info("INIT");
    }

    @PreDestroy
    public void destroy() {
        logger.info("DESTROY");
    }

    /**
     * Clears the temp datastore tables for data reload
     */
    public void resetTempStore() {
        // doesn't really make sense to reset the persistent store
        this.tempJdbcTemplate.execute("TRUNCATE TABLE ACTIVITY");
        this.tempJdbcTemplate.execute("TRUNCATE TABLE GRADE");
        this.tempJdbcTemplate.execute("TRUNCATE TABLE ENROLLMENT");
        this.tempJdbcTemplate.execute("TRUNCATE TABLE COURSE");
        this.tempJdbcTemplate.execute("TRUNCATE TABLE PERSONAL");
    }

    /**
     * Check if a table exists and contains the given column
     * @param tableName the table name (should be all CAPS)
     * @param columnName the column name (should be all CAPS)
     * @return true if the table and column exist, false otherwise
     */
    public boolean checkTableAndColumnExist(String tableName, String columnName) {
        assert StringUtils.isNotBlank(tableName);
        assert StringUtils.isNotBlank(columnName);
        String query = "SELECT COUNT(*) as COUNT FROM information_schema.COLUMNS WHERE TABLE_NAME = ? AND COLUMN_NAME = ?";
        Integer count;
        count = this.getTempJdbcTemplate().queryForObject(query, Integer.class, tableName, columnName);
        return (count != null && count > 0);
    }

    public ConfigurationService getConfiguration() {
        return configuration;
    }

    public JdbcTemplate getTempJdbcTemplate() {
        return tempJdbcTemplate;
    }
}

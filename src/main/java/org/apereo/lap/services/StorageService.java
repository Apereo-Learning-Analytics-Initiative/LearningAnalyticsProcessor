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

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.annotation.Resource;
import javax.sql.DataSource;

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

    @Resource(name="tempDataSource")
    DataSource tempDataSource;
    @Resource(name="persistentDataSource")
    DataSource persistentDataSource;
    @Resource
    ConfigurationService configuration;

    JdbcTemplate tempJdbcTemplate;
    JdbcTemplate persistentJdbcTemplate;

    @PostConstruct
    public void init() {
        // Initialize the temp database connection
        tempJdbcTemplate = new JdbcTemplate(tempDataSource);

        // Initialize the persistent database connection
        persistentJdbcTemplate = new JdbcTemplate(persistentDataSource);

        logger.info("INIT");
    }

    @PreDestroy
    public void destroy() {
        tempJdbcTemplate = null;
        persistentJdbcTemplate = null;
        logger.info("DESTROY");
    }

    /**
     * Clears the temp datastore tables for data reload
     */
    public void resetTempStore() {
        // doesn't really make sense to reset the persistent store
        this.tempJdbcTemplate.execute("TRUNCATE ACTIVITY");
        this.tempJdbcTemplate.execute("TRUNCATE GRADE");
        this.tempJdbcTemplate.execute("TRUNCATE ENROLLMENT");
        this.tempJdbcTemplate.execute("TRUNCATE COURSE");
        this.tempJdbcTemplate.execute("TRUNCATE PERSONAL");
    }

    /**
     * Check if a table exists and contains the given column
     * @param tableName the table name (should be all CAPS)
     * @param columnName the column name (should be all CAPS)
     * @param tempDB if true, check the temp DB, if false, check the persistent DB
     * @return true if the table and column exist, false otherwise
     */
    public boolean checkTableAndColumnExist(String tableName, String columnName, boolean tempDB) {
        assert StringUtils.isNotBlank(tableName);
        assert StringUtils.isNotBlank(columnName);
        String query = "SELECT COUNT(*) as COUNT FROM information_schema.COLUMNS WHERE TABLE_NAME = ? AND COLUMN_NAME = ?";
        Integer count;
        if (tempDB) {
            count = this.getTempJdbcTemplate().queryForObject(query, Integer.class, tableName, columnName);
        } else {
            count = this.getPersistentJdbcTemplate().queryForObject(query, Integer.class, tableName, columnName);
        }
        return (count != null && count > 0);
    }

    /**
     * @param tempDB if true, use the temp DB, if false, use the persistent DB
     * @return the JdbcTemplate for the appropriate database
     */
    public JdbcTemplate getJdbcTemplate(boolean tempDB) {
        if (tempDB) {
            return tempJdbcTemplate;
        } else {
            return persistentJdbcTemplate;
        }
    }


    public ConfigurationService getConfiguration() {
        return configuration;
    }

    public DataSource getTempDataSource() {
        return tempDataSource;
    }

    public DataSource getPersistentDataSource() {
        return persistentDataSource;
    }

    public JdbcTemplate getTempJdbcTemplate() {
        return tempJdbcTemplate;
    }

    public JdbcTemplate getPersistentJdbcTemplate() {
        return persistentJdbcTemplate;
    }

}

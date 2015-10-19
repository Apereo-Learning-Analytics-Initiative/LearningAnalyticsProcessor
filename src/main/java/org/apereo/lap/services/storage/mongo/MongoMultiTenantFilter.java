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
package org.apereo.lap.services.storage.mongo;

import java.io.IOException;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.apereo.lap.exception.MissingTenantException;
import org.apereo.lap.services.TenantService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

/**
* @author jbrown
*/
@Profile("mongo-multitenant")
@Component
public class MongoMultiTenantFilter extends OncePerRequestFilter {
    private static final Logger logger = LoggerFactory.getLogger(MongoMultiTenantFilter.class);

    @Value("${lap.defaultDatabaseName:lap_default}")
    private String defaultDatabase;
    
    @Value("${lap.useDefaultDatabaseName:true}")
    private String useDefaultDatabaseName;
    
    @Autowired private TenantService tenantService;

    @Override
    public void doFilterInternal(HttpServletRequest req, HttpServletResponse res, FilterChain fc) throws ServletException, IOException {
        logger.debug("applying MongoMultiTenantFilter");
        logger.debug("allow defaultDatabase: "+useDefaultDatabaseName);
        
        // For now header trumps all sources        
        String tenant = req.getHeader("X-LAP-TENANT");
        
        if (StringUtils.isBlank(tenant)) {
          // Next try path
          String requestURI = req.getRequestURI();
          if (StringUtils.isBlank(tenant) && 
              StringUtils.isNotBlank(requestURI) && StringUtils.startsWith(requestURI, "/api/output/")) {
            tenant = StringUtils.substringBetween(requestURI, "/api/output/", "/");
          }
          
          if (StringUtils.isBlank(tenant)) {
            // Next try session
            tenant = tenantService.getTenant();
            
            // If still blank and a default db is allowed
            if (StringUtils.isBlank(tenant)) {
              if (Boolean.valueOf(useDefaultDatabaseName)) {
                logger.warn("No tenant available in request. Using default database.");
                tenant = defaultDatabase;
              }
              else {
                throw new MissingTenantException("No tenant available in request and default database disabled.");
              }
            }
          }          
        }
        
        tenantService.setTenant(tenant);
        logger.debug("Using tenant {}",tenantService.getTenant());
        
        fc.doFilter(req, res);
    }
}

package org.apereo.lap.services.storage.mongo;

import java.io.IOException;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.apereo.lap.exception.MissingTenantException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.WebUtils;

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

    @Override
    public void doFilterInternal(HttpServletRequest req, HttpServletResponse res, FilterChain fc) throws ServletException, IOException {
        logger.debug("applying MongoMultiTenantFilter");
        logger.debug("allow defaultDatabase: "+useDefaultDatabaseName);
        
        MultiTenantMongoDbFactory.clearDatabaseNameForCurrentThread();
        String tenant = req.getHeader("X-LAP-TENANT");
        Cookie tenantCookie = WebUtils.getCookie(req, "X-LAP-TENANT");
        
        if (StringUtils.isBlank(tenant) && tenantCookie == null) {
          // Don't know who the tenant is
          if (Boolean.valueOf(useDefaultDatabaseName)) {
            logger.warn("No tenant available in request. Using default database.");
            tenant = defaultDatabase;
          }
          else {
            throw new MissingTenantException("No tenant available in request and default database disabled.");
          }
        }
        else if (StringUtils.isBlank(tenant) && tenantCookie != null) {
          tenant = tenantCookie.getValue();
          logger.debug("Tenant value from cookie");
        }
        else if (StringUtils.isNotBlank(tenant) && tenantCookie == null) {
          tenantCookie = new Cookie("X-LAP-TENANT", tenant);
          tenantCookie.setPath("/");
          res.addCookie(tenantCookie);
          logger.debug("Tenant value from header");
        }
        else {
          // header and cookie
          String tenantValueFromCookie = tenantCookie.getValue();
          if (!tenant.equals(tenantValueFromCookie)) {
            tenantCookie = new Cookie("X-LAP-TENANT", tenant);
            tenantCookie.setPath("/");
            res.addCookie(tenantCookie);
          }
        }
        logger.debug("Tenant: "+tenant);
        MultiTenantMongoDbFactory.setDatabaseNameForCurrentThread(tenant);
        fc.doFilter(req, res);
    }
}

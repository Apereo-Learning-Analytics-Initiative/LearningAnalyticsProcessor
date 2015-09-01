package org.apereo.lap.services.storage.mongo;

import java.io.IOException;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

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
        String tenant = null;
        Cookie tenantCookie = WebUtils.getCookie(req, "X-LAP-TENANT");
        
        if (tenantCookie != null) {
          // if we have the cookie that is the tenant authority
          tenant = tenantCookie.getValue();
          logger.debug("Tenant value from cookie");
        }
        else {
          // if we don't have it, then it is either login or an api call
          // both of those cases should pass the tenant as a http header
          tenant = req.getHeader("X-LAP-TENANT");
          logger.debug("Tenant value from header");
          
          if (org.apache.commons.lang.StringUtils.isNotBlank(tenant)) {
            tenantCookie = new Cookie("X-LAP-TENANT", tenant);
            tenantCookie.setPath("/");
            res.addCookie(tenantCookie);
          }
          else {
            
            if (Boolean.valueOf(useDefaultDatabaseName)) {
              logger.warn("No tenant available in request. Using default database.");
              tenant = defaultDatabase;
            }
            else {
              throw new MissingTenantException("A default database was not set at start up.");
            }
          }
        }
        
        logger.debug("Tenant: "+tenant);
        MultiTenantMongoDbFactory.setDatabaseNameForCurrentThread(tenant);
        fc.doFilter(req, res);
    }
}

package org.apereo.lap.services.storage.mongo;

import java.io.IOException;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.lang3.StringUtils;
import org.apereo.lap.security.LapWebAuthenticationDetails;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.CookieGenerator;

/**
* @author jbrown
*/
@Profile("mongo-multitenant")
@Component
public class MongoMultiTenantFilter extends OncePerRequestFilter {
    private static final Logger logger = LoggerFactory.getLogger(MongoMultiTenantFilter.class);

    @Value("${lap.defaultDatabaseName:lap_default}")
    private String defaultDatabase;

    @Override
    public void doFilterInternal(HttpServletRequest req, HttpServletResponse res, FilterChain fc) throws ServletException, IOException {
        logger.debug("applying MongoMultiTenantFilter");

        String databaseName = defaultDatabase;
        Object details = null;
        String principal = null;

        MultiTenantMongoDbFactory.clearDatabaseNameForCurrentThread();

        //Spring Security Documentation warns against getting information from the session
        //However the securityContextHolder is null on redirect.
        HttpSession session =req.getSession(true);
        SecurityContext securityContext = (SecurityContext) session.getAttribute("SPRING_SECURITY_CONTEXT");
        Authentication authentication = securityContext.getAuthentication();
        logger.debug("securityContext: " + securityContext);
        logger.debug("authentication: " + authentication);
        if (authentication != null){
            details = authentication.getDetails();
            principal = authentication.getName();
            logger.debug("details: " + details);
            logger.debug("principal: " + principal);
        }else{
            throw new SecurityException("Unauthenticated access not authorized");
        }

        if(details != null && details instanceof LapWebAuthenticationDetails && ((LapWebAuthenticationDetails) details).getOauthConsumerKey() != null){
            databaseName = ((LapWebAuthenticationDetails) details).getOauthConsumerKey();
        }else if (principal != null && StringUtils.isNotBlank(principal)){
            databaseName = principal;
        }else{
            //TODO oauth 1
            throw new SecurityException("Authenticated access does not contain correct data (Principal and Oauth Consumer Key)");
        }

        if (databaseName != null || StringUtils.isNotBlank(databaseName)) {
            logger.info("setting database name: " + databaseName);
            MultiTenantMongoDbFactory.setDatabaseNameForCurrentThread(databaseName);
        }
        else {
            throw new IllegalStateException("Mongo database not set for thread");
        }
        fc.doFilter(req, res);
    }
}

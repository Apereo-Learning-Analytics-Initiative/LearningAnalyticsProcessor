package org.apereo.lap.security;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.web.authentication.WebAuthenticationDetails;

public class LapWebAuthenticationDetails extends WebAuthenticationDetails {

    private static final long serialVersionUID = 1L;
    final static Logger logger = LoggerFactory.getLogger(LapWebAuthenticationDetails.class);
    private final String oauthConsumerKey;

    public LapWebAuthenticationDetails(HttpServletRequest request) {
        super(request);
        oauthConsumerKey = request.getParameter("oauth_consumer_key");
        logger.debug("Oauth Consumer Key from login form: "+ oauthConsumerKey);
    }

    public String getOauthConsumerKey() {
        return oauthConsumerKey;
    }

    @Override
    public String toString() {
        return "CustomWebAuthenticationDetails [oauthConsumerKey=" + oauthConsumerKey + "]";
    }

}
package org.apereo.lap.services.storage.mongo.tests;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.io.IOException;
import java.util.Collection;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apereo.lap.security.LapWebAuthenticationDetails;
import org.apereo.lap.services.storage.mongo.MongoMultiTenantFilter;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.test.util.ReflectionTestUtils;


public class MongoMultiTenantFilterTest extends MongoTests{

    private static final String DEFAULT_DATABASE = "TEST_DB";
    
    @Mock 
    HttpServletRequest req;
    @Mock
    HttpServletResponse res;
    @Mock
    FilterChain fc;
    @InjectMocks
    MongoMultiTenantFilter mongoMultiTenantFilter;
    @Mock
    Authentication authentication;
    
    SecurityException securityEx;
    IllegalArgumentException illegalArgumentEx;
    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        ReflectionTestUtils.setField(mongoMultiTenantFilter, "defaultDatabase", DEFAULT_DATABASE);
    }
    @After
    public void validate() {
        Mockito.validateMockitoUsage();
    }

    @Test(expected = SecurityException.class)
    public void doInternalFilterWillThrowSecurityExceptionWhenAuthenticationIsNull() throws ServletException, IOException{
        
        UsernamePasswordAuthenticationToken testAuthentication = null;
        MockHttpSession mockSession = new MockHttpSession();
        MockHttpServletRequest mockRequest = new MockHttpServletRequest();
        MockSecurityContext mockSecurityContext = new MockSecurityContext(testAuthentication);
        mockSession.setAttribute(HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY, mockSecurityContext);
        mockRequest.setSession(mockSession);
        
        mongoMultiTenantFilter.doFilterInternal(mockRequest, res, fc);
        
    }

    @Test
    public void doInternalFilterWillNotThrowExceptionWhenAuthenticationIsNotNullAndHasName() throws ServletException, IOException{
        
        UsernamePasswordAuthenticationToken testAuthentication = new UsernamePasswordAuthenticationToken("principal", "credentials");
        MockHttpSession mockSession = new MockHttpSession();
        MockHttpServletRequest mockRequest = new MockHttpServletRequest();
        MockSecurityContext mockSecurityContext = new MockSecurityContext(testAuthentication);
        mockSession.setAttribute(HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY, mockSecurityContext);
        mockRequest.setSession(mockSession);

        try{
            mongoMultiTenantFilter.doFilterInternal(mockRequest, res, fc);
        }catch (SecurityException e){
            securityEx = e;
        }catch (IllegalArgumentException e){
            illegalArgumentEx = e;
        }

        assertEquals(null, securityEx);
        assertEquals(null, illegalArgumentEx);
        verify(fc, times(1)).doFilter(mockRequest, res);
    }


    @Test
    public void doInternalFilterWillThrowNotExceptionWhenAuthenticationIsNotNullAndHasValidLapWebAuthenticationDetails() throws ServletException, IOException{
        MockHttpServletRequest req = new MockHttpServletRequest();
        req.setParameter("oauth_consumer_key", "test_database");
        LapWebAuthenticationDetails details = new LapWebAuthenticationDetails(req);

        MockHttpServletRequest mockRequest = new MockHttpServletRequest();
        MockHttpSession mockSession = new MockHttpSession();
        MockSecurityContext mockSecurityContext = new MockSecurityContext(new MockAuthentication(null, details, null));
        mockSession.setAttribute(HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY, mockSecurityContext);
        mockRequest.setSession(mockSession);

        try{
            mongoMultiTenantFilter.doFilterInternal(mockRequest, res, fc);
        }catch (SecurityException e){
            securityEx = e;
        }catch (IllegalArgumentException e){
            illegalArgumentEx = e;
        }

        assertEquals(null, securityEx);
        assertEquals(null, illegalArgumentEx);
        verify(fc, times(1)).doFilter(mockRequest, res);
    }
    

    @Test(expected = SecurityException.class)
    public void doInternalFilterWillThrowSecurityExceptionWhenAuthenticationIsNotNullAndHasInvalidLapWebAuthenticationDetailsAndNameIsNull() throws ServletException, IOException{
        MockHttpServletRequest req = new MockHttpServletRequest();
        //no parameter oauth_consumer_key on mock request
        LapWebAuthenticationDetails details = new LapWebAuthenticationDetails(req);

        MockHttpServletRequest mockRequest = new MockHttpServletRequest();
        MockHttpSession mockSession = new MockHttpSession();
        MockSecurityContext mockSecurityContext = new MockSecurityContext(new MockAuthentication(null, details, null));
        mockSession.setAttribute(HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY, mockSecurityContext);
        mockRequest.setSession(mockSession);

       mongoMultiTenantFilter.doFilterInternal(mockRequest, res, fc);

    }

    public static class MockSecurityContext implements SecurityContext {

        private static final long serialVersionUID = -1386535243513362694L;

        private Authentication authentication;
        
        public MockSecurityContext(Authentication authentication) {
            this.authentication = authentication;
        }
        @Override
        public Authentication getAuthentication() {
            return this.authentication;
        }

        @Override
        public void setAuthentication(Authentication authentication) {
           this.authentication = authentication;
        }
        
    }

    public static class MockAuthentication implements Authentication {

        private static final long serialVersionUID = -1386535243513362693L;

        private Object principal;
        private Object details;
        private String name;

        public MockAuthentication(Object principal, Object details, String name) {
            super();
            this.principal = principal;
            this.details = details;
            this.name = name;
        }

        @Override
        public String getName() {
            // TODO Auto-generated method stub
            return name;
        }

        @Override
        public Collection<? extends GrantedAuthority> getAuthorities() {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public Object getCredentials() {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public Object getDetails() {
            return details;
        }

        @Override
        public Object getPrincipal() {
            // TODO Auto-generated method stub
            return principal;
        }

        @Override
        public boolean isAuthenticated() {
            // TODO Auto-generated method stub
            return false;
        }

        @Override
        public void setAuthenticated(boolean isAuthenticated) throws IllegalArgumentException {
            // TODO Auto-generated method stub
            
        }
    }
}

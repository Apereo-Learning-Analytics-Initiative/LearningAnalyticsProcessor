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
package org.apereo.lap.services.storage.mongo.tests;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.io.IOException;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;

import org.apereo.lap.exception.MissingTenantException;
import org.apereo.lap.services.storage.mongo.MongoMultiTenantFilter;
import org.apereo.lap.test.MongoUnitTests;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.util.ReflectionTestUtils;

public class MongoMultiTenantFilterTest extends MongoUnitTests{

    @Mock
    HttpServletResponse res;
    @Mock
    FilterChain fc;
    @InjectMocks
    MongoMultiTenantFilter mongoMultiTenantFilter;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        ReflectionTestUtils.setField(mongoMultiTenantFilter, "useDefaultDatabaseName", "true");
    }
    @After
    public void validate() {
        Mockito.validateMockitoUsage();
    }

    @Test
    public void doInternalFilterWillNotChangeResponseRequestWhenRequestWithExpectedCookie() throws ServletException, IOException{
        MockHttpServletRequest mockRequest = new MockHttpServletRequest();
        
        Cookie testCookie = new Cookie("X-LAP-TENANT", "test_tenant");
        testCookie.setPath("/");
        mockRequest.setCookies(testCookie);
        
        mongoMultiTenantFilter.doFilterInternal(mockRequest, res, fc);
        
        verify(fc, times(1)).doFilter(mockRequest, res);

    }

    @Test
    public void doInternalFilterWillAddCookieToResponseWhenRequestWithoutCookieWithExpecedTenantHeader() throws ServletException, IOException{
        
        MockHttpServletRequest mockRequest = new MockHttpServletRequest();
        mockRequest.addHeader("X-LAP-TENANT", "test_tenant");

        MockHttpServletResponse mockResponse = new MockHttpServletResponse();
        mongoMultiTenantFilter.doFilterInternal(mockRequest, mockResponse, fc);
        
        verify(fc, times(1)).doFilter(mockRequest, mockResponse);
        assertEquals(mockResponse.getCookie("X-LAP-TENANT").getValue(), "test_tenant");
    }

    @Test
    public void doInternalFilterWillUseDefaultDatabaseWhenRequestExpectedHeaderAndCookieAreEmptyStrings() throws ServletException, IOException{
        MockHttpServletRequest mockRequest = new MockHttpServletRequest();
        mockRequest.addHeader("X-LAP-TENANT", "");
        mockRequest.setCookies(new Cookie("X-LAP-TENANT", ""));
        MockHttpServletResponse mockResponse = new MockHttpServletResponse();
        mongoMultiTenantFilter.doFilterInternal(mockRequest, mockResponse, fc);
        
        verify(fc, times(1)).doFilter(mockRequest, mockResponse);
        assertEquals(mockResponse.getCookie("X-LAP-TENANT"), null);
    }

    @Test
    public void doInternalFilterWillUseDefaultDatabaseWhenRequestExpectedHeaderAndCookieAreMissing() throws ServletException, IOException{
        MockHttpServletRequest mockRequest = new MockHttpServletRequest();
        MockHttpServletResponse mockResponse = new MockHttpServletResponse();
        mongoMultiTenantFilter.doFilterInternal(mockRequest, mockResponse, fc);
        
        verify(fc, times(1)).doFilter(mockRequest, mockResponse);
        assertEquals(mockResponse.getCookie("X-LAP-TENANT"), null);
    }

    @Test(expected = MissingTenantException.class)
    public void doInternalFilterWillThrowExceptionWhenRequestExpectedHeaderAndCookieAreMissingAndDefualtDatabaseNotSet() throws ServletException, IOException{
        ReflectionTestUtils.setField(mongoMultiTenantFilter, "useDefaultDatabaseName", null);
        MockHttpServletRequest mockRequest = new MockHttpServletRequest();
        MockHttpServletResponse mockResponse = new MockHttpServletResponse();
        mongoMultiTenantFilter.doFilterInternal(mockRequest, mockResponse, fc);
        
        verify(fc, times(1)).doFilter(mockRequest, mockResponse);
        assertEquals(mockResponse.getCookie("X-LAP-TENANT").getValue(), "");
    }
}

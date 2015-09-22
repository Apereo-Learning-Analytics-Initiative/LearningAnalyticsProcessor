package org.apereo.lap.services;

import static org.junit.Assert.assertEquals;
import static org.mockito.BDDMockito.given;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;

import org.apereo.lap.exception.MissingPipelineException;
import org.apereo.lap.exception.MissingTenantException;
import org.apereo.lap.model.ExceptionResponseDto;
import org.apereo.lap.test.AbstractUnitTest;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

public class LapControllerAdviceTest extends AbstractUnitTest{
    private static final Logger logger = LoggerFactory.getLogger(LapControllerAdviceTest.class);
    @Mock
    private HttpServletRequest httpServletRequest;
    @InjectMocks
    private LapControllerAdvice lapControllerAdvice;

    @Before
    public void init() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void handleAppExceptionWillReturnResponseWith500WhenGivenNotACustomException() throws IOException {
        given(httpServletRequest.getRequestURL()).willReturn(new StringBuffer("/abc123"));
        ResponseEntity jsonResponseEntity = (ResponseEntity)lapControllerAdvice.handleAppException(httpServletRequest, new RuntimeException("Something went wrong"));

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, jsonResponseEntity.getStatusCode());
        ExceptionResponseDto responseBody = (ExceptionResponseDto)jsonResponseEntity.getBody();
        assertEquals("No message available", responseBody.getMessage());
        assertEquals(new Integer(500), responseBody.getStatus());
        assertEquals("/abc123", responseBody.getPath());
    }

    @Test
    public void handleAppExceptionWillReturnResponseWith404WhenGivenAMissingTenantException() throws IOException {
        given(httpServletRequest.getRequestURL()).willReturn(new StringBuffer("/tenant"));
        ResponseEntity jsonResponseEntity = (ResponseEntity)lapControllerAdvice.handleAppException(httpServletRequest, new MissingTenantException("Missing tenant test"));

        assertEquals(HttpStatus.NOT_FOUND, jsonResponseEntity.getStatusCode());
        ExceptionResponseDto responseBody = (ExceptionResponseDto)jsonResponseEntity.getBody();
        assertEquals("Missing tenant test", responseBody.getMessage());
        assertEquals(new Integer(404), responseBody.getStatus());
        assertEquals("/tenant", responseBody.getPath());
    }
    
    @Test
    public void handleAppExceptionWillReturnResponseWith404WhenGivenAMissingPipelineException() throws IOException {
        given(httpServletRequest.getRequestURL()).willReturn(new StringBuffer("/pipeline"));
        ResponseEntity jsonResponseEntity = (ResponseEntity)lapControllerAdvice.handleAppException(httpServletRequest, new MissingPipelineException("Missing pipeline test"));
        logger.warn(jsonResponseEntity.toString());
        assertEquals(HttpStatus.NOT_FOUND, jsonResponseEntity.getStatusCode());
        ExceptionResponseDto responseBody = (ExceptionResponseDto)jsonResponseEntity.getBody();
        assertEquals("Missing pipeline test", responseBody.getMessage());
        assertEquals(new Integer(404), responseBody.getStatus());
        assertEquals("/pipeline", responseBody.getPath());
    }
}
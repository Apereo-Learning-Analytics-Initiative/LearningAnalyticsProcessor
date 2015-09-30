package org.apereo.lap.controllers;

import java.io.IOException;
import java.util.Calendar;

import javax.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apereo.lap.exception.MissingPipelineException;
import org.apereo.lap.exception.MissingTenantException;
import org.apereo.lap.model.ExceptionResponseDto;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;

@ControllerAdvice
public class LapControllerAdvice {
    private static final Logger logger = LoggerFactory.getLogger(LapControllerAdvice.class);
   
    public LapControllerAdvice(){
        super();
    }
    
    @ExceptionHandler({Exception.class})
    public Object handleAppException(HttpServletRequest request, Exception ex) throws IOException {
        if (ex instanceof MissingPipelineException || ex instanceof MissingTenantException) {
            return handleExceptionWithMessageAndStatusCode(request, ex, 404);
        } 
        // Everything else gets a generic 500 exception
        return handleGenericExceptionWithoutMessage(request, ex);
    }

    private Object handleExceptionWithMessageAndStatusCode(HttpServletRequest request, Exception ex, int statusCode) {
        logger.error("Exception", ex);
        
        ExceptionResponseDto response = new ExceptionResponseDto();
        response.setError(ex.getClass().toString());
        response.setTimestamp(Calendar.getInstance().getTimeInMillis());
        response.setPath(request.getRequestURL().toString());
        response.setStatus(statusCode);
        response.setMessage(ex.getMessage());

        return new ResponseEntity<>(response, HttpStatus.valueOf(statusCode));

    }
    private Object handleGenericExceptionWithoutMessage(HttpServletRequest request, Exception ex) {
        logger.error("Exception", ex);
        
        ExceptionResponseDto response = new ExceptionResponseDto();
        response.setError(ex.getClass().toString());
        response.setTimestamp(Calendar.getInstance().getTimeInMillis());
        response.setPath(request.getRequestURL().toString());
        response.setStatus(500);
        response.setMessage("No message available");
        
        return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}

package org.apereo.lap.exception;

import javax.servlet.ServletException;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value=HttpStatus.NOT_FOUND, reason="Pipeline Not Found") //404
public class MissingPipelineException extends ServletException {
        private static final long serialVersionUID = 156338621418684633L;
        private String message;
        
        public MissingPipelineException(){
            super();
        }
        
        public MissingPipelineException(String message){
            this.message = message;
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }
}


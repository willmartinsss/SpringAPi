package jala.university.ds3.Exceptions;

import jakarta.servlet.http.HttpServletRequest;
import jala.university.ds3.utils.ResponseBuilder;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.NoHandlerFoundException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private final ResponseBuilder responseBuilder;

    public GlobalExceptionHandler(@Qualifier("appResponseBuilder") ResponseBuilder responseBuilder) {
        this.responseBuilder = responseBuilder;
    }

    @ExceptionHandler(GeneralExceptions.class)
    public ResponseEntity<?> handleGeneralExceptions(GeneralExceptions e) {
        return responseBuilder.buildResponse(e.getMessage(), e.getStatus());
    }

    @ExceptionHandler({NoHandlerFoundException.class, NoResourceFoundException.class})
    public ResponseEntity<?> handleNotFoundException(Exception ex, HttpServletRequest request) {
        return responseBuilder.build("Endpoint not found!", HttpStatus.NOT_FOUND, request.getRequestURI());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<?> handleException(Exception ex, HttpServletRequest request) {
        return responseBuilder.build("Something went wrong!", HttpStatus.INTERNAL_SERVER_ERROR, request.getRequestURI());
    }
}
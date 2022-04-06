package ee.fujitsu.movieapi.rest.controller;

import ee.fujitsu.movieapi.rest.api.response.GeneralApiResponse;
import ee.fujitsu.movieapi.rest.api.response.ResponseCode;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.NoHandlerFoundException;

@ControllerAdvice
@RequestMapping(produces = "application/json")
@ResponseBody
public class RestControllerAdvice {
    @ExceptionHandler(NoHandlerFoundException.class)
    public ResponseEntity<?> unhandledPath(final NoHandlerFoundException e) {
        GeneralApiResponse response = new GeneralApiResponse(ResponseCode.INVALID_REQUEST, e.getMessage());
        return new ResponseEntity(response, HttpStatus.NOT_FOUND);
    }

}

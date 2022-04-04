package ee.fujitsu.movieapi.rest.api.exception.general;

import ee.fujitsu.movieapi.rest.api.response.GeneralApiResponse;
import ee.fujitsu.movieapi.rest.api.response.ResponseCode;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

public class GeneralHttpException extends Exception {

    private final HttpStatus httpStatus;
    private final ResponseCode responseCode;
    private final String message;

    public GeneralHttpException(ResponseCode errorCode, String message) {
        this(HttpStatus.OK, errorCode, message);
    }

    public GeneralHttpException(HttpStatus httpStatus, ResponseCode errorCode, String message) {
        super(message);
        this.httpStatus = httpStatus;
        this.responseCode = errorCode;
        this.message = message;
    }

    public ResponseEntity createResponse() {
        GeneralApiResponse response = new GeneralApiResponse();
        response.setResponseCode(responseCode);
        response.setMessage(message);
        return ResponseEntity.status(httpStatus).body(response);
    }
}
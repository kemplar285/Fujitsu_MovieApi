package ee.fujitsu.movieapi.rest.api.exception.movie;

public class MovieValidationException extends Exception{

    public MovieValidationException(String message) {
        super(message);
    }
}

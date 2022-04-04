package ee.fujitsu.movieapi.rest.api.exception.movie;

public class MovieIdNotUniqueException extends Exception{
    public MovieIdNotUniqueException() {
        super("Movie ID should be unique!");
    }
}

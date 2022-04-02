package ee.fujitsu.movieapi.exception;

public class MovieIdNotUniqueException extends Exception{
    public MovieIdNotUniqueException() {
        super("Movie ID should be unique!");
    }
}

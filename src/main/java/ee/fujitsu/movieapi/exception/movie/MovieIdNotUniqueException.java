package ee.fujitsu.movieapi.exception.movie;

public class MovieIdNotUniqueException extends Exception{
    public MovieIdNotUniqueException() {
        super("Movie ID should be unique!");
    }
}

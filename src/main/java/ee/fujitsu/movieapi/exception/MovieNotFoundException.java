package ee.fujitsu.movieapi.exception;

public class MovieNotFoundException extends Exception{
    public MovieNotFoundException(){
        super("Movie not found!");
    }
}

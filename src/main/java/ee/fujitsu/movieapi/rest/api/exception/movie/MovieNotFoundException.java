package ee.fujitsu.movieapi.rest.api.exception.movie;

public class MovieNotFoundException extends Exception{
    public MovieNotFoundException(){
        super("Movie not found!");
    }
}

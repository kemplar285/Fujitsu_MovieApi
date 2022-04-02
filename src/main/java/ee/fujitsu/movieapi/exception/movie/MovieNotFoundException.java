package ee.fujitsu.movieapi.exception.movie;

public class MovieNotFoundException extends Exception{
    public MovieNotFoundException(){
        super("Movie not found!");
    }
}

package ee.fujitsu.movieapi.rest.api.exception.general;

public class NotFoundException extends Exception{
    public NotFoundException(){
        super("Movie not found!");
    }
}

package ee.fujitsu.movieapi.rest.api.exception.general;

public class NotFoundException extends Exception{
    public NotFoundException(){
        super("Not found!");
    }
    public NotFoundException(String message){
        super(message);
    }
}

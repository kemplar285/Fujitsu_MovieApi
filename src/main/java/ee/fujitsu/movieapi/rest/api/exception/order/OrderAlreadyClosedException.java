package ee.fujitsu.movieapi.rest.api.exception.order;

public class OrderAlreadyClosedException extends Exception{
    public OrderAlreadyClosedException(){
        super("This order is already closed.");
    }
}

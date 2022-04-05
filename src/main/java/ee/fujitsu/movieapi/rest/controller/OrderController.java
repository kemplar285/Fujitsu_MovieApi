package ee.fujitsu.movieapi.rest.controller;

import ee.fujitsu.movieapi.db.model.movie.Movie;
import ee.fujitsu.movieapi.db.model.order.Order;
import ee.fujitsu.movieapi.db.model.order.OrderItem;
import ee.fujitsu.movieapi.db.model.order.OrderStatus;
import ee.fujitsu.movieapi.db.repository.MovieRepository;
import ee.fujitsu.movieapi.db.repository.OrderRepository;
import ee.fujitsu.movieapi.rest.api.exception.general.NotFoundException;
import ee.fujitsu.movieapi.rest.api.response.GeneralApiResponse;
import ee.fujitsu.movieapi.rest.api.response.OrderApiResponse;
import ee.fujitsu.movieapi.rest.api.response.ResponseCode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping(path = "/orders")
public class OrderController {
    private OrderRepository orderRepository;
    private MovieRepository movieRepository;

    @Autowired
    public OrderController(OrderRepository orderRepository, MovieRepository movieRepository) {
        this.orderRepository = orderRepository;
        this.movieRepository = movieRepository;
    }

    @GetMapping
    public ResponseEntity<?> findAll() {
        List<Order> orders = orderRepository.findAll();
        OrderApiResponse response = new OrderApiResponse();
        response.setData(orders);
        response.setResponseCode(ResponseCode.OK);
        if(orders.size() == 0){
            response.setMessage("Orders not found.");
        }
        return new ResponseEntity<>(response, HttpStatus.OK);

    }

    @RequestMapping(value = "/new", method = RequestMethod.POST)
    public ResponseEntity<?> addOrder(@RequestBody OrderItem orderItem) {
        try {
            Movie movie = movieRepository.findById(orderItem.getMovieId());

            orderItem.setCurrentPricePerWeek(movie.getPrice());
            orderItem.setMovieReleaseDate(movie.getReleaseDate());
            orderItem.calculateTotalPrice();

            Order order = new Order();
            order.generateOrderId();
            order.addToOrderItems(orderItem);
            order.calculateTotalPrice();

            OrderApiResponse response = new OrderApiResponse();
            response.setResponseCode(ResponseCode.OK);
            response.setMessage("Order created.");
            response.setData(List.of(orderRepository.add(order)));
            return new ResponseEntity<>(response, HttpStatus.OK);

        } catch (IOException | NotFoundException e) {
            GeneralApiResponse response = new GeneralApiResponse();
            response.setResponseCode(ResponseCode.INVALID_REQUEST);
            response.setMessage(e.getMessage());
            return new ResponseEntity<>(response, HttpStatus.OK);
        }
    }

    @RequestMapping(value = "/extend/{orderId}", method = RequestMethod.POST)
    public ResponseEntity<?> addToOrder(@PathVariable String orderId, @RequestBody OrderItem orderItem) {
        try {
            Movie movie = movieRepository.findById(orderItem.getMovieId());
            orderItem.setCurrentPricePerWeek(movie.getPrice());
            orderItem.setMovieReleaseDate(movie.getReleaseDate());
            orderItem.calculateTotalPrice();

            Order order = orderRepository.findById(orderId);
            order.addToOrderItems(orderItem);
            order.calculateTotalPrice();

            OrderApiResponse response = new OrderApiResponse();
            response.setResponseCode(ResponseCode.OK);
            response.setMessage("Order extended.");
            response.setData(List.of(orderRepository.add(order)));
            return new ResponseEntity<>(response, HttpStatus.OK);

        } catch (IOException | NotFoundException e) {
            GeneralApiResponse response = new GeneralApiResponse();
            response.setResponseCode(ResponseCode.INVALID_REQUEST);
            response.setMessage(e.getMessage());
            return new ResponseEntity<>(response, HttpStatus.OK);
        }
    }

    @RequestMapping(value = "/order/{orderId}", method = RequestMethod.GET)
    public ResponseEntity<?> findById(@PathVariable String orderId) {
        try {
            Order order = orderRepository.findById(orderId);
            System.out.println(order);
            OrderApiResponse response = new OrderApiResponse();
            response.setResponseCode(ResponseCode.OK);
            response.setMessage("Order found.");
            response.setData(List.of(orderRepository.add(order)));
            return new ResponseEntity<>(response, HttpStatus.OK);

        } catch (IOException | NotFoundException e) {
            GeneralApiResponse response = new GeneralApiResponse();
            response.setResponseCode(ResponseCode.INVALID_REQUEST);
            response.setMessage(e.getMessage());
            return new ResponseEntity<>(response, HttpStatus.OK);
        }
    }
}

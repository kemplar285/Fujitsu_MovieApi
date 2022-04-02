package ee.fujitsu.movieapi.controller;

import ee.fujitsu.movieapi.model.movie.Movie;
import ee.fujitsu.movieapi.model.order.Order;
import ee.fujitsu.movieapi.repository.MovieRepository;
import ee.fujitsu.movieapi.repository.OrderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping(path = "/orders")
public class OrderController {
    private OrderRepository orderRepository;

    @Autowired
    public OrderController(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    @GetMapping
    public ResponseEntity<?> findAll() {
        List<Order> orders = orderRepository.findAll();
        if (orders.size() > 0) {
            return new ResponseEntity<>(orders, HttpStatus.OK);
        } else {
            return ResponseEntity.noContent().build();
        }
    }
}

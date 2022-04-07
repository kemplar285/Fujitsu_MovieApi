package ee.fujitsu.movieapi.rest.controller;

import ee.fujitsu.movieapi.db.model.movie.Movie;
import ee.fujitsu.movieapi.db.model.order.Order;
import ee.fujitsu.movieapi.db.model.order.OrderItem;
import ee.fujitsu.movieapi.db.model.order.OrderStatus;
import ee.fujitsu.movieapi.db.model.statistics.OrderStatistics;
import ee.fujitsu.movieapi.db.repository.MovieRepository;
import ee.fujitsu.movieapi.db.repository.OrderRepository;
import ee.fujitsu.movieapi.rest.api.exception.general.NotFoundException;
import ee.fujitsu.movieapi.rest.api.exception.order.OrderAlreadyClosedException;
import ee.fujitsu.movieapi.rest.api.response.GeneralApiResponse;
import ee.fujitsu.movieapi.rest.api.response.OrderApiResponse;
import ee.fujitsu.movieapi.rest.api.response.OrderStatisticsApiResponse;
import ee.fujitsu.movieapi.rest.api.response.ResponseCode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping(path = "/orders")
public class OrderController {
    private final OrderRepository orderRepository;
    private final MovieRepository movieRepository;

    @Autowired
    public OrderController(OrderRepository orderRepository, MovieRepository movieRepository) {
        this.orderRepository = orderRepository;
        this.movieRepository = movieRepository;
    }

    /**
     * Returns all orders
     *
     * @return Response entity with all orders and status
     */
    @RequestMapping(value = "", method = RequestMethod.GET, produces = "application/json")
    public ResponseEntity<?> findAll() {
        List<Order> orders = orderRepository.findAll();
        OrderApiResponse response = new OrderApiResponse();
        response.setData(orders);
        response.setResponseCode(ResponseCode.OK);
        if (orders.size() == 0) {
            response.setMessage("Orders not found.");
        }
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    /**
     * Shows the statistics data
     *
     * @return Response entity with statistics and status
     */
    @RequestMapping(value = "/stats", method = RequestMethod.GET, produces = "application/json")
    public ResponseEntity<?> findStatistics() {
        OrderStatistics statistics = orderRepository.getStatistics();
        OrderStatisticsApiResponse response = new OrderStatisticsApiResponse();
        response.setOrderStatistics(statistics);
        response.setResponseCode(ResponseCode.OK);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    /**
     * Creates a new order
     *
     * @param orderItem OrderItem Body. Data needed - movieId, rentDurationInWeeks
     * @return ResponseEntity with order data and status code
     */
    @RequestMapping(value = "/new", method = RequestMethod.POST, consumes = "application/json", produces = "application/json")
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
            orderRepository.add(order);

            OrderApiResponse response = new OrderApiResponse();
            response.setResponseCode(ResponseCode.OK);
            response.setMessage("Order created.");
            response.setData(List.of(order));
            return new ResponseEntity<>(response, HttpStatus.OK);

        } catch (IOException | NotFoundException e) {
            GeneralApiResponse response = new GeneralApiResponse(ResponseCode.INVALID_REQUEST, e.getMessage());
            return new ResponseEntity<>(response, HttpStatus.OK);
        }
    }

    /**
     * Adds new orderItem to an existing order.
     *
     * @param orderId   Id of existing order
     * @param orderItem New orderItem
     * @return ResponseEntity with order data and status code
     */
    @RequestMapping(value = "/extend/{orderId}", method = RequestMethod.POST, consumes = "application/json"
            , produces = "application/json")
    public ResponseEntity<?> addToOrder(@PathVariable String orderId, @RequestBody OrderItem orderItem) {
        try {
            Movie movie = movieRepository.findById(orderItem.getMovieId());
            orderItem.setCurrentPricePerWeek(movie.getPrice());
            orderItem.setMovieReleaseDate(movie.getReleaseDate());
            orderItem.calculateTotalPrice();

            Order order = orderRepository.findById(orderId);
            if (order.getOrderStatus().equals(OrderStatus.CLOSED)) {
                throw new OrderAlreadyClosedException();
            }
            order.addToOrderItems(orderItem);
            order.calculateTotalPrice();
            order.setTimestamp(LocalDateTime.now());
            orderRepository.update(order);


            OrderApiResponse response = new OrderApiResponse();
            response.setResponseCode(ResponseCode.OK);
            response.setMessage("Order extended.");
            response.setData(List.of(order));
            return new ResponseEntity<>(response, HttpStatus.OK);

        } catch (NotFoundException | NullPointerException | OrderAlreadyClosedException e) {
            GeneralApiResponse response = new GeneralApiResponse(ResponseCode.INVALID_REQUEST, e.getMessage());
            return new ResponseEntity<>(response, HttpStatus.OK);
        }
    }

    /**
     * Gets order by id
     *
     * @param orderId order id
     * @return ResponseEntity with order and status code
     */
    @RequestMapping(value = "/{orderId}", method = RequestMethod.GET, produces = "application/json")
    public ResponseEntity<?> findById(@PathVariable String orderId) {
        try {
            Order order = orderRepository.findById(orderId);
            OrderApiResponse response = new OrderApiResponse();
            response.setResponseCode(ResponseCode.OK);
            response.setMessage("Order found.");
            response.setData(List.of(order));
            return new ResponseEntity<>(response, HttpStatus.OK);

        } catch (NotFoundException e) {
            GeneralApiResponse response = new GeneralApiResponse(ResponseCode.OK, e.getMessage());
            return new ResponseEntity<>(response, HttpStatus.OK);
        }
    }

    /**
     * Closes the order. It then becomes immutable. After closing the order, its data is added to statistics
     *
     * @param orderId id of an order to close
     * @return Final order invoice.
     */

    @RequestMapping(value = "/checkout", method = RequestMethod.PATCH, produces = "application/json")
    public ResponseEntity<?> checkout(@RequestParam String orderId) {
        try {
            //Closing the order
            Order order = orderRepository.findById(orderId);
            if (order.getOrderStatus().equals(OrderStatus.CLOSED)) {
                throw new OrderAlreadyClosedException();
            }
            order.calculateTotalPrice();
            order.setOrderStatus(OrderStatus.CLOSED);
            order.setTimestamp(LocalDateTime.now());
            orderRepository.update(order);

            // Statistics
            OrderStatistics stats = orderRepository.getStatistics();
            for (OrderItem s : order.getOrderItemList()) {
                stats.addToOrderCount(s.getMovieId(), 1);
                stats.addToRentedFor(s.getMovieId(), s.getRentDurationInWeeks());
            }
            orderRepository.recordStatistics(stats);

            //Response
            OrderApiResponse response = new OrderApiResponse();
            response.setResponseCode(ResponseCode.OK);
            response.setMessage("Final order invoice.");
            response.setData(List.of(order));
            return new ResponseEntity<>(response, HttpStatus.OK);

        } catch (NotFoundException | OrderAlreadyClosedException e) {
            GeneralApiResponse response = new GeneralApiResponse(ResponseCode.INVALID_REQUEST, e.getMessage());
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (IOException e) {
            GeneralApiResponse response = new GeneralApiResponse(ResponseCode.SYSTEM_ERROR, e.getMessage());
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Deletes the order by its id.
     * At first I was planning to forbid deleting closed order, but now I'm not so sure.
     * @param id
     * @return
     */
    @RequestMapping(value = "/delete", method = RequestMethod.DELETE, produces = "application/json")
    public ResponseEntity<?> deleteOrder(@RequestParam String id) {
        try {
//            if (orderRepository.findById(id).getOrderStatus().equals(OrderStatus.CLOSED)) {
//                throw new OrderAlreadyClosedException();
//            }
            orderRepository.delete(id);

            GeneralApiResponse response = new GeneralApiResponse();
            response.setResponseCode(ResponseCode.OK);
            response.setMessage("Order deleted");
            return new ResponseEntity<>(response, HttpStatus.OK);

        } catch (NotFoundException | IOException e) {
            GeneralApiResponse response = new GeneralApiResponse(ResponseCode.INVALID_REQUEST, e.getMessage());
            return new ResponseEntity<>(response, HttpStatus.OK);
        }
    }


    /**
     * Deletes a movie record from statistics
     * @param movieId movie id
     */
    @RequestMapping(value = "/stats/clear", method = RequestMethod.DELETE, produces = "application/json")
    public ResponseEntity<?> deleteFromStats(@RequestParam String movieId) {
        try {

            orderRepository.deleteFromStats(movieId);
            GeneralApiResponse response = new GeneralApiResponse();
            response.setResponseCode(ResponseCode.OK);
            response.setMessage("Statistics cleared");
            return new ResponseEntity<>(response, HttpStatus.OK);

        } catch (IOException e) {
            GeneralApiResponse response = new GeneralApiResponse(ResponseCode.SYSTEM_ERROR, e.getMessage());
            return new ResponseEntity<>(response, HttpStatus.OK);
        }
    }

}

package ee.fujitsu.movieapi;

import ee.fujitsu.movieapi.db.model.movie.Movie;
import ee.fujitsu.movieapi.db.model.movie.MoviePriceClass;
import ee.fujitsu.movieapi.db.model.order.Order;
import ee.fujitsu.movieapi.db.model.order.OrderItem;
import ee.fujitsu.movieapi.db.model.order.OrderStatus;
import ee.fujitsu.movieapi.rest.api.response.GeneralApiResponse;
import ee.fujitsu.movieapi.rest.api.response.MovieApiResponse;
import ee.fujitsu.movieapi.rest.api.response.OrderApiResponse;
import ee.fujitsu.movieapi.rest.api.response.ResponseCode;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.*;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.http.HttpClient;
import java.time.LocalDate;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class OrderTests extends AbstractMovieApiTest{
    private static final Logger logger = LoggerFactory.getLogger(OrderTests.class);
    @LocalServerPort
    private int port;
    @Autowired
    private TestRestTemplate restTemplate;

    private final String[] getEndpoints = {"/orders", "/orders/1", "/orders/stats"};

    @Test
    void testSuccessfulResponseShouldContainOkStatus() throws Exception {
        for (String end : getEndpoints) {
            verifyResponse("http://localhost:" + port + end, ResponseCode.OK, false);
        }
    }

    @Test
    void testAddingAndDeletingWorksWithValidOrder(){
        OrderItem item = createOrderItem();
        Movie movie = getMockMovie();
        ResponseEntity<MovieApiResponse> addResponse = addMovie(movie);
        System.out.println(addResponse.getBody());
        ResponseEntity<OrderApiResponse> orderResponse = addValidOrder(item, movie);
        System.out.println(orderResponse.getBody());
        deleteMovie(movie);
        deleteOrder(orderResponse.getBody().getData().get(0).getOrderId());
        assertEquals(ResponseCode.OK, orderResponse.getBody().getResponseCode());
        assertEquals(HttpStatus.OK, orderResponse.getStatusCode());
    }

    @Test
    void testCantOrderItemWithNonExistingMovieId(){
        ResponseEntity<OrderApiResponse> orderResponse = addInvalidOrder(createOrderItem());
        assertEquals(ResponseCode.INVALID_REQUEST, orderResponse.getBody().getResponseCode());
    }

    @Test
    void testInvalidRequestOnDeletingNonExistingOrder(){
        ResponseEntity<GeneralApiResponse> response = deleteOrder("abc");
        assertEquals(ResponseCode.INVALID_REQUEST, response.getBody().getResponseCode());
    }
    @Test
    void testCheckoutWorksWithOpenOrder() throws IOException {
        Movie movie = getMockMovie();
        OrderItem item = createOrderItem();
        ResponseEntity<MovieApiResponse> addResponse = addMovie(movie);
        ResponseEntity<OrderApiResponse> orderResponse = addValidOrder(item, movie);
        String orderId = orderResponse.getBody().getData().get(0).getOrderId();
        ResponseEntity<OrderApiResponse> checkoutResponse = checkout(orderId);
        deleteMovie(movie);
        deleteOrder(orderId);
        deleteStats(movie.getImdbId());
        System.out.println(checkoutResponse.getBody());
        assertEquals(OrderStatus.CLOSED, checkoutResponse.getBody().getData().get(0).getOrderStatus());
        assertEquals(ResponseCode.OK, checkoutResponse.getBody().getResponseCode());
    }

    @Test
    void testCantCheckoutWithClosedOrder() throws IOException {
        Movie movie = getMockMovie();
        OrderItem item = createOrderItem();
        addMovie(movie);
        ResponseEntity<OrderApiResponse> orderResponse = addValidOrder(item, movie);
        String orderId = orderResponse.getBody().getData().get(0).getOrderId();
        ResponseEntity<OrderApiResponse> checkoutResponse = checkout(orderId);
        ResponseEntity<OrderApiResponse> checkoutResponse2 = checkout(orderId);
        deleteMovie(movie);
        deleteOrder(orderId);
        deleteStats(movie.getImdbId());
        assertEquals(OrderStatus.CLOSED, checkoutResponse.getBody().getData().get(0).getOrderStatus());
        assertEquals(ResponseCode.OK, checkoutResponse.getBody().getResponseCode());
        assertEquals(ResponseCode.INVALID_REQUEST, checkoutResponse2.getBody().getResponseCode());
    }

    @Test
    void testExtendWorksWithValidOrderItem(){
        OrderItem item = createOrderItem();
        Movie movie = getMockMovie();
        item.setMovieId(movie.getImdbId());
        item.setMovieReleaseDate(LocalDate.now());

        // Extend needs movie and order to be present in db
        ResponseEntity<MovieApiResponse> addResponse = addMovie(movie);
        System.out.println(addResponse.getBody());
        ResponseEntity<OrderApiResponse> orderResponse = addValidOrder(item, movie);
        System.out.println(orderResponse.getBody());
        String orderId = orderResponse.getBody().getData().get(0).getOrderId();
        ResponseEntity<OrderApiResponse> extendResponse = extendOrder(orderId, item);
        System.out.println(extendResponse.getBody());

        // Clean up
        deleteMovie(movie);
        deleteOrder(orderResponse.getBody().getData().get(0).getOrderId());
        assertEquals(ResponseCode.OK, extendResponse.getBody().getResponseCode());
        assertEquals(HttpStatus.OK, extendResponse.getStatusCode());
    }

    @Test
    void testOrderTotalPriceCalculatedCorrectly(){
        OrderItem item = createOrderItem();
        Movie movie = getMockMovie();
        movie.setReleaseDate(LocalDate.now());
        movie.setPriceClass();

        item.setMovieId(movie.getImdbId());
        item.setMovieReleaseDate(movie.getReleaseDate());
        item.calculateTotalPrice();
        item.setRentDurationInWeeks(5);
        System.out.println(item.getTotalPrice());

        addMovie(movie);
        //Order with two items of total price 25
        ResponseEntity<OrderApiResponse> orderResponse = addValidOrder(item, movie);
        String orderId = orderResponse.getBody().getData().get(0).getOrderId();
        ResponseEntity<OrderApiResponse> extendResponse = extendOrder(orderId, item);

        //clean up
        deleteMovie(movie);
        deleteOrder(orderResponse.getBody().getData().get(0).getOrderId());

        // 25 + 25 = 50 -> total should be 50
        assertEquals(0, BigDecimal.valueOf(50).compareTo(extendResponse.getBody().getData().get(0).getTotalPrice()));
    }

    public void deleteStats(String movieId){
        String delUrl = "http://localhost:" + port + "/orders/stats/clear?movieId=" + movieId;
        this.restTemplate.exchange(delUrl, HttpMethod.DELETE,
                null, OrderApiResponse.class);
    }

    public ResponseEntity<OrderApiResponse> extendOrder(String orderId, OrderItem item){
        String url = "http://localhost:" + port +"/orders/extend/"+orderId;
        HttpEntity<OrderItem> orderRequest = new HttpEntity<>(item, getJsonHeaders());
        ResponseEntity<OrderApiResponse> response = this.restTemplate.exchange(url, HttpMethod.POST,
                orderRequest, OrderApiResponse.class);
        return response;
    }


    public ResponseEntity<OrderApiResponse> addValidOrder(OrderItem orderItem, Movie movie) {
        // Create order
        String orderAddUrl = "http://localhost:" + port + "/orders/new";
        orderItem.setMovieId(movie.getImdbId());
        HttpEntity<OrderItem> orderRequest = new HttpEntity<>(orderItem, getJsonHeaders());
        ResponseEntity<OrderApiResponse> orderResponse = this.restTemplate.exchange(orderAddUrl, HttpMethod.POST,
                orderRequest, OrderApiResponse.class);
        logger.info(orderResponse.getBody().getData().toString());
        return orderResponse;
    }

    public ResponseEntity<OrderApiResponse> addInvalidOrder(OrderItem orderItem) {
        String orderAddUrl = "http://localhost:" + port + "/orders/new";
        orderItem.setMovieId(UUID.randomUUID().toString());
        orderItem.setMovieReleaseDate(LocalDate.now());
        HttpEntity<OrderItem> orderRequest = new HttpEntity<>(orderItem, getJsonHeaders());
        ResponseEntity<OrderApiResponse> orderResponse = this.restTemplate.exchange(orderAddUrl, HttpMethod.POST,
                orderRequest, OrderApiResponse.class);
        return orderResponse;
    }

    public ResponseEntity<GeneralApiResponse> deleteOrder(String orderid) {
        String delUrl = "http://localhost:" + port + "/orders/delete?id=" + orderid;
        HttpEntity<Order> entity = new HttpEntity<>(null);
        ResponseEntity<GeneralApiResponse> response = this.restTemplate.exchange
                (delUrl, HttpMethod.DELETE, entity, GeneralApiResponse.class);
        return response;
    }

    public ResponseEntity<OrderApiResponse> checkout(String orderid) throws IOException {
        RestTemplate patchRestTemplate = restTemplate.getRestTemplate();
        CloseableHttpClient httpClient = HttpClientBuilder.create().build();
        patchRestTemplate.setRequestFactory(new HttpComponentsClientHttpRequestFactory(httpClient));
        String checkUrl = "http://localhost:" + port + "/orders/checkout?orderId=" + orderid;
        HttpEntity<Order> entity = new HttpEntity<>(null);
        ResponseEntity<OrderApiResponse> orderResponse = this.restTemplate.exchange(checkUrl, HttpMethod.PATCH,
                null, OrderApiResponse.class);
        return orderResponse;
    }

    public Order createMockOrder(){
        Order order = new Order();
        return order;
    }

    public OrderItem createOrderItem(){
        OrderItem orderItem = new OrderItem();
        orderItem.setMovieReleaseDate(LocalDate.now());
        orderItem.setRentDurationInWeeks(5);
        orderItem.calculateTotalPrice();
        return orderItem;
    }
}

package ee.fujitsu.movieapi;

import ee.fujitsu.movieapi.db.model.movie.Movie;
import ee.fujitsu.movieapi.db.model.order.Order;
import ee.fujitsu.movieapi.db.model.order.OrderItem;
import ee.fujitsu.movieapi.db.model.order.OrderStatus;
import ee.fujitsu.movieapi.rest.api.response.*;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class OrderTests extends AbstractMovieApiTest {
    private static final Logger logger = LoggerFactory.getLogger(OrderTests.class);
    private final String[] getEndpoints = {"/orders", "/orders/1", "/orders/stats"};
    @LocalServerPort
    private int port;
    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    void testSuccessfulResponseShouldContainOkStatus() throws Exception {
        for (String end : getEndpoints) {
            verifyResponse("http://localhost:" + port + end, ResponseCode.OK, false);
        }
    }


    @Test
    void addOrderTest(){
        OrderItem item = createOrderItem();
        Movie movie = getMockMovie();

        ResponseEntity<MovieApiResponse> addResponse = addMovie(movie);
        ResponseEntity<OrderApiResponse> orderResponse = addValidOrder(item, movie);

        System.out.println(addResponse.getBody());
        System.out.println(orderResponse.getBody());

        assertEquals(ResponseCode.OK, orderResponse.getBody().getResponseCode());
        assertEquals(HttpStatus.OK, orderResponse.getStatusCode());
    }

    @Test
    void deleteOrderTest() {
        OrderItem item = createOrderItem();
        Movie movie = getMockMovie();

        ResponseEntity<MovieApiResponse> addResponse = addMovie(movie);
        ResponseEntity<OrderApiResponse> orderResponse = addValidOrder(item, movie);

        System.out.println(addResponse.getBody());
        System.out.println(orderResponse.getBody());

        deleteMovie(movie);
        deleteOrder(orderResponse.getBody().getData().get(0).getOrderId());

        assertEquals(ResponseCode.OK, orderResponse.getBody().getResponseCode());
        assertEquals(HttpStatus.OK, orderResponse.getStatusCode());
    }


    @Test
    void testCantOrderItemWithNonExistingMovieId() {
        ResponseEntity<OrderApiResponse> orderResponse = addInvalidOrder(createOrderItem());
        assertEquals(ResponseCode.INVALID_REQUEST, orderResponse.getBody().getResponseCode());
    }

    @Test
    void testInvalidRequestOnDeletingNonExistingOrder() {
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

        //Clean up
        deleteMovie(movie);
        deleteOrder(orderId);
        deleteStats(movie.getImdbId());

        assertEquals(OrderStatus.CLOSED, checkoutResponse.getBody().getData().get(0).getOrderStatus());
        assertEquals(ResponseCode.OK, checkoutResponse.getBody().getResponseCode());
        assertEquals(ResponseCode.INVALID_REQUEST, checkoutResponse2.getBody().getResponseCode());
    }

    @Test
    void testExtendWorksWithValidOrderItem() {
        OrderItem item = createOrderItem();
        Movie movie = getMockMovie();
        item.setMovieId(movie.getImdbId());
        item.setMovieReleaseDate(LocalDate.now());

        // Extend needs movie and order to be present in db
        ResponseEntity<MovieApiResponse> addResponse = addMovie(movie);
        ResponseEntity<OrderApiResponse> orderResponse = addValidOrder(item, movie);
        String orderId = orderResponse.getBody().getData().get(0).getOrderId();
        ResponseEntity<OrderApiResponse> extendResponse = extendOrder(orderId, item);

        System.out.println(addResponse.getBody());
        System.out.println(orderResponse.getBody());
        System.out.println(extendResponse.getBody());

        assertEquals(ResponseCode.OK, extendResponse.getBody().getResponseCode());
        assertEquals(HttpStatus.OK, extendResponse.getStatusCode());
    }

    @Test
    void testOrderTotalPriceCalculatedCorrectly() {
        OrderItem item = createOrderItem();
        Movie movie = getMockMovie();
        movie.setReleaseDate(LocalDate.now());
        movie.setPriceClass();

        item.setMovieId(movie.getImdbId());
        item.setMovieReleaseDate(movie.getReleaseDate());
        item.setRentDurationInWeeks(5);
        item.calculateTotalPrice();
        System.out.println(item.getTotalPrice());

        //Order with two items of total price 25
        ResponseEntity<MovieApiResponse> addResponse = addMovie(movie);
        ResponseEntity<OrderApiResponse> orderResponse = addValidOrder(item, movie);
        String orderId = orderResponse.getBody().getData().get(0).getOrderId();
        ResponseEntity<OrderApiResponse> extendResponse = extendOrder(orderId, item);

        //clean up
        deleteMovie(movie);
        deleteOrder(orderResponse.getBody().getData().get(0).getOrderId());
        System.out.println(extendResponse.getBody().getData().get(0).getTotalPrice());
        // 25 + 25 = 50 -> total should be 50
        assertEquals(0, BigDecimal.valueOf(50).compareTo(extendResponse.getBody().getData().get(0).getTotalPrice()));
    }

    @Test
    void negativeWeeksOrderTest(){
        OrderItem item = createOrderItem();
        Movie movie = getMockMovie();
        movie.setReleaseDate(LocalDate.now());
        movie.setPriceClass();

        item.setMovieId(movie.getImdbId());
        item.setMovieReleaseDate(movie.getReleaseDate());
        // Set negative rent weeks
        item.setRentDurationInWeeks(-5);
        item.calculateTotalPrice();

        ResponseEntity<MovieApiResponse> addResponse = addMovie(movie);
        ResponseEntity<OrderApiResponse> orderResponse = addValidOrder(item, movie);
        System.out.println(orderResponse.getBody().getData().get(0).getTotalPrice());
        System.out.println(orderResponse.getBody().getResponseCode());

        //clean up
        deleteMovie(movie);
        deleteOrder(orderResponse.getBody().getData().get(0).getOrderId());
        assertEquals(ResponseCode.INVALID_REQUEST, orderResponse.getBody().getResponseCode());
    }

    @Test
    void zeroWeeksOrderTest(){
        OrderItem item = createOrderItem();
        Movie movie = getMockMovie();
        movie.setReleaseDate(LocalDate.now());
        movie.setPriceClass();

        item.setMovieId(movie.getImdbId());
        item.setMovieReleaseDate(movie.getReleaseDate());
        // Set 0 rent weeks
        item.setRentDurationInWeeks(0);
        item.calculateTotalPrice();

        ResponseEntity<MovieApiResponse> addResponse = addMovie(movie);
        ResponseEntity<OrderApiResponse> orderResponse = addValidOrder(item, movie);

        System.out.println(orderResponse.getBody().getData().get(0).getTotalPrice());
        System.out.println(orderResponse.getBody().getResponseCode());

        //clean up
        deleteMovie(movie);
        deleteOrder(orderResponse.getBody().getData().get(0).getOrderId());
        assertEquals(ResponseCode.INVALID_REQUEST, orderResponse.getBody().getResponseCode());
    }

    @Test
    void checkoutWrongIDTest(){
        ResponseEntity<OrderApiResponse> checkout = checkout(UUID.randomUUID().toString());
        System.out.println(checkout.getBody().getMessage());
        System.out.println(checkout.getBody().toString());
        assertEquals("Order not found", checkout.getBody().getMessage());
        assertEquals(ResponseCode.OK, checkout.getBody().getResponseCode());
    }

    @Test
    void statisticsTest(){
        OrderItem item = createOrderItem();
        Movie movie = getMockMovie();
        movie.setReleaseDate(LocalDate.now());
        movie.setPriceClass();

        item.setMovieId(movie.getImdbId());
        item.setMovieReleaseDate(movie.getReleaseDate());
        item.setRentDurationInWeeks(5);
        item.calculateTotalPrice();

        // Send requests and get responses
        ResponseEntity<MovieApiResponse> addResponse = addMovie(movie);
        ResponseEntity<OrderApiResponse> orderResponse = addValidOrder(item, movie);
        String orderId = orderResponse.getBody().getData().get(0).getOrderId();
        ResponseEntity<OrderApiResponse> checkoutResponse = checkout(orderId);
        ResponseEntity<OrderStatisticsApiResponse> statsResponse = getStatistics();

        //Clean up
        deleteMovie(movie);
        deleteOrder(orderId);
        deleteStats(movie.getImdbId());

        assertEquals(1, statsResponse.getBody().getOrderStatistics().getMovieOrderCount().get(movie.getImdbId()));
        assertEquals(5, statsResponse.getBody().getOrderStatistics().getMovieRentedFor().get(movie.getImdbId()));
    }

    @Test
    void sameOrdersIncrementStatisticsTest(){
        OrderItem item = createOrderItem();
        Movie movie = getMockMovie();
        movie.setReleaseDate(LocalDate.now());
        movie.setPriceClass();

        item.setMovieId(movie.getImdbId());
        item.setMovieReleaseDate(movie.getReleaseDate());
        item.setRentDurationInWeeks(5);
        item.calculateTotalPrice();

        // Send requests and get responses
        ResponseEntity<MovieApiResponse> addResponse = addMovie(movie);
        ResponseEntity<OrderApiResponse> orderResponse = addValidOrder(item, movie);
        String orderId = orderResponse.getBody().getData().get(0).getOrderId();
        ResponseEntity<OrderApiResponse> checkoutResponse = checkout(orderId);

        orderResponse = addValidOrder(item, movie);
        orderId = orderResponse.getBody().getData().get(0).getOrderId();
        checkoutResponse = checkout(orderId);

        ResponseEntity<OrderStatisticsApiResponse> statsResponse = getStatistics();

        // Clean up
        deleteMovie(movie);
        deleteOrder(orderId);
        deleteStats(movie.getImdbId());

        System.out.printf("Movie order count: %d\n", statsResponse.getBody().getOrderStatistics().getMovieOrderCount().get(movie.getImdbId()));
        System.out.printf("Movie order time in weeks: %d\n", statsResponse.getBody().getOrderStatistics().getMovieRentedFor().get(movie.getImdbId()));

        assertEquals(2, statsResponse.getBody().getOrderStatistics().getMovieOrderCount().get(movie.getImdbId()));
        assertEquals(10, statsResponse.getBody().getOrderStatistics().getMovieRentedFor().get(movie.getImdbId()));
    }

    @Test
    void clearStatisticsTest(){
        OrderItem item = createOrderItem();
        Movie movie = getMockMovie();
        movie.setReleaseDate(LocalDate.now());
        movie.setPriceClass();

        item.setMovieId(movie.getImdbId());
        item.setMovieReleaseDate(movie.getReleaseDate());
        item.setRentDurationInWeeks(5);
        item.calculateTotalPrice();

        // Send requests and get responses
        ResponseEntity<MovieApiResponse> addResponse = addMovie(movie);
        ResponseEntity<OrderApiResponse> orderResponse = addValidOrder(item, movie);
        String orderId = orderResponse.getBody().getData().get(0).getOrderId();
        ResponseEntity<OrderApiResponse> checkoutResponse = checkout(orderId);

        ResponseEntity<OrderStatisticsApiResponse> statsResponseBefore = getStatistics();

        //Clear up
        deleteMovie(movie);
        deleteOrder(orderId);
        deleteStats(movie.getImdbId());

        ResponseEntity<OrderStatisticsApiResponse> statsResponseAfter = getStatistics();

        assertTrue(statsResponseBefore.getBody().getOrderStatistics().getMovieOrderCount().containsKey(movie.getImdbId()));
        assertFalse(statsResponseAfter.getBody().getOrderStatistics().getMovieOrderCount().containsKey(movie.getImdbId()));
    }



    public void deleteStats(String movieId) {
        String delUrl = "http://localhost:" + port + "/orders/stats/clear?movieId=" + movieId;
        this.restTemplate.exchange(delUrl, HttpMethod.DELETE,
                null, OrderApiResponse.class);
    }

    public ResponseEntity<OrderApiResponse> extendOrder(String orderId, OrderItem item) {
        String url = "http://localhost:" + port + "/orders/extend/" + orderId;
        HttpEntity<OrderItem> orderRequest = new HttpEntity<>(item, getJsonHeaders());
        ResponseEntity<OrderApiResponse> response = this.restTemplate.exchange(url, HttpMethod.POST, orderRequest, OrderApiResponse.class);
        return response;
    }


    public ResponseEntity<OrderApiResponse> addValidOrder(OrderItem orderItem, Movie movie) {
        // Create order
        String orderAddUrl = "http://localhost:" + port + "/orders/new";
        orderItem.setMovieId(movie.getImdbId());
        HttpEntity<OrderItem> orderRequest = new HttpEntity<>(orderItem, getJsonHeaders());
        ResponseEntity<OrderApiResponse> orderResponse = this.restTemplate.exchange(orderAddUrl, HttpMethod.POST, orderRequest, OrderApiResponse.class);
        logger.info(orderResponse.getBody().getData().toString());
        return orderResponse;
    }

    public ResponseEntity<OrderApiResponse> addInvalidOrder(OrderItem orderItem) {
        String orderAddUrl = "http://localhost:" + port + "/orders/new";
        orderItem.setMovieId(UUID.randomUUID().toString());
        orderItem.setMovieReleaseDate(LocalDate.now());
        HttpEntity<OrderItem> orderRequest = new HttpEntity<>(orderItem, getJsonHeaders());
        ResponseEntity<OrderApiResponse> orderResponse = this.restTemplate.exchange(orderAddUrl, HttpMethod.POST, orderRequest, OrderApiResponse.class);
        return orderResponse;
    }

    public ResponseEntity<GeneralApiResponse> deleteOrder(String orderid) {
        String delUrl = "http://localhost:" + port + "/orders/delete?id=" + orderid;
        HttpEntity<Order> entity = new HttpEntity<>(null);
        ResponseEntity<GeneralApiResponse> response = this.restTemplate.exchange(delUrl, HttpMethod.DELETE, entity, GeneralApiResponse.class);
        return response;
    }

    public ResponseEntity<OrderApiResponse> checkout(String orderid){
        RestTemplate patchRestTemplate = restTemplate.getRestTemplate();
        CloseableHttpClient httpClient = HttpClientBuilder.create().build();
        patchRestTemplate.setRequestFactory(new HttpComponentsClientHttpRequestFactory(httpClient));
        String checkUrl = "http://localhost:" + port + "/orders/checkout?orderId=" + orderid;
        HttpEntity<Order> entity = new HttpEntity<>(null);
        ResponseEntity<OrderApiResponse> orderResponse = this.restTemplate.exchange(checkUrl, HttpMethod.PATCH, null, OrderApiResponse.class);
        return orderResponse;
    }

    public ResponseEntity<OrderStatisticsApiResponse> getStatistics(){
        String url = "http://localhost:" + port + "/orders/stats/";
        HttpEntity<Order> entity = new HttpEntity<>(null);
        ResponseEntity<OrderStatisticsApiResponse> response = this.restTemplate.exchange(url, HttpMethod.GET, entity, OrderStatisticsApiResponse.class);
        return response;
    }

    public Order createMockOrder() {
        Order order = new Order();
        return order;
    }

    public OrderItem createOrderItem() {
        OrderItem orderItem = new OrderItem();
        orderItem.setMovieReleaseDate(LocalDate.now());
        orderItem.setRentDurationInWeeks(5);
        orderItem.calculateTotalPrice();
        return orderItem;
    }
}

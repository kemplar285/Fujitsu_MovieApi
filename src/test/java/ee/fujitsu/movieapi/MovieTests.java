package ee.fujitsu.movieapi;

import ee.fujitsu.movieapi.db.model.movie.Movie;
import ee.fujitsu.movieapi.rest.api.response.GeneralApiResponse;
import ee.fujitsu.movieapi.rest.api.response.MovieApiResponse;
import ee.fujitsu.movieapi.rest.api.response.ResponseCode;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class MovieTests {
    @LocalServerPort
    private int port;
    @Autowired
    private TestRestTemplate restTemplate;

    private final String[] getEndpoints = {"/movies", "/movies/testId", "/movies/test"};

    @Test
    void testSuccessfulResponseShouldContainOkStatus() throws Exception {
        for (String end : getEndpoints) {
            verifyResponse("http://localhost:" + port + end, ResponseCode.OK, false);
        }
    }

    @Test
    void testFailedResponseShouldContainStatusAndMessage() throws Exception {
        String url = "http://localhost:" + port + "/" + RandomStringUtils.random(10);
        verifyResponse(url, ResponseCode.INVALID_REQUEST, true);
    }

    @Test
    void testValidMovieShouldSuccessfullyBeAddedAndDeleted() throws Exception {
        String id = UUID.randomUUID().toString();
        addMovie(id);
        updateMovie(id);
        deleteMovie(id);
    }

    public void verifyResponse(String url, ResponseCode code, boolean expectMessage) throws Exception {
        GeneralApiResponse response = restTemplate.getForObject(url, GeneralApiResponse.class);
        System.out.println(response);
        assertEquals(code, response.getResponseCode());
        if (expectMessage) {
            assertNotNull(response.getMessage());
        }
    }

    public void addMovie(String movieId) {
        String addUrl = "http://localhost:" + port + "/movies/add";
        Movie movie = new Movie();
        movie.setImdbId(movieId);
        movie.setReleaseDate(LocalDate.now());
        movie.setTitle("TestTitle");
        movie.setCategories(new HashSet<>());
        HttpEntity<Movie> request = new HttpEntity<>(movie);

        ResponseEntity<MovieApiResponse> response = this.restTemplate.postForEntity(addUrl, request, MovieApiResponse.class);
        assertEquals(200, response.getStatusCodeValue());
        assertEquals(ResponseCode.OK, response.getBody().getResponseCode());
        System.out.println(response.getBody());


        // Second request with the same id should be invalid
        response = this.restTemplate.postForEntity(addUrl, request, MovieApiResponse.class);
        assertEquals(200, response.getStatusCodeValue());
        assertEquals(ResponseCode.INVALID_REQUEST, response.getBody().getResponseCode());
        assertNotNull(response.getBody().getMessage());
        System.out.println(response.getBody());
    }

    public void updateMovie(String movieId) {
        String movieUrl = "http://localhost:" + port + "/movies/id/" + movieId;
        ResponseEntity<MovieApiResponse> response = this.restTemplate.getForEntity(movieUrl, MovieApiResponse.class);
        Movie movie = response.getBody().getData().get(0);
        movie.setTitle("updated");
        String updateUrl = "http://localhost:" + port + "/movies/update?id=" + movieId;
        ResponseEntity<GeneralApiResponse> secondResponse = this.restTemplate.exchange
                (updateUrl, HttpMethod.PUT, new HttpEntity<>(movie), GeneralApiResponse.class);
        assertEquals(ResponseCode.OK, secondResponse.getBody().getResponseCode());
        assertEquals(HttpStatus.OK, secondResponse.getStatusCode());
        System.out.println(secondResponse.getBody());
    }

    public void deleteMovie(String movieId) {
        String addUrl = "http://localhost:" + port + "/movies/delete?id=" + movieId;
        HttpEntity<Movie> entity = new HttpEntity<>(null);
        ResponseEntity<GeneralApiResponse> response = this.restTemplate.exchange
                (addUrl, HttpMethod.DELETE, entity, GeneralApiResponse.class);

        assertEquals(200, response.getStatusCodeValue());
        assertEquals(ResponseCode.OK, response.getBody().getResponseCode());
        assertNotNull(response.getBody().getMessage());
        System.out.println(response.getBody());

        // Second request with the same id should be invalid
        response = this.restTemplate.exchange
                (addUrl, HttpMethod.DELETE, entity, GeneralApiResponse.class);
        assertEquals(ResponseCode.INVALID_REQUEST, response.getBody().getResponseCode());
        assertNotNull(response.getBody().getMessage());
        System.out.println(response.getBody());
    }


}

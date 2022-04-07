package ee.fujitsu.movieapi;

import ee.fujitsu.movieapi.db.model.movie.Movie;
import ee.fujitsu.movieapi.rest.api.response.GeneralApiResponse;
import ee.fujitsu.movieapi.rest.api.response.MovieApiResponse;
import ee.fujitsu.movieapi.rest.api.response.ResponseCode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.*;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public abstract class AbstractMovieApiTest {
    @LocalServerPort
    private int port;
    @Autowired
    private TestRestTemplate restTemplate;

    public ResponseEntity<MovieApiResponse> addMovie(Movie movie) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        // Add movie for creating order
        String movieAddUrl = "http://localhost:" + port + "/movies/add";
        HttpEntity<Movie> movieRequest = new HttpEntity<>(movie, headers);
        ResponseEntity<MovieApiResponse> movieResponse = this.restTemplate.postForEntity(movieAddUrl, movieRequest, MovieApiResponse.class);
        return movieResponse;
    }

    public ResponseEntity<GeneralApiResponse> deleteMovie(Movie movie) {
        //Delete test movie
        String delUrl = "http://localhost:" + port + "/movies/delete?id=" + movie.getImdbId();
        HttpEntity<Movie> entity = new HttpEntity<>(null);
        ResponseEntity<GeneralApiResponse> response = this.restTemplate.exchange
                (delUrl, HttpMethod.DELETE, entity, GeneralApiResponse.class);
        return response;
    }

    public void verifyResponse(String url, ResponseCode code, boolean expectMessage) throws Exception {
        GeneralApiResponse response = restTemplate.getForObject(url, GeneralApiResponse.class);
        System.out.println(response);
        assertEquals(code, response.getResponseCode());
        if (expectMessage) {
            assertNotNull(response.getMessage());
        }
    }

    public Movie getMockMovie() {
        String id = UUID.randomUUID().toString();
        Movie movie = new Movie();
        movie.setImdbId(id);
        movie.setReleaseDate(LocalDate.now());
        movie.setTitle("TestTitle");
        movie.setCategories(new HashSet<>());
        return movie;
    }

    public HttpHeaders getJsonHeaders(){
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        return headers;
    }



}

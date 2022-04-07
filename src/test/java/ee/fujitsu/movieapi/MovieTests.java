package ee.fujitsu.movieapi;

import ee.fujitsu.movieapi.db.model.movie.Movie;
import ee.fujitsu.movieapi.rest.api.response.GeneralApiResponse;
import ee.fujitsu.movieapi.rest.api.response.MovieApiResponse;
import ee.fujitsu.movieapi.rest.api.response.ResponseCode;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.*;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class MovieTests extends AbstractMovieApiTest {
    private static final Logger logger = LoggerFactory.getLogger(MovieTests.class);
    @LocalServerPort
    private int port;
    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    void testSuccessfulResponseShouldContainOkStatus() throws Exception {
        String[] getEndpoints = {"/movies", "/movies/id/testId", "/movies/test"};
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
    void testValidMovieShouldSuccessfullyBeAddedUpdatedAndDeleted() throws Exception {
        Movie movie = getMockMovie();
        ResponseEntity<MovieApiResponse> addResponse = addMovie(movie);
        ResponseEntity<GeneralApiResponse> updateResponse = updateMovie(movie.getImdbId());
        ResponseEntity<GeneralApiResponse> deleteResponse = deleteMovie(movie.getImdbId());
        assertEquals(ResponseCode.OK, addResponse.getBody().getResponseCode());
        assertEquals(HttpStatus.OK, addResponse.getStatusCode());
        assertEquals(ResponseCode.OK, updateResponse.getBody().getResponseCode());
        assertEquals(HttpStatus.OK, updateResponse.getStatusCode());
        assertEquals(ResponseCode.OK, deleteResponse.getBody().getResponseCode());
        assertEquals(HttpStatus.OK, deleteResponse.getStatusCode());
    }

    @Test
    void testMovieWithMissingValuesShouldNotBeCreated() {
        ResponseEntity<GeneralApiResponse> response = addInvalidMovie();
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    void testUpdateShouldNotWorkWithMissingValues() {
        Movie movie = getMockMovie();
        addMovie(movie);
        ResponseEntity<MovieApiResponse> response = invalidUpdateMovie(movie.getImdbId());
        deleteMovie(movie.getImdbId());

        assertEquals(ResponseCode.INVALID_REQUEST, response.getBody().getResponseCode());
    }

    @Test
    void testDeleteTwoTimesShouldBeSafe() {
        Movie movie = getMockMovie();
        addMovie(movie);
        ResponseEntity<GeneralApiResponse> delresponse = deleteMovie(movie.getImdbId());
        assertEquals(200, delresponse.getStatusCodeValue());
        assertEquals(ResponseCode.OK, delresponse.getBody().getResponseCode());
        assertNotNull(delresponse.getBody().getMessage());
        // Try to delete for the second time
        delresponse = deleteMovie(movie.getImdbId());
        assertEquals(ResponseCode.INVALID_REQUEST, delresponse.getBody().getResponseCode());
        assertNotNull(delresponse.getBody().getMessage());
        logger.info(delresponse.getBody().toString());
    }

    @Test
    void testMovieIdShouldBeUnique(){
        Movie movie = getMockMovie();
        addMovie(movie);
        ResponseEntity<MovieApiResponse> response = addMovie(movie);
        deleteMovie(movie);
        assertEquals(ResponseCode.INVALID_REQUEST, response.getBody().getResponseCode());
    }

    @Test
    void omdbFetchWorksWithCorrectId() {
        Movie movie = getMockMovie();
        movie.setImdbId("tt0137523");
        addMovie(movie);
        ResponseEntity<MovieApiResponse> response = findMovieById(movie.getImdbId());
        logger.info(response.getBody().toString());
        assertEquals(true, response.getBody().getData().get(0).movieMetadata.getDataFound());
        deleteMovie(movie.getImdbId());
    }

    public ResponseEntity<GeneralApiResponse> deleteMovie(String movieId) {
        String delUrl = "http://localhost:" + port + "/movies/delete?id=" + movieId;
        HttpEntity<Movie> entity = new HttpEntity<>(null);
        ResponseEntity<GeneralApiResponse> response = this.restTemplate.exchange
                (delUrl, HttpMethod.DELETE, entity, GeneralApiResponse.class);
        return response;
    }

    public void verifyResponse(String url, ResponseCode code, boolean expectMessage) throws Exception {
        GeneralApiResponse response = restTemplate.getForObject(url, GeneralApiResponse.class);
        logger.info(response.toString());
        assertEquals(code, response.getResponseCode());
        if (expectMessage) {
            assertNotNull(response.getMessage());
        }
    }

    public ResponseEntity<GeneralApiResponse> addInvalidMovie() {
        Movie movie = getMockMovie();
        movie.setImdbId("");
        String addUrl = "http://localhost:" + port + "/movies/add";
        HttpEntity<Movie> request = new HttpEntity<Movie>(movie, getJsonHeaders());
        ResponseEntity<GeneralApiResponse> response = this.restTemplate.postForEntity(addUrl, request, GeneralApiResponse.class);
        logger.info(response.getBody().toString());
        return response;
    }

    public ResponseEntity<GeneralApiResponse> updateMovie(String movieId) {
        // Get movie from db
        ResponseEntity<MovieApiResponse> response = findMovieById(movieId);
        Movie movie = response.getBody().getData().get(0);
        movie.setTitle("updated");

        // Update movie
        String updateUrl = "http://localhost:" + port + "/movies/update?id=" + movieId;
        ResponseEntity<GeneralApiResponse> secondResponse = this.restTemplate.exchange
                (updateUrl, HttpMethod.PUT, new HttpEntity<>(movie), GeneralApiResponse.class);
        return secondResponse;
    }

    public ResponseEntity<MovieApiResponse> invalidUpdateMovie(String movieId) {
        ResponseEntity<MovieApiResponse> findResponse = findMovieById(movieId);
        Movie movie = findResponse.getBody().getData().get(0);
        //Missing value - release date
        movie.setReleaseDate(null);

        String updateUrl = "http://localhost:" + port + "/movies/update?id=" + movieId;
        HttpEntity<Movie> entity = new HttpEntity<>(movie, getJsonHeaders());
        ResponseEntity<MovieApiResponse> secondResponse = this.restTemplate.exchange
                (updateUrl, HttpMethod.PUT, entity, MovieApiResponse.class);
        logger.info(secondResponse.getBody().toString());
        return secondResponse;
    }

    public ResponseEntity<MovieApiResponse> findMovieById(String movieId) {
        String movieUrl = "http://localhost:" + port + "/movies/id/" + movieId;
        ResponseEntity<MovieApiResponse> response = this.restTemplate.getForEntity(movieUrl, MovieApiResponse.class);
        return response;
    }


}

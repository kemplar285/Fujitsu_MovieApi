package ee.fujitsu.movieapi;

import ee.fujitsu.movieapi.db.model.movie.Movie;
import ee.fujitsu.movieapi.db.model.movie.MoviePriceClass;
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

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class MovieTests extends AbstractMovieApiTest{
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
    void addMovieTest() throws Exception {
        Movie movie = getMockMovie();
        ResponseEntity<MovieApiResponse> addResponse = addMovie(movie);
        System.out.println(addResponse.getBody().getResponseCode());
        System.out.println(addResponse.getStatusCode());
        assertEquals(ResponseCode.OK, addResponse.getBody().getResponseCode());
        assertEquals(HttpStatus.OK, addResponse.getStatusCode());
    }
    @Test
    void updateMovieTest(){
        Movie movie = getMockMovie();
        addMovie(movie);
        ResponseEntity<GeneralApiResponse> updateResponse = updateMovie(movie.getImdbId());
        System.out.println(updateResponse.getBody().getResponseCode());
        assertEquals(ResponseCode.OK, updateResponse.getBody().getResponseCode());
        assertEquals(HttpStatus.OK, updateResponse.getStatusCode());

    }

    @Test
    void deleteMovieTest(){
        Movie movie = getMockMovie();
        addMovie(movie);
        ResponseEntity<GeneralApiResponse> deleteResponse = deleteMovie(movie.getImdbId());
        System.out.println(deleteResponse.getStatusCode());
        System.out.println(deleteResponse.getBody().getResponseCode());
        assertEquals(ResponseCode.OK, deleteResponse.getBody().getResponseCode());
        assertEquals(HttpStatus.OK, deleteResponse.getStatusCode());
    }

    @Test
    void testMovieWithMissingValuesShouldNotBeCreated() {
        ResponseEntity<GeneralApiResponse> response = addInvalidMovie();
        System.out.println(response.getBody().getResponseCode());
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    void testUpdateShouldNotWorkWithMissingValues() {
        Movie movie = getMockMovie();
        addMovie(movie);
        ResponseEntity<MovieApiResponse> response = invalidUpdateMovie(movie.getImdbId());
        System.out.println(response.getBody().getResponseCode());
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
        System.out.println(delresponse.getBody().toString());
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
        System.out.println(response.getBody().getResponseCode());
        assertEquals(ResponseCode.INVALID_REQUEST, response.getBody().getResponseCode());
    }

    @Test
        void omdbFetchWorksWithCorrectId() {
        Movie movie = getMockMovie();
        movie.setImdbId("tt0137523");
        addMovie(movie);
        ResponseEntity<MovieApiResponse> response = findMovieById(movie.getImdbId());
        logger.info(response.getBody().toString());
        System.out.println(response.getBody().getData().get(0).getTitle());
        assertEquals(true, response.getBody().getData().get(0).movieMetadata.getDataFound());
        deleteMovie(movie.getImdbId());
    }

    @Test
    void zeroLengthTitleUpdateTest(){
        Movie movie = getMockMovie();
        movie.setTitle("");
        movie.setReleaseDate(LocalDate.of(2021, 01, 01));
        ResponseEntity<MovieApiResponse> addResponse = addMovie(movie);

        assertEquals(ResponseCode.INVALID_REQUEST, addResponse.getBody().getResponseCode());
        assertEquals(HttpStatus.OK, addResponse.getStatusCode());

        deleteMovie(movie.getImdbId());
    }

    @Test
    void releaseDateCantBeTooLongAgo(){
        Movie movie = getMockMovie();
        movie.setReleaseDate(LocalDate.of(999, 01, 01));
        ResponseEntity<MovieApiResponse> addResponse = addMovie(movie);
        assertEquals(ResponseCode.INVALID_REQUEST, addResponse.getBody().getResponseCode());
        assertEquals(HttpStatus.OK, addResponse.getStatusCode());
    }

    @Test
    void categoriesCantBeEmptyStringTest(){
        Movie movie = getMockMovie();
        Set<String> category = new HashSet<>();
        category.add("");
        movie.setCategories(category);
        ResponseEntity<MovieApiResponse> addResponse = addMovie(movie);

        assertEquals(ResponseCode.INVALID_REQUEST, addResponse.getBody().getResponseCode());
        assertEquals(HttpStatus.OK, addResponse.getStatusCode());
    }

    @Test
    void searchByCategoryTest(){
        Movie movie = getMockMovie();
        Set<String> category = new HashSet<>();
        String cat = UUID.randomUUID().toString();
        category.add(cat);
        movie.setCategories(category);
        addMovie(movie);
        ResponseEntity<MovieApiResponse> findResponse = findMovieByCategory(cat);
        assertEquals(true, findResponse.getBody().getData().get(0).getImdbId().equals(movie.getImdbId()));
    }

    @Test
    void changingReleaseDateClassChangesPriceTest(){
        Movie movie = getMockMovie();
        movie.setTitle("old_movie");
        addMovie(movie);

        movie.setReleaseDate(LocalDate.of(1999, 01, 01));
        ResponseEntity<GeneralApiResponse> updateResponse = updateMovie(movie);

        ResponseEntity<MovieApiResponse> findResponse = findMovieById(movie.getImdbId());

        System.out.println(findResponse.getBody().getData().get(0).getPrice());
        System.out.println(findResponse.getBody().getData().get(0).getPriceClass());
        assertEquals(MoviePriceClass.OLD, findResponse.getBody().getData().get(0).getPriceClass());
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
        System.out.println(response.toString());
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

    public ResponseEntity<GeneralApiResponse> updateMovie(Movie movie) {
        // Update movie
        String updateUrl = "http://localhost:" + port + "/movies/update?id=" + movie.getImdbId();
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

    public ResponseEntity<MovieApiResponse> findMovieByCategory(String cat) {
        String movieUrl = "http://localhost:" + port + "/movies/" + cat + "/";
        ResponseEntity<MovieApiResponse> response = this.restTemplate.getForEntity(movieUrl, MovieApiResponse.class);
        return response;
    }


}

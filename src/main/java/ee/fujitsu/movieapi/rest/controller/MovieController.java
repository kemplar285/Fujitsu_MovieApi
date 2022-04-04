package ee.fujitsu.movieapi.rest.controller;

import ee.fujitsu.movieapi.rest.api.exception.movie.MovieIdNotUniqueException;
import ee.fujitsu.movieapi.rest.api.exception.movie.MovieNotFoundException;
import ee.fujitsu.movieapi.db.model.movie.Movie;
import ee.fujitsu.movieapi.db.repository.MovieRepository;
import ee.fujitsu.movieapi.rest.api.exception.movie.MovieValidationException;
import ee.fujitsu.movieapi.rest.api.response.GeneralApiResponse;
import ee.fujitsu.movieapi.rest.api.response.MovieApiResponse;
import ee.fujitsu.movieapi.rest.api.response.ResponseCode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

@RestController
@RequestMapping(path = "/movies")
public class MovieController {
    private static final Logger logger = LoggerFactory.getLogger(MovieController.class);
    private final MovieRepository movieRepository;

    public MovieController(MovieRepository movieRepository) {
        this.movieRepository = movieRepository;
    }

    /**
     * Gets all movies
     *
     * @return a response entity with list of all movies
     * or http 204 noContent if list is empty
     */
    @GetMapping
    public ResponseEntity<?> findAll() {
        List<Movie> movies = movieRepository.findAll();
        if (movies.size() > 0) {
            MovieApiResponse response = new MovieApiResponse();
            response.setData(movies);
            response.setResponseCode(ResponseCode.OK);
            return new ResponseEntity<>(response, HttpStatus.OK);
        } else {
            GeneralApiResponse response = new GeneralApiResponse();
            response.setResponseCode(ResponseCode.OK);
            response.setMessage("Movies not found");
            return new ResponseEntity<>(response, HttpStatus.OK);
        }
    }

    /**
     * Creates a new movie from json in request body
     *
     * @param movie movie in json format
     * @return new list of movies
     */
    @PostMapping("/add")
    public ResponseEntity<?> addMovie(@RequestBody Movie movie) {
        try {
            MovieApiResponse response = new MovieApiResponse();
            response.setResponseCode(ResponseCode.OK);
            response.setMessage("Movie added.");
            response.setData(List.of(movieRepository.addMovie(movie)));
            return new ResponseEntity<>(response, HttpStatus.OK);

        } catch (IOException | MovieIdNotUniqueException e) {
            GeneralApiResponse response = new GeneralApiResponse();
            response.setResponseCode(ResponseCode.INVALID_REQUEST);
            response.setMessage(e.getMessage());
            return new ResponseEntity<>(response, HttpStatus.OK);
        }
    }

    /**
     * Deletes all movies with specified id
     *
     * @param id Movie imdb id
     * @return responseEntity 'movie deleted', httpstatus ok
     */
    @DeleteMapping("/delete")
    public ResponseEntity<?> deleteMovie(@RequestParam String id) {
        try {
            GeneralApiResponse response = new GeneralApiResponse();
            response.setResponseCode(ResponseCode.OK);
            response.setMessage("Movie deleted");
            movieRepository.deleteMovieFromFile(id);
            return new ResponseEntity<>(response, HttpStatus.OK);

        } catch (MovieNotFoundException | IOException e) {
            GeneralApiResponse response = new GeneralApiResponse();
            response.setResponseCode(ResponseCode.INVALID_REQUEST);
            response.setMessage(e.getMessage());
            return new ResponseEntity<>(response, HttpStatus.OK);
        }
    }

    /**
     * Replaces the movie with provided imdbID with the movie from request body
     *
     * @param id    imdb id
     * @param movie new movie
     * @return ResponseEntity with status code and message
     */
    @PutMapping("/update")
    public ResponseEntity<?> updateMovie(@RequestParam String id, @RequestBody Movie movie) {
        try {
            GeneralApiResponse response = new GeneralApiResponse();
            response.setResponseCode(ResponseCode.OK);
            response.setMessage("Movie updated");
            movieRepository.updateMovie(id, movie);
            return new ResponseEntity<>(response, HttpStatus.OK);

        } catch (IOException | MovieNotFoundException | MovieIdNotUniqueException | MovieValidationException e) {
            GeneralApiResponse response = new GeneralApiResponse();
            response.setResponseCode(ResponseCode.INVALID_REQUEST);
            response.setMessage(e.getMessage());
            return new ResponseEntity<>(response, HttpStatus.OK);
        }
    }

    /**
     * Returns all movies where categories set contains the specified category
     *
     * @param category
     * @return ResponseEntity with movies of that category
     */
    @GetMapping("/{category}")
    public ResponseEntity<?> findMoviesByCategory(@PathVariable String category) {
        category = category.trim().toLowerCase(Locale.ROOT);
        try {
            MovieApiResponse response = new MovieApiResponse();
            response.setData(movieRepository.findMoviesByCategory(category));
            response.setResponseCode(ResponseCode.OK);
            return new ResponseEntity<>(response, HttpStatus.OK);

        } catch (MovieNotFoundException e) {
            GeneralApiResponse response = new GeneralApiResponse();
            response.setResponseCode(ResponseCode.INVALID_REQUEST);
            response.setMessage(e.getMessage());
            return new ResponseEntity<>(response, HttpStatus.OK);
        }
    }

    /**
     * Returns a movie with the specified imdbID
     *
     * @param id imdbID
     * @return movie with the specified imdbID
     * @throws Exception TBA
     */
    @RequestMapping(method = RequestMethod.GET, params = {"id"}, value = {"/id"})
    public ResponseEntity<?> findMovieById(@RequestParam String id) {
        try {
            MovieApiResponse response = new MovieApiResponse();
            response.setResponseCode(ResponseCode.OK);
            response.setData(List.of(movieRepository.findById(id)));
            return new ResponseEntity<>(response, HttpStatus.OK);

        } catch (MovieNotFoundException e) {
            GeneralApiResponse response = new GeneralApiResponse();
            response.setResponseCode(ResponseCode.INVALID_REQUEST);
            response.setMessage(e.getMessage());
            return new ResponseEntity<>(response, HttpStatus.OK);
        }
    }

}

package ee.fujitsu.movieapi.rest.controller;

import ee.fujitsu.movieapi.rest.api.exception.movie.MovieIdNotUniqueException;
import ee.fujitsu.movieapi.rest.api.exception.general.NotFoundException;
import ee.fujitsu.movieapi.db.model.movie.Movie;
import ee.fujitsu.movieapi.db.repository.MovieRepository;
import ee.fujitsu.movieapi.rest.api.exception.movie.MovieValidationException;
import ee.fujitsu.movieapi.rest.api.response.GeneralApiResponse;
import ee.fujitsu.movieapi.rest.api.response.MovieApiResponse;
import ee.fujitsu.movieapi.rest.api.response.ResponseCode;
import ee.fujitsu.movieapi.rest.controller.utils.MovieUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.constraints.Null;
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
    @RequestMapping(value = "", method = RequestMethod.GET, produces="application/json")
    public ResponseEntity<?> findAll() {
        List<Movie> movies = movieRepository.findAll();
        if (movies.size() > 0) {
            MovieApiResponse response = new MovieApiResponse();
            response.setData(movies);
            response.setResponseCode(ResponseCode.OK);
            return new ResponseEntity<>(response, HttpStatus.OK);
        } else {
            GeneralApiResponse response = new GeneralApiResponse(ResponseCode.OK, "Movies not found");
            return new ResponseEntity<>(response, HttpStatus.OK);
        }
    }

    /**
     * Creates a new movie from json in request body
     *
     * @param movie movie in json format
     * @return new list of movies
     */
    @RequestMapping(value="/add", method = RequestMethod.POST, consumes="application/json")
    public ResponseEntity<?> addMovie(@RequestBody Movie movie) {
        try {
            MovieApiResponse response = new MovieApiResponse();
            response.setResponseCode(ResponseCode.OK);
            response.setMessage("Movie added.");
            response.setData(List.of(movieRepository.add(movie)));
            return new ResponseEntity<>(response, HttpStatus.OK);

        } catch (IOException | MovieIdNotUniqueException | MovieValidationException e) {
            GeneralApiResponse response = new GeneralApiResponse(ResponseCode.INVALID_REQUEST, e.getMessage());
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }
    }

    /**
     * Deletes all movies with specified id
     *
     * @param id Movie imdb id
     * @return responseEntity 'movie deleted', httpstatus ok
     */
    @RequestMapping(value="/delete", method = RequestMethod.DELETE, produces = "application/json")
    public ResponseEntity<?> deleteMovie(@RequestParam String id) {
        try {
            GeneralApiResponse response = new GeneralApiResponse();
            response.setResponseCode(ResponseCode.OK);
            response.setMessage("Movie deleted");
            movieRepository.deleteMovieFromFile(id);
            return new ResponseEntity<>(response, HttpStatus.OK);

        } catch (NotFoundException | IOException e) {
            GeneralApiResponse response = new GeneralApiResponse(ResponseCode.INVALID_REQUEST, e.getMessage());
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
    @RequestMapping(value="/update", method = RequestMethod.PUT, consumes="application/json", produces = "application/json")
    public ResponseEntity<?> updateMovie(@RequestParam String id, @RequestBody Movie movie) {
        try {
            MovieUtils.checkNecessaryFieldsPresent(movie);

            GeneralApiResponse response = new GeneralApiResponse();
            response.setResponseCode(ResponseCode.OK);
            response.setMessage("Movie updated");

            movieRepository.updateMovie(id, movie);
            return new ResponseEntity<>(response, HttpStatus.OK);

        } catch (IOException | NotFoundException | MovieIdNotUniqueException | MovieValidationException e) {
            GeneralApiResponse response = new GeneralApiResponse(ResponseCode.INVALID_REQUEST, e.getMessage());
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }
    }

    /**
     * Returns all movies where categories set contains the specified category
     *
     * @param category
     * @return ResponseEntity with movies of that category
     */
    @RequestMapping(value="/{category}", method = RequestMethod.GET, produces = "application/json")
    public ResponseEntity<?> findMoviesByCategory(@PathVariable String category) {
        category = category.trim().toLowerCase(Locale.ROOT);
        try {
            MovieApiResponse response = new MovieApiResponse();
            response.setData(movieRepository.findMoviesByCategory(category));
            response.setResponseCode(ResponseCode.OK);
            return new ResponseEntity<>(response, HttpStatus.OK);

        } catch (NotFoundException e) {
            GeneralApiResponse response = new GeneralApiResponse(ResponseCode.OK, e.getMessage());
            return new ResponseEntity<>(response, HttpStatus.OK);
        }
    }

    /**
     * Returns a movie with the specified imdbID
     *
     * @param id imdbID
     * @return movie with the specified imdbID
     */
    @RequestMapping(value="/id/{id}", method = RequestMethod.GET, produces = "application/json")
    public ResponseEntity<?> findMovieById(@PathVariable String id) {
        try {
            MovieApiResponse response = new MovieApiResponse();
            response.setResponseCode(ResponseCode.OK);
            response.setMessage("Movie found.");
            response.setData(List.of(movieRepository.findById(id)));
            return new ResponseEntity<>(response, HttpStatus.OK);

        } catch (NotFoundException e) {
            GeneralApiResponse response = new GeneralApiResponse(ResponseCode.OK, e.getMessage());
            return new ResponseEntity<>(response, HttpStatus.OK);
        }
    }

}

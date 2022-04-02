package ee.fujitsu.movieapi.controller;

import ee.fujitsu.movieapi.exception.MovieIdNotUniqueException;
import ee.fujitsu.movieapi.exception.MovieNotFoundException;
import ee.fujitsu.movieapi.model.movie.Movie;
import ee.fujitsu.movieapi.repository.MovieRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping(path = "/movies")
public class MovieController {
    private final MovieRepository movieRepository;
    private static final Logger logger = LoggerFactory.getLogger(MovieController.class);

    @Autowired
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
        List<Movie> movies = movieRepository.getMovies();
        if (movies.size() > 0) {
            return new ResponseEntity<>(movies, HttpStatus.OK);
        } else {
            return ResponseEntity.noContent().build();
        }
    }

    /**
     * Creates a new movie from json in request body
     *
     * @param movie movie in json format
     * @return new list of movies
     * @throws IOException TBA
     */
    @PostMapping("/add")
    public ResponseEntity<?> addMovie(@RequestBody Movie movie)  {
        try {
            return new ResponseEntity<>(movieRepository.addMovie(movie), HttpStatus.CREATED);
        }catch (IOException io){
            logger.warn(io.getMessage());
            return ResponseEntity.badRequest().body("Database Error");
        } catch (MovieIdNotUniqueException minue) {
            logger.warn(minue.getMessage());
            return ResponseEntity.badRequest().body("Movie id should be unique");
        }
    }

    /**
     * Deletes all movies with specified id
     *
     * @param id
     * @return responseEntity 'movie deleted', httpstatus ok
     * @throws IOException TBA
     */
    @DeleteMapping("/delete")
    public ResponseEntity<?> deleteMovie(@RequestParam String id){
        try {
            movieRepository.deleteMovieFromFile(id);
            return ResponseEntity.ok("Movie deleted");
        } catch (MovieNotFoundException mnfe) {
            logger.warn(mnfe.getMessage());
            return ResponseEntity.badRequest().body("Movie not found");
        } catch (IOException io) {
            logger.warn(io.getMessage());
            return ResponseEntity.badRequest().body("Request failed");
        }catch (NullPointerException nul){
            logger.warn(nul.getMessage());
            return ResponseEntity.badRequest().body("Id not found");
        }
    }

}

package ee.fujitsu.movieapi.controller;

import ee.fujitsu.movieapi.exception.movie.MovieIdNotUniqueException;
import ee.fujitsu.movieapi.exception.movie.MovieNotFoundException;
import ee.fujitsu.movieapi.exception.movie.MovieValidationException;
import ee.fujitsu.movieapi.model.movie.Movie;
import ee.fujitsu.movieapi.repository.MovieRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

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
        List<Movie> movies = movieRepository.findAll();
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

    /**
     * Replaces the movie with provided imdbID with the movie from request body
     * @param id imdb id
     * @param movie new movie
     * @return ResponseEntity with status code or message
     */
    @PutMapping("/update")
    public ResponseEntity<?> updateMovie(@RequestParam String id, @RequestBody Movie movie){
        try {
            movieRepository.updateMovie(id, movie);
            return ResponseEntity.ok("Movie updated");
        } catch (MovieNotFoundException e) {
            logger.warn(e.getMessage());
            return ResponseEntity.badRequest().body("Movie not found");
        } catch (IOException e) {
            logger.warn(e.getMessage());
            return ResponseEntity.badRequest().body("Movie not found");
        } catch (MovieIdNotUniqueException e) {
            logger.warn(e.getMessage());
            return ResponseEntity.badRequest().body("Movie id should be unique");
        } catch (MovieValidationException e) {
            logger.warn(e.getMessage());
            return ResponseEntity.badRequest().body("Movie body should contain at least 3 values: title, imdbId, releaseDate");
        } catch (HttpMessageNotReadableException e){
            logger.warn(e.getMessage());
            return ResponseEntity.badRequest().body("Bad request");
        }
    }

    /**
     * Returns all movies where categories set contains the specified category
     * @param category
     * @return ResponseEntity with movies of that category
     */
    @GetMapping("/{category}")
    public ResponseEntity<?> findMoviesByCategory(@PathVariable String category){
        category = category.trim().toLowerCase(Locale.ROOT);
        try {
            return new ResponseEntity<>(movieRepository.findMoviesByCategory(category), HttpStatus.OK);
        } catch (MovieNotFoundException e) {
            logger.warn(e.getMessage());
            return ResponseEntity.noContent().build();
        }
    }

    /**
     * Returns a movie with the specified imdbID
     *
     * @param id imdbID
     * @return movie with the specified imdbID
     * @throws Exception TBA
     */
    @RequestMapping(method = RequestMethod.GET, params = {"id"}, value={"/id"})
    public ResponseEntity<?> findMovieById(@RequestParam String id) {
        try {
            return new ResponseEntity<>(movieRepository.findById(id), HttpStatus.OK);
        } catch (MovieNotFoundException e) {
            logger.warn(e.getMessage());
            return ResponseEntity.noContent().build();
        }
    }

}

package ee.fujitsu.movieapi.controller;

import ee.fujitsu.movieapi.model.movie.Movie;
import ee.fujitsu.movieapi.repository.MovieRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(path = "/movies")
public class MovieController {
    private final MovieRepository movieRepository;

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

}

package ee.fujitsu.movieapi.repository;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import ee.fujitsu.movieapi.JavaMovieApiApplication;
import ee.fujitsu.movieapi.configuration.ApiConfiguration;
import ee.fujitsu.movieapi.exception.MovieIdNotUniqueException;
import ee.fujitsu.movieapi.exception.MovieNotFoundException;
import ee.fujitsu.movieapi.model.movie.Movie;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import javax.annotation.PostConstruct;
import javax.validation.constraints.Null;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Repository
public class MovieRepository {
    private static final Logger logger = LoggerFactory.getLogger(MovieRepository.class);
    private final ObjectMapper mapper = new ObjectMapper(new YAMLFactory()).findAndRegisterModules();
    private ApiConfiguration apiConfiguration;
    private File dataFile;
    private List<Movie> movies;

    /**
     * a setter method so that the Spring container can inject a configuration
     */
    @Autowired
    public void setApiConfiguration(ApiConfiguration apiConfiguration) {
        this.apiConfiguration = apiConfiguration;
    }

    @PostConstruct
    public void initializeFields() throws IOException {
        this.dataFile = new File(
                apiConfiguration.getFilePath() +
                        apiConfiguration.getFileName() +
                        apiConfiguration.getFileExtension()
        );
        this.movies = findAllMoviesFromFile();
    }

    /**
     * Reads movies from a db file
     *
     * @return movies - A list of movies
     */
    public List<Movie> findAllMoviesFromFile() throws IOException {
        Movie[] objects = mapper.readValue(dataFile, Movie[].class);
        List<Movie> movies = new ArrayList<>(List.of(objects));
        movies.forEach(movie -> {
            movie.setPriceClass();
            movie.setPrice();
        });
        return movies;
    }

    /**
     * Writes the movies list to file
     *
     * @throws IOException TBA
     */
    public void saveMoviesToFile() throws IOException {
        if (apiConfiguration.getFileExtension().equals(".json")) {
            new ObjectMapper().findAndRegisterModules().writeValue(dataFile, movies);
        } else if (apiConfiguration.getFileExtension().equals(".yaml")) {
            mapper.writeValue(dataFile, movies);
        }
    }

    /**
     * Returns previously loaded movies list
     *
     * @return list of movies
     */
    public List<Movie> getMovies() {
        return movies;
    }

    /**
     * Adds new movie to the list of movies and saves it to file.
     * Then reads from the db again to recalculate prices.
     *
     * @param movie Movie that we want to save
     * @return movies List of movies
     * @throws IOException TBA
     */
    public Movie addMovie(Movie movie) throws IOException, MovieIdNotUniqueException {
        if(!checkUnique(movie.getImdbId())){
            throw new MovieIdNotUniqueException();
        }
        movie.setPriceClass();
        movie.setPrice();
        movies.add(movie);
        saveMoviesToFile();
        return movie;
    }

    /**
     * Checks if there are any movies with the same ID in movies list
     * @param id movie imdb id
     * @return true or false
     * @throws MovieIdNotUniqueException movie id should be unique
     */

    public boolean checkUnique(String id)  {
        for(Movie m : movies){
            if(id.equals(m.getImdbId())){
                return false;
            }
        }
        return true;
    }

    /**
     * Removes movie from the movies list and saves it to file
     *
     * @param id imdb id
     * @throws IOException TBA
     */

    public void deleteMovieFromFile(String id) throws MovieNotFoundException, IOException, NullPointerException {
            movies.stream().filter(movie -> movie.getImdbId().equals(id))
                    .findAny().orElseThrow(MovieNotFoundException::new);
            movies.removeIf(movie -> movie.getImdbId().equals(id));
            saveMoviesToFile();

    }



}

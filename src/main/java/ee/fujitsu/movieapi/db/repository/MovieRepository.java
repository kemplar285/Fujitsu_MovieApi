package ee.fujitsu.movieapi.db.repository;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import ee.fujitsu.movieapi.db.configuration.ApiConfiguration;
import ee.fujitsu.movieapi.db.model.movie.MovieMetadata;
import ee.fujitsu.movieapi.rest.api.exception.movie.MovieIdNotUniqueException;
import ee.fujitsu.movieapi.rest.api.exception.general.NotFoundException;
import ee.fujitsu.movieapi.rest.api.exception.movie.MovieValidationException;
import ee.fujitsu.movieapi.db.model.movie.Movie;
import ee.fujitsu.movieapi.rest.controller.utils.MovieUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Repository;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import javax.annotation.PostConstruct;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Repository
public class MovieRepository implements IRepository<Movie> {
    private static final Logger logger = LoggerFactory.getLogger(MovieRepository.class);
    private final ObjectMapper mapper = new ObjectMapper(new YAMLFactory()).findAndRegisterModules();
    private ApiConfiguration apiConfiguration;
    private File dataFile;
    private List<Movie> movies;

    @Autowired
    public void setApiConfiguration(ApiConfiguration apiConfiguration) {
        this.apiConfiguration = apiConfiguration;
    }

    @PostConstruct
    public void initializeFields() throws IOException {
        this.dataFile = new File(
                apiConfiguration.getFilePath() +
                        apiConfiguration.getMovieFileName() +
                        apiConfiguration.getFileExtension()
        );
        this.movies = findAllFromFile();
    }

    /**
     * Reads movies from a db file
     *
     * @return movies - A list of movies
     */
    @Override
    public List<Movie> findAllFromFile() throws IOException {
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
    @Override
    public void saveToFile() throws IOException {
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
    @Override
    public List<Movie> findAll() {
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
    public Movie add(Movie movie) throws IOException, MovieIdNotUniqueException {
        if (!MovieUtils.checkUnique(movie.getImdbId(), movies)) {
            throw new MovieIdNotUniqueException();
        }
        movie.setPriceClass();
        movie.setPrice();
        movies.add(movie);
        saveToFile();
        return movie;
    }

    /**
     * Removes movie from the movies list and saves it to file
     *
     * @param id imdb id
     * @throws IOException TBA
     */

    public void deleteMovieFromFile(String id) throws NotFoundException, IOException, NullPointerException {
        movies.stream().filter(movie -> movie.getImdbId().equals(id))
                .findAny().orElseThrow(NotFoundException::new);
        movies.removeIf(movie -> movie.getImdbId().equals(id));
        saveToFile();

    }

    /**
     * Replaces the movie with specified id in the movies list
     * with the provided movie object
     *
     * @param id    imdbID of a target
     * @param movie movie to replace the target with
     * @throws IOException TBA
     */
    public void updateMovie(String id, Movie movie) throws IOException, NotFoundException, MovieIdNotUniqueException, MovieValidationException {
        MovieUtils.checkNecessaryFieldsPresent(movie);
        movie.setPriceClass();
        movie.setPrice();
        movies.stream().filter(mov -> mov.getImdbId().equals(id))
                .findAny().orElseThrow(NotFoundException::new);
        movies.replaceAll(mov -> mov.getImdbId().equals(id) ? movie : mov);
        saveToFile();
    }

    /**
     * Returns a list of movies where categories field contains the specified category
     *
     * @param categoryName the category you want to display
     * @return a list of movies
     */
    public List<Movie> findMoviesByCategory(String categoryName) throws NotFoundException {
        List<Movie> moviesToReturn = movies.stream()
                .filter(s -> s.getCategories().stream()
                        .anyMatch(categoryName::equalsIgnoreCase))
                .collect(Collectors.toList());
        if (moviesToReturn.size() > 0) {
            return moviesToReturn;
        } else {
            throw new NotFoundException();
        }

    }

    /**
     * Finds a movie by imdb id. If id is found in imdb, fetches additional metadata.
     *
     * @param id IMDB id
     * @return first movie with this imdb id in the file
     * @throws NotFoundException movie not found
     */
    @Override
    public Movie findById(String id) throws NotFoundException {
        String url = apiConfiguration.getOmdbUrl()
                + "?apikey=" + apiConfiguration.getApiKey()
                + "&i=" + id;
        RestTemplate restTemplate = new RestTemplate();
        MovieMetadata metadata = restTemplate.getForObject(url, MovieMetadata.class);
        return movies.stream().filter(movie -> movie.getImdbId().equals(id))
                .peek(movie -> movie.setMovieMetadata(metadata))
                .findFirst().orElseThrow(NotFoundException::new);
    }


}

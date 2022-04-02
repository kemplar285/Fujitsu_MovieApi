package ee.fujitsu.movieapi.repository;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import ee.fujitsu.movieapi.configuration.ApiConfiguration;
import ee.fujitsu.movieapi.model.movie.Movie;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import javax.annotation.PostConstruct;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Repository
public class MovieRepository {
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
     * Returns previously loaded movies list
     *
     * @return list of movies
     */
    public List<Movie> getMovies() {
        return movies;
    }
}

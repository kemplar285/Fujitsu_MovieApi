package ee.fujitsu.movieapi;
import ee.fujitsu.movieapi.db.configuration.ApiConfiguration;
import ee.fujitsu.movieapi.db.model.movie.Movie;
import ee.fujitsu.movieapi.db.repository.MovieRepository;
import ee.fujitsu.movieapi.rest.api.exception.general.NotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

@SpringBootApplication
public class JavaMovieApiApplication {
	private final MovieRepository movieRepository;
	private static final Logger logger = LoggerFactory.getLogger(JavaMovieApiApplication.class);

	public JavaMovieApiApplication(MovieRepository movieRepository ) {
		this.movieRepository = movieRepository;
	}

	public static void main(String[] args) {
		ConfigurableApplicationContext context = SpringApplication.run(JavaMovieApiApplication.class, args);
		ApiConfiguration apiConfig = context.getBean(ApiConfiguration.class);
		logger.info("Using movie database file: " + apiConfig.getMovieFileName() + apiConfig.getFileExtension());
		logger.info("Using order database file: " + apiConfig.getOrderFileName() + apiConfig.getFileExtension());
	}

	@Bean
	InitializingBean testData() {
		return () -> {
			try{
				Movie movie = movieRepository.findById("testId");
			} catch (NotFoundException e){
				Movie movie = new Movie();
				movie.setImdbId("testId");
				movie.setTitle("testTitle");
				movie.setCategories(new HashSet<String>(Arrays.asList("Test")));
				movie.setReleaseDate(LocalDate.of(1999, 10, 10));
				movieRepository.add(movie);
			}
		};
	}
}
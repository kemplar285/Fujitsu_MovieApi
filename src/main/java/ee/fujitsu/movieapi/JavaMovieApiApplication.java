package ee.fujitsu.movieapi;
import ee.fujitsu.movieapi.db.configuration.ApiConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

@SpringBootApplication
public class JavaMovieApiApplication {
	private static final Logger logger = LoggerFactory.getLogger(JavaMovieApiApplication.class);

	public static void main(String[] args) {
		ConfigurableApplicationContext context = SpringApplication.run(JavaMovieApiApplication.class, args);
		ApiConfiguration apiConfig = context.getBean(ApiConfiguration.class);
		logger.info("Using database file: " + apiConfig.getMovieFileName() + apiConfig.getFileExtension());
	}
}
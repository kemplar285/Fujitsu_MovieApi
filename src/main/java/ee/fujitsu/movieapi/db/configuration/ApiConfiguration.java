package ee.fujitsu.movieapi.db.configuration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ApiConfiguration {
    @Value("${db.fileExtension}")
    private String fileExtension;
    @Value("${db.filePath}")
    private String filePath;
    @Value("${db.movieDbFileName}")
    private String movieFileName;
    @Value("${db.orderDbFileName}")
    private String orderFileName;
    @Value("${omdb.apiKey}")
    private String apiKey;

    public String getFileExtension() {
        return fileExtension;
    }

    public String getFilePath() {
        return filePath;
    }

    public String getMovieFileName() {
        return movieFileName;
    }

    public String getApiKey() {
        return apiKey;
    }

    public String getOrderFileName() {
        return orderFileName;
    }
}

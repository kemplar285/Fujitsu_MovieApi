package ee.fujitsu.movieapi.configuration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ApiConfiguration {
    @Value("${db.fileExtension}")
    private String fileExtension;
    @Value("${db.filePath}")
    private String filePath;
    @Value("${db.fileName}")
    private String fileName;
    @Value("${omdb.apiKey}")
    private String apiKey;

    public String getFileExtension() {
        return fileExtension;
    }

    public String getFilePath() {
        return filePath;
    }

    public String getFileName() {
        return fileName;
    }

    public String getApiKey() {
        return apiKey;
    }
}

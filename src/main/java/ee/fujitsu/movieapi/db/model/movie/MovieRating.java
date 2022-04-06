package ee.fujitsu.movieapi.db.model.movie;

import com.fasterxml.jackson.annotation.JsonProperty;

public class MovieRating {
    @JsonProperty("Source")
    private String source;
    @JsonProperty("Value")
    private String value;

    @JsonProperty("Source")
    public String getSource() {
        return source;
    }
    @JsonProperty("Source")
    public void setSource(String source) {
        this.source = source;
    }
    @JsonProperty("Value")
    public String getValue() {
        return value;
    }
    @JsonProperty("Value")
    public void setValue(String value) {
        this.value = value;
    }
}
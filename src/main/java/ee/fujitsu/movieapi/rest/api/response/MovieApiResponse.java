package ee.fujitsu.movieapi.rest.api.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.MoreObjects;
import ee.fujitsu.movieapi.db.model.movie.Movie;

import javax.validation.constraints.NotNull;
import java.util.List;
@JsonIgnoreProperties
@JsonInclude( JsonInclude.Include.NON_NULL )
public class MovieApiResponse extends AbstractResponse{
    @JsonProperty
    @NotNull
    List<Movie> data;

    public MovieApiResponse() {

    }

    public List<Movie> getData() {
        return data;
    }

    public void setData(List<Movie> data) {
        this.data = data;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper( this ).omitNullValues()
                .addValue(super.toString())
                .add("movieData", data)
                .toString();
    }
}

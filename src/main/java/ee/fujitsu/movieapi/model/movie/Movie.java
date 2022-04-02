package ee.fujitsu.movieapi.model.movie;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDate;
import java.util.Set;

public class Movie {
    public String imdbId;
    public String Title;
    @JsonProperty("releaseDate") @JsonFormat(pattern = "dd.MM.yyyy")
    public LocalDate releaseDate;
    private Set<String> categories;
    private MoviePriceClass priceClass;
    private double price;

}

package ee.fujitsu.movieapi.model.movie;

import com.fasterxml.jackson.annotation.*;

import java.time.LocalDate;
import java.util.Set;


@JsonPropertyOrder({ "imdbId", "title", "releaseDate", "categories", "priceClass", "price" })
@JsonInclude(JsonInclude.Include.NON_DEFAULT)
@JsonIgnoreProperties(ignoreUnknown = true)
public class Movie {
    public String imdbId;
    public String title;
    @JsonProperty("releaseDate") @JsonFormat(pattern = "dd.MM.yyyy")
    public LocalDate releaseDate;
    private Set<String> categories;
    private MoviePriceClass priceClass;
    private double price;

    public String getImdbId() {
        return imdbId;
    }

    public void setImdbId(String imdbId) {
        this.imdbId = imdbId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public LocalDate getReleaseDate() {
        return releaseDate;
    }

    public void setReleaseDate(LocalDate releaseDate) {
        this.releaseDate = releaseDate;
    }

    public Set<String> getCategories() {
        return categories;
    }

    public void setCategories(Set<String> categories) {
        this.categories = categories;
    }

    public MoviePriceClass getPriceClass() {
        return priceClass;
    }

    /**
     * Automatically sets price class basing on movie's release date
     */
    public void setPriceClass() {
        this.priceClass = MoviePriceClass.getMoviePriceClass(this.releaseDate);
    }

    public double getPrice() {
        return price;
    }

    /**
     * Automatically sets price basing on movie's price class
     */
    public void setPrice() {
        switch (getPriceClass()) {
            case NEW:
                this.price = 5.0;
                break;
            case REGULAR:
                this.price = 3.49;
                break;
            case OLD:
                this.price = 1.99;
                break;
        }
    }
}

package ee.fujitsu.movieapi.db.model.movie;

import com.fasterxml.jackson.annotation.*;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import ee.fujitsu.movieapi.db.model.BigDecimalSerializer;

import java.math.BigDecimal;
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
    @JsonSerialize(using = BigDecimalSerializer.class)
    private BigDecimal price;
    public MovieMetadata movieMetadata;


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

    public BigDecimal getPrice() {
        return price;
    }

    /**
     * Automatically sets price basing on movie's price class
     */
    public void setPrice() {
        switch (getPriceClass()) {
            case NEW:
                this.price = (BigDecimal.valueOf(MoviePriceClass.PriceClassConstants.NEW_PRICE));
                break;
            case REGULAR:
                this.price = (BigDecimal.valueOf(MoviePriceClass.PriceClassConstants.REGULAR_PRICE));
                break;
            case OLD:
                this.price = (BigDecimal.valueOf(MoviePriceClass.PriceClassConstants.OLD_PRICE));
                break;
        }
    }
}

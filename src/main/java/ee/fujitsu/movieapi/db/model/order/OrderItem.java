package ee.fujitsu.movieapi.db.model.order;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import ee.fujitsu.movieapi.db.model.BigDecimalSerializer;
import ee.fujitsu.movieapi.db.model.movie.MoviePriceClass;

import java.math.BigDecimal;
import java.time.LocalDate;

public class OrderItem {
    private String movieId;
    private LocalDate movieReleaseDate;
    private int rentDurationInWeeks;
    @JsonSerialize(using = BigDecimalSerializer.class)
    private BigDecimal currentPricePerWeek;
    @JsonSerialize(using = BigDecimalSerializer.class)
    private BigDecimal totalPrice = BigDecimal.valueOf(0);

    public LocalDate getMovieReleaseDate() {
        return movieReleaseDate;
    }

    public void setMovieReleaseDate(LocalDate movieReleaseDate) {
        this.movieReleaseDate = movieReleaseDate;
    }

    public String getMovieId() {
        return movieId;
    }

    public void setMovieId(String movieId) {
        this.movieId = movieId;
    }

    public int getRentDurationInWeeks() {
        return rentDurationInWeeks;
    }

    public void setRentDurationInWeeks(int rentDurationInWeeks) {
        this.rentDurationInWeeks = rentDurationInWeeks;
    }

    public BigDecimal getCurrentPricePerWeek() {
        return currentPricePerWeek;
    }

    public void setCurrentPricePerWeek(BigDecimal currentPricePerWeek) {
        this.currentPricePerWeek = currentPricePerWeek;
    }

    public BigDecimal getTotalPrice() {
        return totalPrice;
    }

    public void setTotalPrice(BigDecimal totalPrice) {
        this.totalPrice = totalPrice;
    }

    public void calculateTotalPrice() {
        this.totalPrice = MoviePriceClass.calculateTotalPrice(movieReleaseDate, rentDurationInWeeks);
    }
}

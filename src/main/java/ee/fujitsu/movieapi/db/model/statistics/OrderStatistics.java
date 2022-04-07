package ee.fujitsu.movieapi.db.model.statistics;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.HashMap;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_DEFAULT)
@JsonIgnoreProperties(ignoreUnknown = true)
public class OrderStatistics {
    private Map<String, Integer> movieOrderCount = new HashMap<>();
    private Map<String, Integer> movieRentedFor = new HashMap<>();

    public Map<String, Integer> getMovieOrderCount() {
        return movieOrderCount;
    }

    public void setMovieOrderCount(Map<String, Integer> movieOrderCount) {
        this.movieOrderCount = movieOrderCount;
    }

    public Map<String, Integer> getMovieRentedFor() {
        return movieRentedFor;
    }

    public void setMovieRentedFor(Map<String, Integer> movieRentedFor) {
        this.movieRentedFor = movieRentedFor;
    }

    public void addToOrderCount(String movieId, Integer value){
        movieOrderCount.putIfAbsent(movieId, 0);
        System.out.println(movieOrderCount.get(movieId));
        movieOrderCount.put(movieId, movieOrderCount.get(movieId) + value);
    }
    public void addToRentedFor(String movieId, Integer value){
        movieRentedFor.putIfAbsent(movieId, 0);
        movieRentedFor.put(movieId, movieRentedFor.get(movieId).intValue() + value);
    }

    public void removeFromOrderCount(String movieId){
        movieOrderCount.remove(movieId);
    }

    public  void removeFromRentedFor(String movieId){
        movieRentedFor.remove(movieId);
    }
}

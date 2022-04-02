package ee.fujitsu.movieapi.utils;

import ee.fujitsu.movieapi.exception.movie.MovieValidationException;
import ee.fujitsu.movieapi.model.movie.Movie;

import java.util.List;

public class MovieUtils {
    /**
     * Checks if the provided id is not present in the list
     *
     * @param id id to check
     * @param movies list for searching
     * @return true or false
     */
    public static boolean checkUnique(String id, List<Movie> movies) {
        for (Movie m : movies) {
            if (id.equals(m.getImdbId())) {
                return false;
            }
        }
        return true;
    }

    /**
     * Checks if 3 main fields are present: ImdbID, ReleaseDate, Title
     * @param movie movie to check
     * @return true or false
     */
    public static boolean checkNecessaryFieldsPresent(Movie movie) throws MovieValidationException {
        if(movie.getImdbId() != null
                && movie.getReleaseDate() != null
                && movie.getTitle() != null){
            return true;

        } else{
            throw new MovieValidationException("All necessary values should be specified");
        }

    }

    /**
     * Combines check methods together
     * @param movie Movie to validate
     * @param movies Movie list to confirm uniqueness
     * @return true or false
     */
    public static boolean validateMovie(Movie movie, List<Movie> movies) throws MovieValidationException {
        if(checkUnique(movie.getImdbId(), movies) && checkNecessaryFieldsPresent(movie)){
            return true;
        } else{
            throw new MovieValidationException("Movie validation failed!");
        }

    }
}

package ee.fujitsu.movieapi.db.model.movie;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

public enum MoviePriceClass {
    NEW, REGULAR, OLD;


    /**
     * Calculates movie price class with given release date
     *
     * @param releaseDate movie release date
     * @return enum movie price class
     */
    public static MoviePriceClass getMoviePriceClass(LocalDate releaseDate) throws NullPointerException{
        LocalDate today = LocalDate.now();
        long weeks = ChronoUnit.WEEKS.between(releaseDate, today);
        if (!releaseDate.isAfter(today)) {
            if (weeks <= 52) {
                return NEW;
            } else if ((weeks > 52) && (weeks < 156)) {
                return REGULAR;
            } else {
                return OLD;
            }
        } else {
            return NEW;
        }
    }
}

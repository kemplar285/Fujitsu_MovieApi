package ee.fujitsu.movieapi.db.model.movie;

import ee.fujitsu.movieapi.db.model.BigDecimalSerializer;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

public enum MoviePriceClass {
    NEW, REGULAR, OLD;

    public static class PriceClassConstants {
        public static final int WEEKS_NEW = 52;
        public static final int WEEKS_REGULAR = 162;
        public static final double NEW_PRICE = 5.0;
        public static final double REGULAR_PRICE = 3.49;
        public static final double OLD_PRICE = 1.99;
    }

    /**
     * Calculates movie price class with given release date
     *
     * @param releaseDate movie release date
     * @return enum movie price class
     */
    public static MoviePriceClass getMoviePriceClass(LocalDate releaseDate) throws NullPointerException{
        LocalDate today = LocalDate.now();
        Long weeks = ChronoUnit.WEEKS.between(releaseDate, today);
        if (!releaseDate.isAfter(today)) {
            if (weeks <= PriceClassConstants.WEEKS_NEW) {
                return NEW;
            } else if ( (weeks < PriceClassConstants.WEEKS_REGULAR)) {
                return REGULAR;
            } else {
                return OLD;
            }
        } else {
            return NEW;
        }
    }

    public static BigDecimal calculateTotalPrice(LocalDate releaseDate, int rentWeeks){
        LocalDate orderDate = LocalDate.now();
        long releasedWeeksAgo = ChronoUnit.WEEKS.between(releaseDate, orderDate);
        long weeksEndOfRent = releasedWeeksAgo + rentWeeks;
        double total = 0;
        for(long i = weeksEndOfRent; i < weeksEndOfRent+rentWeeks; i++){
            if(i <= PriceClassConstants.WEEKS_NEW){
                total += PriceClassConstants.NEW_PRICE;
            }else if(i < PriceClassConstants.WEEKS_REGULAR){
                total += PriceClassConstants.REGULAR_PRICE;
            } else{
                total += PriceClassConstants.OLD_PRICE;
            }
        }
        return BigDecimal.valueOf(total);
    }
}

package ee.fujitsu.movieapi.rest.api.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import ee.fujitsu.movieapi.db.model.statistics.OrderStatistics;

import javax.validation.constraints.NotNull;

@JsonIgnoreProperties
@JsonInclude( JsonInclude.Include.NON_NULL )
public class OrderStatisticsApiResponse extends AbstractResponse{
    @NotNull
    private OrderStatistics orderStatistics;

    public OrderStatistics getOrderStatistics() {
        return orderStatistics;
    }

    public void setOrderStatistics(OrderStatistics orderStatistics) {
        this.orderStatistics = orderStatistics;
    }
}

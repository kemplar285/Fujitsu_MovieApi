package ee.fujitsu.movieapi.rest.api.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.MoreObjects;
import ee.fujitsu.movieapi.db.model.order.Order;

import javax.validation.constraints.NotNull;
import java.util.List;

@JsonIgnoreProperties
@JsonInclude( JsonInclude.Include.NON_NULL )
public class OrderApiResponse extends AbstractResponse{
    @JsonProperty
    @NotNull
    List<Order> data;

    public OrderApiResponse() {

    }

    public List<Order> getData() {
        return data;
    }

    public void setData(List<Order> data) {
        this.data = data;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper( this ).omitNullValues()
                .addValue(super.toString())
                .add("orderData", data)
                .toString();
    }
}

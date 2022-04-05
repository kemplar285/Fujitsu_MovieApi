package ee.fujitsu.movieapi.db.model.order;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import ee.fujitsu.movieapi.db.model.BigDecimalSerializer;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Order {
    private String orderId;
    private List<OrderItem> orderItemList = new ArrayList<>();
    private LocalDateTime timestamp = LocalDateTime.now();
    private OrderStatus orderStatus = OrderStatus.OPEN;
    @JsonSerialize(using = BigDecimalSerializer.class)
    private BigDecimal totalPrice = BigDecimal.valueOf(0);

    public Order(List<OrderItem> orderItemList) {
        this.orderItemList = orderItemList;
    }

    public Order() {
    }

    public String getOrderId() {
        return orderId;
    }

    public void generateOrderId(){
        UUID uuid = UUID.randomUUID();
        this.orderId = uuid.toString();
    }

    public OrderStatus getOrderStatus() {
        return orderStatus;
    }

    public void setOrderStatus(OrderStatus orderStatus) {
        this.orderStatus = orderStatus;
    }

    public List<OrderItem> getOrderItemList() {
        return orderItemList;
    }

    public void setOrderItemList(List<OrderItem> orderItemList) {
        this.orderItemList = orderItemList;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public BigDecimal getTotalPrice() {
        return totalPrice;
    }

    public void setTotalPrice(BigDecimal totalPrice) {
        this.totalPrice = totalPrice;
    }

    public void calculateTotalPrice() {
        for(OrderItem item : orderItemList){
            this.totalPrice.add(item.getTotalPrice());
        }
    }

    public void addToOrderItems(OrderItem orderItem) {
        this.orderItemList.add(orderItem);
    }
}

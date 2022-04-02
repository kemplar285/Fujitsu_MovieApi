package ee.fujitsu.movieapi.model.order;

import java.time.LocalDateTime;
import java.util.List;

public class Order {
    private int orderId;
    private List<OrderItem> orderItemList;
    private LocalDateTime timestamp = LocalDateTime.now();
    private boolean confirmed = false;
    private double totalPrice = 0;

    public Order(int orderId, List<OrderItem> orderItemList){
        this.orderId = orderId;
        this.orderItemList = orderItemList;
    }

    public int getOrderId() {
        return orderId;
    }

    public void setOrderId(int orderId) {
        this.orderId = orderId;
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

    public boolean isConfirmed() {
        return confirmed;
    }

    public void setConfirmed(boolean confirmed) {
        this.confirmed = confirmed;
    }

    public double getTotalPrice() {
        return totalPrice;
    }

    public void setTotalPrice() {

    }

    public void setTotalPrice(double totalPrice) {
        this.totalPrice = totalPrice;
    }
}

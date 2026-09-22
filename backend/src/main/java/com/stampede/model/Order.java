package com.stampede.model;

public class Order {
    private String orderId;     // unique id for this order
    private String personId;    // who placed the order
    private String productId;   // what they bought (V1: single product per order)
    private OrderStatus orderStatus;

    public Order(String orderId, String personId, String productId, OrderStatus orderStatus) {
        this.orderId = orderId;
        this.personId = personId;
        this.productId = productId;
        this.orderStatus = orderStatus;
    }

    public String getOrderId()          { return orderId; }
    public String getPersonId()         { return personId; }
    public String getProductId()        { return productId; }
    public OrderStatus getOrderStatus() { return orderStatus; }

    public void setOrderStatus(OrderStatus orderStatus) {
        this.orderStatus = orderStatus;
    }
}

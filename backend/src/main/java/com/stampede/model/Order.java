package com.stampede.model;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "orders")
public class Order {

    @Id
    private String orderId;

    private String personId;

    @Enumerated(EnumType.STRING)
    private OrderStatus orderStatus;

    private Instant createdAt;

    private long totalAmount;   // sum of (priceAtPurchase * quantity) across items, in paisa

    // one order has many line items; cascade so saving the order saves its items
    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private List<OrderItem> items = new ArrayList<>();

    protected Order() {
        // default constructor for JPA
    }

    public Order(String orderId, String personId) {
        this.orderId = orderId;
        this.personId = personId;
        this.orderStatus = OrderStatus.PENDING;
        this.createdAt = Instant.now();
        this.totalAmount = 0L;
    }

    // links an item to this order (both sides) and updates the running total
    public void addItem(OrderItem item) {
        item.setOrder(this);
        this.items.add(item);
        this.totalAmount += item.getPriceAtPurchase() * item.getQuantity();
    }

    public String getOrderId()          { return orderId; }
    public String getPersonId()         { return personId; }
    public OrderStatus getOrderStatus() { return orderStatus; }
    public Instant getCreatedAt()       { return createdAt; }
    public long getTotalAmount()        { return totalAmount; }
    public List<OrderItem> getItems()   { return items; }

    public void setOrderStatus(OrderStatus orderStatus) {
        this.orderStatus = orderStatus;
    }
}

package com.stampede.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

/**
 * One line of an order: a product, a quantity, and the price paid per unit.
 * The price is snapshotted at purchase time so later price changes don't
 * rewrite historical orders.
 */
@Entity
@Table(name = "order_items")
public class OrderItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // back-reference to the owning order; ignored in JSON to avoid infinite recursion
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id")
    @JsonIgnore
    private Order order;

    private String productId;
    private int quantity;
    private long priceAtPurchase;   // per-unit price in paisa, snapshotted at order time

    protected OrderItem() {
        // default constructor for JPA
    }

    public OrderItem(String productId, int quantity, long priceAtPurchase) {
        this.productId = productId;
        this.quantity = quantity;
        this.priceAtPurchase = priceAtPurchase;
    }

    public Long getId()             { return id; }
    public Order getOrder()         { return order; }
    public String getProductId()    { return productId; }
    public int getQuantity()        { return quantity; }
    public long getPriceAtPurchase(){ return priceAtPurchase; }

    public void setOrder(Order order) {
        this.order = order;
    }
}

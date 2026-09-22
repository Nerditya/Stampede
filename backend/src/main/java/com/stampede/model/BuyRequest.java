package com.stampede.model;

public class BuyRequest {
    private String personId;
    private String productId;
    private int quantity;
    public BuyRequest() {
        // default constructor for deserialization
    }
    public BuyRequest(String personId, String productId, int quantity) {
        this.personId = personId;
        this.productId = productId;
        this.quantity = quantity;
    }
    public String getPersonId() { return personId; }
    public String getProductId() { return productId; }
    public int getQuantity() { return quantity; }

}

package com.stampede.dto;

import jakarta.validation.constraints.Min;

public class RestockRequest {

    @Min(1)
    private int quantity;

    public RestockRequest() {
    }

    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }
}
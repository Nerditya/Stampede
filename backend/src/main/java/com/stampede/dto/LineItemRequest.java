package com.stampede.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

/** One line in a buy request: which product and how many units. */
public class LineItemRequest {

    @NotBlank
    private String productId;

    @Min(1)
    private int quantity;

    public LineItemRequest() {
    }

    public LineItemRequest(String productId, int quantity) {
        this.productId = productId;
        this.quantity = quantity;
    }

    public String getProductId() { return productId; }
    public int getQuantity()     { return quantity; }

    public void setProductId(String productId) { this.productId = productId; }
    public void setQuantity(int quantity)      { this.quantity = quantity; }
}

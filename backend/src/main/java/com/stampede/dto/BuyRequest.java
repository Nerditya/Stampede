package com.stampede.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

/**
 * A cart checkout: one or more line items. The buyer's identity is taken from
 * the authenticated token, not the request body.
 */
public class BuyRequest {

    @NotEmpty
    @Valid
    private List<LineItemRequest> items;

    public BuyRequest() {
    }

    public BuyRequest(List<LineItemRequest> items) {
        this.items = items;
    }

    public List<LineItemRequest> getItems()          { return items; }
    public void setItems(List<LineItemRequest> items) { this.items = items; }
}

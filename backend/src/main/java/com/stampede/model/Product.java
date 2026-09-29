package com.stampede.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;

@Entity 
public class Product {
    @Id
    private String productId;
    private String name;
    private String description;
    private String imageUrl;
    private long price;       // stored in paisa
    private int initialStock; // starting stock count, used for seeding liveStock
    private int liveStock;    // current stock count, updated in real-time
    protected Product() {
        // default constructor for JPA
    }
    public Product(String productId, String name, String description, String imageUrl, long price, int initialStock) {
        this.productId = productId;
        this.name = name;
        this.description = description;
        this.imageUrl = imageUrl;
        this.price = price;
        this.initialStock = initialStock;
        this.liveStock = initialStock;
    }

    public String getProductId()   { return productId; }
    public String getName()        { return name; }
    public String getDescription() { return description; }
    public String getImageUrl()    { return imageUrl; }
    public long   getPrice()       { return price; }
    public int    getInitialStock(){ return initialStock; }
    public int    getLiveStock()   { return liveStock; }
}

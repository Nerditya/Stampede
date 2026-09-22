package com.stampede.model;

public class Product {
    private String name;
    private String description;
    private String productId;
    private String imageUrl;
    private long price;       // stored in paisa
    private int initialStock; // starting stock count, used for seeding liveStock

    public Product(String productId, String name, String description, String imageUrl, long price, int initialStock) {
        this.productId = productId;
        this.name = name;
        this.description = description;
        this.imageUrl = imageUrl;
        this.price = price;
        this.initialStock = initialStock;
    }

    public String getProductId()   { return productId; }
    public String getName()        { return name; }
    public String getDescription() { return description; }
    public String getImageUrl()    { return imageUrl; }
    public long   getPrice()       { return price; }
    public int    getInitialStock(){ return initialStock; }
}

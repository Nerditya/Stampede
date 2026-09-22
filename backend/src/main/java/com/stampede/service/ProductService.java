package com.stampede.service;

import com.stampede.model.Product;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@Service
public class ProductService {

    private final Map<String, Product> catalog = new ConcurrentHashMap<>();
    private final Map<String, AtomicInteger> liveStock = new ConcurrentHashMap<>();
    public Product getProduct(String productId) {
        return catalog.get(productId);
    }
    public int getLiveStock(String productId) {
        AtomicInteger stock = liveStock.get(productId);
        return stock != null ? stock.get() : 0;
    }
    public boolean decrementStock(String productId, int quantity){
        int current;
        AtomicInteger stock = liveStock.get(productId);
        if(stock==null) return false;
        do{
            current= stock.get();
            if(current<quantity) return false;
        }
        while(!stock.compareAndSet(current, current-quantity));
        return true;
    }
    public List<Product> getAllProducts() {
        return new ArrayList<>(catalog.values());
    }
    @PostConstruct
    public void seed() {
        List<Product> products = List.of(
            new Product("p1", "Nike Air Max",   "Limited edition kicks",          "nike.jpg",   14999L, 100),
            new Product("p2", "Adidas Boost",   "Ultracomfort running shoe",      "adidas.jpg", 12999L,  50),
            new Product("p3", "Puma Suede",     "Classic street style",           "puma.jpg",    9999L,  25)
        );
        for (Product p : products) {
            catalog.put(p.getProductId(), p);
            liveStock.put(p.getProductId(), new AtomicInteger(p.getInitialStock()));
        }
    }
}

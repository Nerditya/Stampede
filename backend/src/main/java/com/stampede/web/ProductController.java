package com.stampede.web;


import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.stampede.model.Product;
import com.stampede.service.ProductService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
@RestController 
@RequestMapping ("/api")
public class ProductController {
    private final ProductService productService;
    public ProductController(ProductService productService) {
        this.productService = productService;
    }
    @GetMapping("/products")
    public ResponseEntity<?> getAllProducts() {
        List<Product> products = productService.getAllProducts();
        // attach live stock to each product in the response
        List<Map<String, Object>> response = products.stream()
            .map(p -> toResponse(p))
            .toList();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/products/{productId}")
    public ResponseEntity<?> getProduct(@PathVariable String productId) {
        var product = productService.getProduct(productId);
        if (product == null) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(toResponse(product));
    }

    // combines static product data with live stock count into one response object
    private Map<String, Object> toResponse(Product p) {
        Map<String, Object> map = new HashMap<>();
        map.put("productId",    p.getProductId());
        map.put("name",         p.getName());
        map.put("description",  p.getDescription());
        map.put("imageUrl",     p.getImageUrl());
        map.put("price",        p.getPrice());
        map.put("liveStock",    productService.getLiveStock(p.getProductId()));
        return map;
    }
    
}
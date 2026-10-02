package com.stampede.web;

import com.stampede.dto.RestockRequest;
import com.stampede.model.Product;
import com.stampede.service.ProductService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import jakarta.validation.Valid;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class ProductController {

    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @GetMapping("/products")
    public ResponseEntity<?> getAllProducts() {
        List<Map<String, Object>> response = productService.getAllProducts().stream()
                .map(this::toResponse)
                .toList();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/products/{productId}")
    public ResponseEntity<?> getProduct(@PathVariable String productId) {
        Product product = productService.getProduct(productId);
        if (product == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(toResponse(product));
    }

    @PostMapping("/products/{productId}/restock")
    public ResponseEntity<?> restock(@PathVariable String productId,
                                     @Valid @RequestBody RestockRequest request) {
        Product product = productService.restock(productId, request.getQuantity());
        if (product == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(toResponse(product));
    }

    private Map<String, Object> toResponse(Product p) {
        Map<String, Object> map = new HashMap<>();
        map.put("productId",    p.getProductId());
        map.put("name",         p.getName());
        map.put("description",  p.getDescription());
        map.put("imageUrl",     p.getImageUrl());
        map.put("price",        p.getPrice());
        map.put("initialStock", p.getInitialStock());
        map.put("liveStock",    p.getLiveStock());
        return map;
    }
}

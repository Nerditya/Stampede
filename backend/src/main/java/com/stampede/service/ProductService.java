package com.stampede.service;

import com.stampede.model.Product;
import com.stampede.repository.ProductRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ProductService {

    private final ProductRepository productRepository;

    public ProductService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    public Product getProduct(String productId) {
        return productRepository.findById(productId).orElse(null);
    }

    public List<Product> getAllProducts() {
        return productRepository.findAll();
    }

    public int getLiveStock(String productId) {
        Product product = getProduct(productId);
        return product != null ? product.getLiveStock() : 0;
    }

    /**
     * Atomically decrements stock via a single conditional UPDATE.
     * Returns true only if a row was actually changed (enough stock existed).
     */
    @Transactional
    public boolean decrementStock(String productId, int quantity) {
        return productRepository.decrementStock(productId, quantity) == 1;
    }

    @Transactional
    public Product restock(String productId, int quantity) {
        if (productRepository.restock(productId, quantity) != 1) {
            return null;
        }
        return productRepository.findById(productId).orElse(null);
    }
}

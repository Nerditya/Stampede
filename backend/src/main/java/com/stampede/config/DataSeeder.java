package com.stampede.config;

import com.stampede.model.Product;
import com.stampede.repository.ProductRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Seeds the product catalog once, on first startup. If products already exist the
 * seeder does nothing — so restarts preserve the current (sold-down) stock rather
 * than resetting it. This is the whole point of moving from RAM to a database.
 */
@Component
public class DataSeeder implements CommandLineRunner {

    private final ProductRepository productRepository;

    public DataSeeder(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    @Override
    public void run(String... args) {
        if (productRepository.count() > 0) {
            return;   // already seeded — leave existing stock untouched
        }

        List<Product> products = List.of(
                new Product("p1", "Nike Air Max", "Limited edition kicks",     "nike.jpg",   14999L, 100),
                new Product("p2", "Adidas Boost", "Ultracomfort running shoe", "adidas.jpg", 12999L,  50),
                new Product("p3", "Puma Suede",   "Classic street style",      "puma.jpg",    9999L,  25)
        );
        productRepository.saveAll(products);
    }
}

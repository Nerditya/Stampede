package com.stampede.repository;

import com.stampede.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ProductRepository extends JpaRepository<Product, String> {

    @Modifying
    @Query("UPDATE Product p SET p.liveStock = p.liveStock - :quantity " +
           "WHERE p.productId = :productId AND p.liveStock >= :quantity")
    int decrementStock(@Param("productId") String productId,
                       @Param("quantity") int quantity);
}

package com.stampede.service;

import com.stampede.model.Order;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentLinkedQueue;
@Service
public class OrderService {

    private final ProductService productService;
    private final ConcurrentLinkedQueue<Order> ordersFinal = new ConcurrentLinkedQueue<>();

    public OrderService(ProductService productService) {
        this.productService = productService;
    }
    public boolean placeOrder(String personId, String productId, int quantity){
        if(productService.decrementStock(productId, quantity)){
            String orderId = generateOrderId();
            Order order = new Order(orderId, personId, productId, com.stampede.model.OrderStatus.COMPLETED);
            ordersFinal.add(order);
            return true;
        }
        return false;
    }
    public List<Order> getAllOrders(){
        return new ArrayList<>(ordersFinal);
    }
    private String generateOrderId() {
        return "ORD-" + UUID.randomUUID().toString();
    }
}

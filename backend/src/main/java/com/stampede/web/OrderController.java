package com.stampede.web;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestBody;
import com.stampede.model.BuyRequest;
import com.stampede.service.OrderService;
@RestController
@RequestMapping ("/api")
public class OrderController {
    private final OrderService orderService;
    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }
    @PostMapping ("/orders/order")
    public ResponseEntity<?> createOrder(@RequestBody BuyRequest order){
        boolean orderCreated = orderService.placeOrder(order.getPersonId(), order.getProductId(), order.getQuantity());
        if (!orderCreated) {
            return ResponseEntity.status(409).body("Sold out or product not found");
        }
        return ResponseEntity.ok("Order created successfully");
    }
    
}

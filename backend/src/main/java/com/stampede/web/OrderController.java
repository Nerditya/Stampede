package com.stampede.web;

import com.stampede.dto.BuyRequest;
import com.stampede.model.Order;
import com.stampede.service.OrderService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping("/orders/order")
    public ResponseEntity<?> createOrder(@Valid @RequestBody BuyRequest request,
                                         Authentication authentication) {
        String personId = authentication.getName();
        Order order = orderService.placeOrder(personId, request.getItems());
        return ResponseEntity.ok(order);
    }

    @GetMapping("/orders/my")
    public ResponseEntity<?> getMyOrders(Authentication authentication) {
        return ResponseEntity.ok(orderService.getOrdersByPerson(authentication.getName()));
    }

    @GetMapping("/orders")
    public ResponseEntity<?> getAllOrders() {
        return ResponseEntity.ok(orderService.getAllOrders());
    }
}

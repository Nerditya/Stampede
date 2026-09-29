package com.stampede.service;

import com.stampede.dto.LineItemRequest;
import com.stampede.exception.OutOfStockException;
import com.stampede.model.Order;
import com.stampede.model.OrderItem;
import com.stampede.model.OrderStatus;
import com.stampede.model.Product;
import com.stampede.repository.OrderRepository;
import com.stampede.repository.ProductRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class OrderService {

    private final ProductRepository productRepository;
    private final OrderRepository orderRepository;

    public OrderService(ProductRepository productRepository, OrderRepository orderRepository) {
        this.productRepository = productRepository;
        this.orderRepository = orderRepository;
    }

    /**
     * Places a multi-item order atomically. Every item's stock is decremented with
     * an atomic conditional UPDATE. If any item is out of stock, an exception is
     * thrown and @Transactional rolls back ALL decrements — the whole cart is
     * all-or-nothing.
     */
    @Transactional
    public Order placeOrder(String personId, List<LineItemRequest> items) {
        Order order = new Order(generateOrderId(), personId);

        for (LineItemRequest item : items) {
            Product product = productRepository.findById(item.getProductId())
                    .orElseThrow(() -> new OutOfStockException(
                            "Product not found: " + item.getProductId()));

            int rowsChanged = productRepository.decrementStock(item.getProductId(), item.getQuantity());
            if (rowsChanged == 0) {
                // triggers rollback of every decrement done so far in this transaction
                throw new OutOfStockException(
                        "Insufficient stock for product: " + item.getProductId());
            }

            // snapshot the price at purchase time
            order.addItem(new OrderItem(item.getProductId(), item.getQuantity(), product.getPrice()));
        }

        order.setOrderStatus(OrderStatus.COMPLETED);
        return orderRepository.save(order);
    }

    public List<Order> getAllOrders() {
        return orderRepository.findAll();
    }

    public List<Order> getOrdersByPerson(String personId) {
        return orderRepository.findByPersonIdOrderByCreatedAtDesc(personId);
    }

    private String generateOrderId() {
        return "ORD-" + UUID.randomUUID();
    }
}

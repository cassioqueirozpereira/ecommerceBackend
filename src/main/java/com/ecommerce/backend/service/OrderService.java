package com.ecommerce.backend.service;

import com.ecommerce.backend.dto.OrderItemRequestDTO;
import com.ecommerce.backend.dto.OrderRequestDTO;
import com.ecommerce.backend.entity.*;
import com.ecommerce.backend.repository.OrderRepository;
import com.ecommerce.backend.repository.ProductRepository;
import com.ecommerce.backend.repository.UserRepository;
import com.ecommerce.backend.repository.VariantRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final VariantRepository variantRepository;
    private final UserRepository userRepository;

    @Transactional
    public Order createOrder(UUID userId, OrderRequestDTO orderRequestDTO) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        Order order = new Order();
        order.setUser(user);
        order.setStatus("PENDING");

        BigDecimal total = BigDecimal.ZERO;

        for (OrderItemRequestDTO itemDto : orderRequestDTO.getItems()) {
            Product product = productRepository.findById(itemDto.getProductId())
                    .orElseThrow(() -> new IllegalArgumentException("Product not found"));
            
            Variant variant = variantRepository.findById(itemDto.getVariantId())
                    .orElseThrow(() -> new IllegalArgumentException("Variant not found"));

            if (variant.getStock() < itemDto.getQuantity()) {
                throw new IllegalArgumentException("Not enough stock for variant: " + variant.getSku());
            }

            // Reduce stock
            variant.setStock(variant.getStock() - itemDto.getQuantity());

            // Create OrderItem with SNAPSHOT price from Variant
            OrderItem orderItem = new OrderItem();
            orderItem.setProduct(product);
            orderItem.setVariant(variant);
            orderItem.setQuantity(itemDto.getQuantity());
            
            // Hard-copy the price at this exact moment
            BigDecimal unitPrice = variant.getPrice();
            orderItem.setUnitPrice(unitPrice);
            
            BigDecimal itemTotal = unitPrice.multiply(BigDecimal.valueOf(itemDto.getQuantity()));
            orderItem.setTotalPrice(itemTotal);

            order.addItem(orderItem);
            total = total.add(itemTotal);
        }

        order.setSubtotal(total);
        order.setTotal(total);

        // Optional: Create initial payment record
        Payment payment = new Payment();
        payment.setPaymentMethod(orderRequestDTO.getPaymentMethod());
        payment.setStatus("PENDING");
        payment.setAmount(total);
        order.addPayment(payment);

        return orderRepository.save(order);
    }
}
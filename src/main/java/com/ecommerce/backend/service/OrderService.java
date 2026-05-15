package com.ecommerce.backend.service;

import com.ecommerce.backend.dto.OrderDTOs;
import com.ecommerce.backend.entity.Order;
import com.ecommerce.backend.entity.OrderItem;
import com.ecommerce.backend.entity.User;
import com.ecommerce.backend.entity.Variant;
import com.ecommerce.backend.enums.OrderStatus;
import com.ecommerce.backend.repository.OrderRepository;
import com.ecommerce.backend.repository.UserRepository;
import com.ecommerce.backend.repository.VariantRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class OrderService {

    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    private final VariantRepository variantRepository;

    public OrderService(OrderRepository orderRepository, UserRepository userRepository, VariantRepository variantRepository) {
        this.orderRepository = orderRepository;
        this.userRepository = userRepository;
        this.variantRepository = variantRepository;
    }

    @Transactional
    public OrderDTOs.OrderResponse createOrder(String userEmail, OrderDTOs.OrderRequest request) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Order order = new Order();
        order.setUser(user);
        order.setStatus(OrderStatus.PENDING);

        BigDecimal total = BigDecimal.ZERO;

        for (OrderDTOs.OrderItemRequest itemReq : request.getItems()) {
            Variant variant = variantRepository.findById(itemReq.getVariantId())
                    .orElseThrow(() -> new RuntimeException("Variant not found"));

            if (variant.getStockQuantity() < itemReq.getQuantity()) {
                throw new RuntimeException("Insufficient stock for variant: " + variant.getSku());
            }

            variant.setStockQuantity(variant.getStockQuantity() - itemReq.getQuantity());

            OrderItem orderItem = new OrderItem();
            orderItem.setOrder(order);
            orderItem.setVariant(variant);
            orderItem.setQuantity(itemReq.getQuantity());
            orderItem.setUnitPrice(variant.getPrice());

            order.getItems().add(orderItem);

            total = total.add(variant.getPrice().multiply(BigDecimal.valueOf(itemReq.getQuantity())));
        }

        order.setSubtotal(total);
        order.setTotal(total);

        Order savedOrder = orderRepository.save(order);
        return mapToDTO(savedOrder);
    }

    public List<OrderDTOs.OrderResponse> getUserOrders(String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        List<Order> orders = orderRepository.findByUserIdOrderByCreatedAtDesc(user.getId());
        return orders.stream().map(this::mapToDTO).collect(Collectors.toList());
    }

    private OrderDTOs.OrderResponse mapToDTO(Order order) {
        OrderDTOs.OrderResponse dto = new OrderDTOs.OrderResponse();
        dto.setId(order.getId());
        dto.setSubtotal(order.getSubtotal());
        dto.setTotal(order.getTotal());
        dto.setStatus(order.getStatus());
        dto.setCreatedAt(order.getCreatedAt());

        List<OrderDTOs.OrderItemResponse> items = order.getItems().stream().map(item -> {
            OrderDTOs.OrderItemResponse itemDto = new OrderDTOs.OrderItemResponse();
            itemDto.setId(item.getId());
            itemDto.setProductTitle(item.getVariant().getProduct().getTitle());
            itemDto.setVariantSku(item.getVariant().getSku());
            itemDto.setQuantity(item.getQuantity());
            itemDto.setUnitPrice(item.getUnitPrice());
            return itemDto;
        }).collect(Collectors.toList());

        dto.setItems(items);
        return dto;
    }
}
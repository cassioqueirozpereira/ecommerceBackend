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
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final VariantRepository variantRepository;
    private final UserRepository userRepository;
    private final MercadoPagoService mercadoPagoService;

    @Transactional
    public Order createOrder(UUID userId, OrderRequestDTO dto, String idempotencyKey) {
        if (idempotencyKey != null && !idempotencyKey.isBlank()) {
            Optional<Order> existing = orderRepository.findByIdempotencyKey(idempotencyKey);
            if (existing.isPresent()) return existing.get();
        }

        if (dto.getPaymentMethod() == PaymentMethod.PIX_MANUAL) {
            validatePixFields(dto);
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        Order order = new Order();
        order.setUser(user);
        order.setIdempotencyKey(idempotencyKey);

        BigDecimal total = BigDecimal.ZERO;

        for (OrderItemRequestDTO itemDto : dto.getItems()) {
            Product product = productRepository.findById(itemDto.getProductId())
                    .orElseThrow(() -> new IllegalArgumentException("Product not found: " + itemDto.getProductId()));

            Variant variant = variantRepository.findById(itemDto.getVariantId())
                    .orElseThrow(() -> new IllegalArgumentException("Variant not found: " + itemDto.getVariantId()));

            if (variant.getStock() < itemDto.getQuantity()) {
                throw new IllegalArgumentException("Insufficient stock for variant: " + variant.getSku());
            }

            variant.setStock(variant.getStock() - itemDto.getQuantity());

            OrderItem orderItem = new OrderItem();
            orderItem.setProduct(product);
            orderItem.setVariant(variant);
            orderItem.setQuantity(itemDto.getQuantity());
            orderItem.setProductName(product.getName());
            orderItem.setSku(variant.getSku());
            orderItem.setVariantName(variant.getName());
            orderItem.setImageUrl(product.getImages() != null && !product.getImages().isEmpty()
                    ? product.getImages().get(0) : "");

            BigDecimal unitPrice = variant.getPrice();
            orderItem.setUnitPrice(unitPrice);
            orderItem.setTotalPrice(unitPrice.multiply(BigDecimal.valueOf(itemDto.getQuantity())));

            order.addItem(orderItem);
            total = total.add(orderItem.getTotalPrice());
        }

        order.setSubtotal(total);
        order.setTotal(total);
        // Save first to get the generated UUID from JPA, then call MP with that ID
        order.setStatus("PENDING_PAYMENT");
        Payment payment = new Payment();
        payment.setPaymentMethod(dto.getPaymentMethod());
        payment.setAmount(total);

        if (dto.getPaymentMethod() == PaymentMethod.PIX_MANUAL) {
            order.setStatus("AWAITING_APPROVAL");
            order.setPayerName(dto.getPayerName());
            order.setPayerCpf(dto.getPayerCpf());
            order.setPayerPhone(dto.getPayerPhone());
            order.setPixReceiptUrl(dto.getPixReceiptUrl());
            payment.setStatus("AWAITING_APPROVAL");
            order.addPayment(payment);
            return orderRepository.save(order);
        } else if (dto.getPaymentMethod() == PaymentMethod.MERCADO_PAGO_CARD) {
            if (dto.getCardPayment() == null) {
                throw new IllegalArgumentException("Card payment details are required for MERCADO_PAGO_CARD");
            }
            // Save order first to get DB-generated UUID (needed as externalReference)
            payment.setStatus("PENDING");
            order.addPayment(payment);
            Order savedOrder = orderRepository.save(order);

            try {
                com.mercadopago.resources.payment.Payment mpPayment = mercadoPagoService.processCardPayment(savedOrder, dto.getCardPayment());
                String status = mpPayment.getStatus();
                if ("rejected".equalsIgnoreCase(status)) {
                    // Update status but keep order for traceability
                    savedOrder.setStatus("FAILED");
                    savedOrder.getPayments().forEach(p -> p.setStatus("REJECTED"));
                    orderRepository.save(savedOrder);
                    throw new com.ecommerce.backend.exception.PaymentRejectedException("Pagamento recusado: " + mpPayment.getStatusDetail());
                } else if ("approved".equalsIgnoreCase(status)) {
                    savedOrder.setStatus("PAID");
                    savedOrder.getPayments().forEach(p -> p.setStatus("APPROVED"));
                } else {
                    // pending / in_process: keep PENDING_PAYMENT
                    savedOrder.setStatus("PENDING_PAYMENT");
                }
                return orderRepository.save(savedOrder);
            } catch (com.ecommerce.backend.exception.PaymentRejectedException e) {
                throw e;
            } catch (Exception e) {
                savedOrder.setStatus("FAILED");
                savedOrder.getPayments().forEach(p -> p.setStatus("FAILED"));
                orderRepository.save(savedOrder);
                throw new RuntimeException("Erro ao processar pagamento: " + e.getMessage(), e);
            }
        } else {
            order.setStatus("PENDING_PAYMENT");
            payment.setStatus("PENDING");
            order.addPayment(payment);
            return orderRepository.save(order);
        }
    }


    @Transactional(readOnly = true)
    public List<Order> getPendingPixOrders() {
        return orderRepository.findByStatus("AWAITING_APPROVAL");
    }

    @Transactional
    public Order approvePix(UUID orderId, UUID adminId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Order not found"));

        if (!"AWAITING_APPROVAL".equals(order.getStatus())) {
            throw new IllegalStateException("Order is not awaiting approval. Current status: " + order.getStatus());
        }

        order.setStatus("PAID");
        order.setApprovedBy(adminId);
        order.setApprovedAt(LocalDateTime.now());

        order.getPayments().forEach(p -> p.setStatus("APPROVED"));

        return orderRepository.save(order);
    }

    @Transactional
    public Order rejectPix(UUID orderId, UUID adminId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Order not found"));

        if (!"AWAITING_APPROVAL".equals(order.getStatus())) {
            throw new IllegalStateException("Order is not awaiting approval. Current status: " + order.getStatus());
        }

        restoreStock(order);

        order.setStatus("REJECTED");
        order.setRejectedBy(adminId);
        order.setRejectedAt(LocalDateTime.now());

        order.getPayments().forEach(p -> p.setStatus("REJECTED"));

        return orderRepository.save(order);
    }

    @Transactional
    public int expireOldPixOrders() {
        LocalDateTime expiryTime = LocalDateTime.now().minusHours(24);
        List<Order> expired = orderRepository.findExpiredPixOrders(expiryTime);

        for (Order order : expired) {
            restoreStock(order);
            order.setStatus("EXPIRED");
            order.getPayments().forEach(p -> p.setStatus("EXPIRED"));
        }

        orderRepository.saveAll(expired);
        return expired.size();
    }

    private void restoreStock(Order order) {
        for (OrderItem item : order.getItems()) {
            if (item.getVariant() != null) {
                item.getVariant().setStock(item.getVariant().getStock() + item.getQuantity());
            }
        }
    }

    private void validatePixFields(OrderRequestDTO dto) {
        if (dto.getPayerName() == null || dto.getPayerName().isBlank()) {
            throw new IllegalArgumentException("Payer name is required for PIX_MANUAL orders");
        }
        if (dto.getPayerCpf() == null || dto.getPayerCpf().isBlank()) {
            throw new IllegalArgumentException("Payer CPF is required for PIX_MANUAL orders");
        }
        if (dto.getPayerPhone() == null || dto.getPayerPhone().isBlank()) {
            throw new IllegalArgumentException("Payer phone is required for PIX_MANUAL orders");
        }
        if (dto.getPixReceiptUrl() == null || dto.getPixReceiptUrl().isBlank()) {
            throw new IllegalArgumentException("Pix receipt URL is required for PIX_MANUAL orders");
        }
    }
}
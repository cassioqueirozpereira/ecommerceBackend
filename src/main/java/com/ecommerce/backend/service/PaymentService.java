package com.ecommerce.backend.service;

import com.ecommerce.backend.entity.Order;
import com.ecommerce.backend.entity.OrderItem;
import com.ecommerce.backend.entity.Payment;
import com.ecommerce.backend.enums.OrderStatus;
import com.ecommerce.backend.enums.PaymentStatus;
import com.ecommerce.backend.repository.OrderRepository;
import com.ecommerce.backend.repository.PaymentRepository;
import com.mercadopago.MercadoPagoConfig;
import com.mercadopago.client.preference.PreferenceClient;
import com.mercadopago.client.preference.PreferenceItemRequest;
import com.mercadopago.client.preference.PreferenceRequest;
import com.mercadopago.resources.preference.Preference;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class PaymentService {

    private final OrderRepository orderRepository;
    private final PaymentRepository paymentRepository;

    @Value("${mercadopago.access-token}")
    private String mercadoPagoAccessToken;

    public PaymentService(OrderRepository orderRepository, PaymentRepository paymentRepository) {
        this.orderRepository = orderRepository;
        this.paymentRepository = paymentRepository;
    }

    @Transactional
    public String createPaymentPreference(UUID orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found: " + orderId));

        try {
            MercadoPagoConfig.setAccessToken(mercadoPagoAccessToken);
            PreferenceClient client = new PreferenceClient();

            List<PreferenceItemRequest> items = new ArrayList<>();
            for (OrderItem orderItem : order.getItems()) {
                PreferenceItemRequest itemRequest = PreferenceItemRequest.builder()
                        .id(orderItem.getVariant().getId().toString())
                        .title(orderItem.getVariant().getProduct().getTitle())
                        .description(orderItem.getVariant().getSku())
                        .quantity(orderItem.getQuantity())
                        .unitPrice(orderItem.getUnitPrice())
                        .build();
                items.add(itemRequest);
            }

            PreferenceRequest request = PreferenceRequest.builder()
                    .items(items)
                    .externalReference(order.getId().toString())
                    // TODO: backUrls
                    .build();

            Preference preference = client.create(request);
            
            // Create pending payment record
            Payment payment = new Payment();
            payment.setOrder(order);
            payment.setStatus(PaymentStatus.PENDING);
            paymentRepository.save(payment);

            return preference.getInitPoint();

        } catch (Exception e) {
            throw new RuntimeException("Error creating Mercado Pago preference", e);
        }
    }

    @Transactional
    public void processWebhook(Map<String, Object> payload) {
        try {
            if (payload.containsKey("type") && "payment".equals(payload.get("type"))) {
                Map<String, Object> data = (Map<String, Object>) payload.get("data");
                if (data != null && data.containsKey("id")) {
                    String paymentId = data.get("id").toString();
                    
                    MercadoPagoConfig.setAccessToken(mercadoPagoAccessToken);
                    com.mercadopago.client.payment.PaymentClient paymentClient = new com.mercadopago.client.payment.PaymentClient();
                    com.mercadopago.resources.payment.Payment mpPayment = paymentClient.get(Long.parseLong(paymentId));

                    if (mpPayment != null && mpPayment.getExternalReference() != null) {
                        UUID orderId = UUID.fromString(mpPayment.getExternalReference());
                        Order order = orderRepository.findById(orderId).orElse(null);
                        
                        if (order != null) {
                            Payment payment = paymentRepository.findByOrderId(orderId).orElse(new Payment());
                            payment.setOrder(order);
                            payment.setExternalReference(paymentId);
                            
                            if ("approved".equals(mpPayment.getStatus())) {
                                payment.setStatus(PaymentStatus.APPROVED);
                                order.setStatus(OrderStatus.PAID);
                            } else if ("rejected".equals(mpPayment.getStatus())) {
                                payment.setStatus(PaymentStatus.REJECTED);
                                order.setStatus(OrderStatus.CANCELLED);
                            }
                            paymentRepository.save(payment);
                            orderRepository.save(order);
                        }
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Error processing MercadoPago webhook: " + e.getMessage());
        }
    }
}
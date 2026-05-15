package com.ecommerce.backend.dto;

import com.ecommerce.backend.enums.OrderStatus;
import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.UUID;

public class OrderDTOs {

    public static class OrderRequest {
        private List<OrderItemRequest> items;

        public List<OrderItemRequest> getItems() { return items; }
        public void setItems(List<OrderItemRequest> items) { this.items = items; }
    }

    public static class OrderItemRequest {
        private UUID variantId;
        private Integer quantity;

        public UUID getVariantId() { return variantId; }
        public void setVariantId(UUID variantId) { this.variantId = variantId; }
        public Integer getQuantity() { return quantity; }
        public void setQuantity(Integer quantity) { this.quantity = quantity; }
    }

    public static class OrderResponse {
        private UUID id;
        private BigDecimal subtotal;
        private BigDecimal total;
        private OrderStatus status;
        private ZonedDateTime createdAt;
        private String paymentInitPoint;
        private List<OrderItemResponse> items;

        public UUID getId() { return id; }
        public void setId(UUID id) { this.id = id; }
        public BigDecimal getSubtotal() { return subtotal; }
        public void setSubtotal(BigDecimal subtotal) { this.subtotal = subtotal; }
        public BigDecimal getTotal() { return total; }
        public void setTotal(BigDecimal total) { this.total = total; }
        public OrderStatus getStatus() { return status; }
        public void setStatus(OrderStatus status) { this.status = status; }
        public ZonedDateTime getCreatedAt() { return createdAt; }
        public void setCreatedAt(ZonedDateTime createdAt) { this.createdAt = createdAt; }
        public String getPaymentInitPoint() { return paymentInitPoint; }
        public void setPaymentInitPoint(String paymentInitPoint) { this.paymentInitPoint = paymentInitPoint; }
        public List<OrderItemResponse> getItems() { return items; }
        public void setItems(List<OrderItemResponse> items) { this.items = items; }
    }

    public static class OrderItemResponse {
        private UUID id;
        private String productTitle;
        private String variantSku;
        private Integer quantity;
        private BigDecimal unitPrice;

        public UUID getId() { return id; }
        public void setId(UUID id) { this.id = id; }
        public String getProductTitle() { return productTitle; }
        public void setProductTitle(String productTitle) { this.productTitle = productTitle; }
        public String getVariantSku() { return variantSku; }
        public void setVariantSku(String variantSku) { this.variantSku = variantSku; }
        public Integer getQuantity() { return quantity; }
        public void setQuantity(Integer quantity) { this.quantity = quantity; }
        public BigDecimal getUnitPrice() { return unitPrice; }
        public void setUnitPrice(BigDecimal unitPrice) { this.unitPrice = unitPrice; }
    }
}
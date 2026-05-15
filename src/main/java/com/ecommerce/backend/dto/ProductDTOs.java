package com.ecommerce.backend.dto;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public class ProductDTOs {

    public static class ProductResponse {
        private UUID id;
        private String title;
        private String slug;
        private String description;
        private BigDecimal basePrice;
        private String imageUrl;
        private CategoryDTOs.CategoryResponse category;
        private List<VariantResponse> variants;

        // Getters and Setters
        public UUID getId() { return id; }
        public void setId(UUID id) { this.id = id; }
        public String getTitle() { return title; }
        public void setTitle(String title) { this.title = title; }
        public String getSlug() { return slug; }
        public void setSlug(String slug) { this.slug = slug; }
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
        public BigDecimal getBasePrice() { return basePrice; }
        public void setBasePrice(BigDecimal basePrice) { this.basePrice = basePrice; }
        public String getImageUrl() { return imageUrl; }
        public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
        public CategoryDTOs.CategoryResponse getCategory() { return category; }
        public void setCategory(CategoryDTOs.CategoryResponse category) { this.category = category; }
        public List<VariantResponse> getVariants() { return variants; }
        public void setVariants(List<VariantResponse> variants) { this.variants = variants; }
    }

    public static class VariantResponse {
        private UUID id;
        private String sku;
        private BigDecimal price;
        private Integer stockQuantity;
        private List<VariantAttributeResponse> attributes;

        // Getters and Setters
        public UUID getId() { return id; }
        public void setId(UUID id) { this.id = id; }
        public String getSku() { return sku; }
        public void setSku(String sku) { this.sku = sku; }
        public BigDecimal getPrice() { return price; }
        public void setPrice(BigDecimal price) { this.price = price; }
        public Integer getStockQuantity() { return stockQuantity; }
        public void setStockQuantity(Integer stockQuantity) { this.stockQuantity = stockQuantity; }
        public List<VariantAttributeResponse> getAttributes() { return attributes; }
        public void setAttributes(List<VariantAttributeResponse> attributes) { this.attributes = attributes; }
    }

    public static class VariantAttributeResponse {
        private String name;
        private List<String> options;

        public VariantAttributeResponse(String name, List<String> options) {
            this.name = name;
            this.options = options;
        }

        public String getName() { return name; }
        public List<String> getOptions() { return options; }
    }
}
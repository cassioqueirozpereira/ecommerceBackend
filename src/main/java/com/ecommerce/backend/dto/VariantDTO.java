package com.ecommerce.backend.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.util.UUID;

@Data
public class VariantDTO {
    private UUID id;
    private String sku;
    private String name;
    private BigDecimal price;
    private Integer stock;
}

package com.ecommerce.backend.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Data
public class ProductDTO {
    private UUID id;
    private String name;
    private String slug;
    private String description;
    private BigDecimal basePrice;
    private BigDecimal promotionalPrice;
    private List<String> images;
    private CategoryDTO category;
    private List<VariantDTO> variants;
}

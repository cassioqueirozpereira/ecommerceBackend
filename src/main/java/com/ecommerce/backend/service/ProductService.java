package com.ecommerce.backend.service;

import com.ecommerce.backend.dto.CategoryDTOs;
import com.ecommerce.backend.dto.ProductDTOs;
import com.ecommerce.backend.entity.AttributeOption;
import com.ecommerce.backend.entity.Category;
import com.ecommerce.backend.entity.Product;
import com.ecommerce.backend.entity.Variant;
import com.ecommerce.backend.repository.ProductRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ProductService {

    private final ProductRepository productRepository;

    public ProductService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    public List<ProductDTOs.ProductResponse> getAllProducts(String search) {
        List<Product> products;
        if (search != null && !search.trim().isEmpty()) {
            products = productRepository.searchByTitleOrDescription(search);
        } else {
            products = productRepository.findAll();
        }
        return products.stream().map(this::mapToDTO).collect(Collectors.toList());
    }

    public ProductDTOs.ProductResponse getProductBySlug(String slug) {
        Product product = productRepository.findBySlug(slug)
                .orElseThrow(() -> new RuntimeException("Product not found"));
        return mapToDTO(product);
    }

    private ProductDTOs.ProductResponse mapToDTO(Product product) {
        ProductDTOs.ProductResponse dto = new ProductDTOs.ProductResponse();
        dto.setId(product.getId());
        dto.setTitle(product.getTitle());
        dto.setSlug(product.getSlug());
        dto.setDescription(product.getDescription());
        dto.setBasePrice(product.getBasePrice());
        dto.setImageUrl(product.getImageUrl());

        Category cat = product.getCategory();
        if (cat != null) {
            dto.setCategory(new CategoryDTOs.CategoryResponse(cat.getId(), cat.getName(), cat.getSlug(), cat.getParent() != null ? cat.getParent().getId() : null));
        }

        List<ProductDTOs.VariantResponse> variants = product.getVariants().stream().map(v -> {
            ProductDTOs.VariantResponse vDto = new ProductDTOs.VariantResponse();
            vDto.setId(v.getId());
            vDto.setSku(v.getSku());
            vDto.setPrice(v.getPrice());
            vDto.setStockQuantity(v.getStockQuantity());

            List<ProductDTOs.VariantAttributeResponse> attrs = v.getAttributes().stream().map(va -> {
                List<String> options = va.getOptions().stream().map(AttributeOption::getValue).collect(Collectors.toList());
                return new ProductDTOs.VariantAttributeResponse(va.getName(), options);
            }).collect(Collectors.toList());

            vDto.setAttributes(attrs);
            return vDto;
        }).collect(Collectors.toList());

        dto.setVariants(variants);
        return dto;
    }
}
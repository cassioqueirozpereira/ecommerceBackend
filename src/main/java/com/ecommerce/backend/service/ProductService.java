package com.ecommerce.backend.service;

import com.ecommerce.backend.dto.ProductDTO;
import com.ecommerce.backend.entity.Product;
import com.ecommerce.backend.entity.Variant;
import com.ecommerce.backend.repository.CategoryRepository;
import com.ecommerce.backend.repository.ProductRepository;
import com.ecommerce.backend.repository.VariantRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.UUID;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final VariantRepository variantRepository;

    @Transactional
    public Product createProduct(ProductDTO productDTO) {
        if (productRepository.existsBySlug(productDTO.getSlug())) {
            throw new IllegalArgumentException("Product with this slug already exists");
        }

        Product product = new Product();
        product.setName(productDTO.getName());
        product.setSlug(productDTO.getSlug());
        product.setDescription(productDTO.getDescription());
        product.setBasePrice(productDTO.getBasePrice());
        product.setPromotionalPrice(productDTO.getPromotionalPrice());
        product.setImages(productDTO.getImages());

        if (productDTO.getCategory() != null && productDTO.getCategory().getId() != null) {
            product.setCategory(categoryRepository.findById(productDTO.getCategory().getId())
                    .orElseThrow(() -> new IllegalArgumentException("Category not found")));
        }

        Product savedProduct = productRepository.save(product);

        // If no variants provided, create a default one
        if (productDTO.getVariants() == null || productDTO.getVariants().isEmpty()) {
            Variant defaultVariant = new Variant();
            defaultVariant.setProduct(savedProduct);
            defaultVariant.setName("Default");
            defaultVariant.setSku(product.getSlug().toUpperCase() + "-DEFAULT");
            defaultVariant.setPrice(product.getPromotionalPrice() != null ? product.getPromotionalPrice() : product.getBasePrice());
            defaultVariant.setStock(0);
            variantRepository.save(defaultVariant);
            savedProduct.addVariant(defaultVariant);
        } else {
            productDTO.getVariants().forEach(v -> {
                Variant variant = new Variant();
                variant.setProduct(savedProduct);
                variant.setName(v.getName());
                variant.setSku(v.getSku() != null ? v.getSku() : UUID.randomUUID().toString());
                variant.setPrice(v.getPrice() != null ? v.getPrice() : product.getBasePrice());
                variant.setStock(v.getStock() != null ? v.getStock() : 0);
                variantRepository.save(variant);
                savedProduct.addVariant(variant);
            });
        }

        return savedProduct;
    }
    
    public List<Product> getAllProducts() {
        return productRepository.findAll();
    }
}
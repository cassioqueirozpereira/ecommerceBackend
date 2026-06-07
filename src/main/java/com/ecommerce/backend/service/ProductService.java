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
import org.springframework.data.domain.Sort;
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
    
    @Transactional
    public Product updateProduct(UUID id, ProductDTO productDTO) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Product not found"));

        if (!product.getSlug().equals(productDTO.getSlug()) && productRepository.existsBySlug(productDTO.getSlug())) {
            throw new IllegalArgumentException("Product with this slug already exists");
        }

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

        if (productDTO.getVariants() != null) {
            // Remove variants not present in DTO
            List<String> incomingSkus = productDTO.getVariants().stream()
                    .map(v -> v.getSku())
                    .toList();
            
            product.getVariants().removeIf(v -> !incomingSkus.contains(v.getSku()));

            // Update or add
            for (var vDto : productDTO.getVariants()) {
                Variant existing = product.getVariants().stream()
                        .filter(v -> v.getSku().equals(vDto.getSku()))
                        .findFirst()
                        .orElse(null);

                if (existing != null) {
                    existing.setName(vDto.getName());
                    existing.setPrice(vDto.getPrice() != null ? vDto.getPrice() : product.getBasePrice());
                    existing.setStock(vDto.getStock() != null ? vDto.getStock() : 0);
                } else {
                    Variant newVariant = new Variant();
                    newVariant.setProduct(product);
                    newVariant.setName(vDto.getName());
                    newVariant.setSku(vDto.getSku() != null ? vDto.getSku() : UUID.randomUUID().toString());
                    newVariant.setPrice(vDto.getPrice() != null ? vDto.getPrice() : product.getBasePrice());
                    newVariant.setStock(vDto.getStock() != null ? vDto.getStock() : 0);
                    product.addVariant(newVariant);
                }
            }
        }

        return productRepository.save(product);
    }

    @Transactional
    public void deleteProduct(UUID id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Product not found"));
        product.setActive(false);
        productRepository.save(product);
    }
    
    public List<Product> getAllProducts(String category, String sort, String search) {
        Sort sortSpec = Sort.by(Sort.Direction.DESC, "createdAt"); // default
        if (sort != null) {
            if (sort.equalsIgnoreCase("price_asc")) {
                sortSpec = Sort.by(Sort.Direction.ASC, "basePrice");
            } else if (sort.equalsIgnoreCase("price_desc")) {
                sortSpec = Sort.by(Sort.Direction.DESC, "basePrice");
            }
        }
        List<Product> products = productRepository.searchAndFilter(category, search, sortSpec);
        sortAvailableFirst(products);
        return products;
    }

    private void sortAvailableFirst(List<Product> products) {
        products.sort((p1, p2) -> {
            boolean p1InStock = p1.getVariants().stream().mapToInt(Variant::getStock).sum() > 0;
            boolean p2InStock = p2.getVariants().stream().mapToInt(Variant::getStock).sum() > 0;
            if (p1InStock && !p2InStock) return -1;
            if (!p1InStock && p2InStock) return 1;
            return 0;
        });
    }
}
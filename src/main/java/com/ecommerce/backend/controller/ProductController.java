package com.ecommerce.backend.controller;

import com.ecommerce.backend.dto.ProductDTOs;
import com.ecommerce.backend.service.ProductService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/products")
public class ProductController {

    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @GetMapping
    public ResponseEntity<List<ProductDTOs.ProductResponse>> getAllProducts(
            @RequestParam(required = false) String search) {
        return ResponseEntity.ok(productService.getAllProducts(search));
    }

    @GetMapping("/{slug}")
    public ResponseEntity<ProductDTOs.ProductResponse> getProductBySlug(@PathVariable String slug) {
        return ResponseEntity.ok(productService.getProductBySlug(slug));
    }
}
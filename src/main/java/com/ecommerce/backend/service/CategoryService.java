package com.ecommerce.backend.service;

import com.ecommerce.backend.dto.CategoryDTOs;
import com.ecommerce.backend.repository.CategoryRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class CategoryService {

    private final CategoryRepository categoryRepository;

    public CategoryService(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    public List<CategoryDTOs.CategoryResponse> getAllCategories() {
        return categoryRepository.findAll().stream()
                .map(cat -> new CategoryDTOs.CategoryResponse(
                        cat.getId(),
                        cat.getName(),
                        cat.getSlug(),
                        cat.getParent() != null ? cat.getParent().getId() : null
                )).collect(Collectors.toList());
    }
}
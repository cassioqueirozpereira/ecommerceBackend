package com.ecommerce.backend.dto;

import java.util.UUID;

public class CategoryDTOs {

    public static class CategoryResponse {
        private UUID id;
        private String name;
        private String slug;
        private UUID parentId;

        public CategoryResponse(UUID id, String name, String slug, UUID parentId) {
            this.id = id;
            this.name = name;
            this.slug = slug;
            this.parentId = parentId;
        }

        public UUID getId() { return id; }
        public String getName() { return name; }
        public String getSlug() { return slug; }
        public UUID getParentId() { return parentId; }
    }
}
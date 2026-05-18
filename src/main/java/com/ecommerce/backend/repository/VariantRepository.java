package com.ecommerce.backend.repository;

import com.ecommerce.backend.entity.Variant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface VariantRepository extends JpaRepository<Variant, UUID> {
    Optional<Variant> findBySku(String sku);
    boolean existsBySku(String sku);
}
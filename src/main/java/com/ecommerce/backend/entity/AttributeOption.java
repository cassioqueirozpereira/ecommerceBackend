package com.ecommerce.backend.entity;

import jakarta.persistence.*;
import java.util.UUID;

@Entity
@Table(name = "attribute_options")
public class AttributeOption {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "variant_attribute_id", nullable = false)
    private VariantAttribute variantAttribute;

    @Column(nullable = false, length = 100)
    private String value;

    // Getters and Setters
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public VariantAttribute getVariantAttribute() { return variantAttribute; }
    public void setVariantAttribute(VariantAttribute variantAttribute) { this.variantAttribute = variantAttribute; }
    public String getValue() { return value; }
    public void setValue(String value) { this.value = value; }
}
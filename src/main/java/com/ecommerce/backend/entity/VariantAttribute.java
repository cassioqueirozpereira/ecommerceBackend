package com.ecommerce.backend.entity;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "variant_attributes")
public class VariantAttribute {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "variant_id", nullable = false)
    private Variant variant;

    @Column(nullable = false, length = 100)
    private String name;

    @OneToMany(mappedBy = "variantAttribute", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<AttributeOption> options = new ArrayList<>();

    // Getters and Setters
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public Variant getVariant() { return variant; }
    public void setVariant(Variant variant) { this.variant = variant; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public List<AttributeOption> getOptions() { return options; }
    public void setOptions(List<AttributeOption> options) { this.options = options; }
}
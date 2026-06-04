package com.ecommerce.backend.repository;

import com.ecommerce.backend.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface OrderRepository extends JpaRepository<Order, UUID> {
    List<Order> findByUserId(UUID userId);
    Optional<Order> findByIdempotencyKey(String idempotencyKey);
    List<Order> findByStatus(String status);

    @Query("SELECT o FROM Order o WHERE o.status = 'AWAITING_APPROVAL' AND o.createdAt < :expiryTime")
    List<Order> findExpiredPixOrders(@Param("expiryTime") LocalDateTime expiryTime);
}
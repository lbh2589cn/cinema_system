package com.cinema.system.order.repository;

import com.cinema.system.order.entity.Order;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
    Optional<Order> findByOrderNo(String orderNo);

    @Query("SELECT o FROM Order o WHERE o.userId = :userId AND o.visible = true ORDER BY o.createdAt DESC")
    Page<Order> findVisibleByUserId(@Param("userId") Long userId, Pageable pageable);

    @Query("SELECT o FROM Order o ORDER BY o.createdAt DESC")
    Page<Order> findAllOrderByCreatedAtDesc(Pageable pageable);

    @Query(value = "SELECT * FROM `order` WHERE status = :status AND created_at < DATE_SUB(NOW(), INTERVAL 3 MINUTE) LIMIT :limit", nativeQuery = true)
    List<Order> findExpiredPendingOrdersBatch(@Param("status") String status, @Param("limit") int limit);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("UPDATE Order o SET o.status = 'CANCELLED' WHERE o.id IN :ids AND o.status = 'PENDING'")
    int batchCancelOrders(@Param("ids") List<Long> ids);
}

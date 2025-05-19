package com.ecom.productcatalog.repository;

import com.ecom.productcatalog.model.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {
    // Bạn có thể thêm các phương thức truy vấn tùy chỉnh ở đây nếu cần
}
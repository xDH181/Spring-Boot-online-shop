package com.ecom.productcatalog.repository;

import com.ecom.productcatalog.model.CustomerOrder;
import com.ecom.productcatalog.model.User; // Thêm import này
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository; // Thêm @Repository nếu chưa có

import java.util.List; // Thêm import này

@Repository // Đảm bảo có @Repository
public interface CustomerOrderRepository extends JpaRepository<CustomerOrder, Long> {
    // Phương thức mới để tìm đơn hàng theo User
    List<CustomerOrder> findByUserOrderByIdDesc(User user); // Sắp xếp theo ID giảm dần để đơn hàng mới nhất lên đầu

    // (Tùy chọn) Nếu bạn muốn tìm một đơn hàng cụ thể của người dùng
    // Optional<CustomerOrder> findByIdAndUser(Long id, User user);
}
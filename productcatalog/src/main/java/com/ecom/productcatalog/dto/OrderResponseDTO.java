package com.ecom.productcatalog.dto;

import com.ecom.productcatalog.model.OrderStatus;
import java.time.LocalDateTime;
import java.util.List;

public record OrderResponseDTO(
        Long id,
        String username, // Tên người dùng đặt hàng
        LocalDateTime orderDate,
        OrderStatus status,
        Double totalAmount,
        List<OrderItemResponseDTO> items
) {
}
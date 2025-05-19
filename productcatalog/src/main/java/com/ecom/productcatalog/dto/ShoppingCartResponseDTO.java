package com.ecom.productcatalog.dto;

import java.time.LocalDateTime;
import java.util.List;

public record ShoppingCartResponseDTO(
        Long cartId,         // ID của ShoppingCart
        Long userId,         // ID của người dùng sở hữu giỏ hàng
        String username,     // Tên của người dùng
        List<CartItemResponseDTO> items,
        Double totalAmount,  // Tổng giá trị của tất cả các mục trong giỏ hàng
        Integer totalItems,  // Tổng số lượng sản phẩm (tính theo từng cái, không phải số loại sản phẩm)
        LocalDateTime lastModifiedDate
) {
}
package com.ecom.productcatalog.dto;

public record OrderItemResponseDTO(
        Long id, // ID của OrderItem
        ProductInfoDTO product, // Thông tin sản phẩm rút gọn
        Integer quantity,
        Double priceAtPurchase,
        Double subtotal // quantity * priceAtPurchase
) {
}
package com.ecom.productcatalog.dto;

// ProductInfoDTO đã được tạo trước đó:
// package com.ecom.productcatalog.dto;
// public record ProductInfoDTO(Long id, String name, String imageUrl) {}

public record CartItemResponseDTO(
        Long cartItemId, // ID của CartItem
        ProductInfoDTO product,
        Integer quantity,
        Double unitPrice, // Giá của một đơn vị sản phẩm tại thời điểm xem giỏ hàng
        Double subtotal   // quantity * unitPrice
) {
}
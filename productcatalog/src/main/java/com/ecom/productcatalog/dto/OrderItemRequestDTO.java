package com.ecom.productcatalog.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

// Sử dụng record cho DTO đơn giản và bất biến
public record OrderItemRequestDTO(
        @NotNull(message = "Product ID cannot be null")
        Long productId,

        @NotNull(message = "Quantity cannot be null")
        @Min(value = 1, message = "Quantity must be at least 1")
        Integer quantity
) {
}
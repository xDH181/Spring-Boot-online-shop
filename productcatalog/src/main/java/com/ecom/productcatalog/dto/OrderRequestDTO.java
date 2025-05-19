package com.ecom.productcatalog.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;

public record OrderRequestDTO(
        @NotEmpty(message = "Order must contain at least one item")
        @Valid // Đảm bảo các OrderItemRequestDTO bên trong cũng được validate
        List<OrderItemRequestDTO> items
        // Bạn có thể thêm các trường khác như shippingAddressId, paymentMethod sau này
) {
}
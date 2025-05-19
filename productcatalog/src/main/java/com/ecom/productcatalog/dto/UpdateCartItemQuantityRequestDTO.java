package com.ecom.productcatalog.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record UpdateCartItemQuantityRequestDTO(
        @NotNull(message = "Quantity cannot be null")
        @Min(value = 0, message = "Quantity cannot be negative. Use 0 to remove or call delete endpoint.")
        // Nếu quantity = 0, service có thể hiểu là xóa item đó. Hoặc client nên gọi API xóa riêng.
        // Để đơn giản, ở đây cho phép 0, service sẽ xử lý việc xóa nếu quantity là 0.
        Integer quantity
) {
}
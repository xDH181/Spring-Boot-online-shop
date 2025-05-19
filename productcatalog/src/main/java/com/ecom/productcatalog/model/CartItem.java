package com.ecom.productcatalog.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Entity
@Table(name = "cart_items") // Đặt tên bảng rõ ràng
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CartItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "shopping_cart_id", nullable = false)
    @ToString.Exclude // Tránh vòng lặp toString với ShoppingCart
    @EqualsAndHashCode.Exclude // Tránh vòng lặp equals/hashCode với ShoppingCart
    private ShoppingCart shoppingCart;

    @ManyToOne(fetch = FetchType.EAGER) // EAGER để dễ lấy thông tin sản phẩm khi hiển thị giỏ hàng
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Column(nullable = false)
    @Min(value = 1, message = "Quantity must be at least 1")
    private Integer quantity;

    // Constructor tiện ích (tùy chọn)
    public CartItem(ShoppingCart shoppingCart, Product product, Integer quantity) {
        this.shoppingCart = shoppingCart;
        this.product = product;
        this.quantity = quantity;
    }
}
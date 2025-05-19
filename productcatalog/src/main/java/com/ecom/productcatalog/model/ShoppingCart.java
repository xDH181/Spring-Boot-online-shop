package com.ecom.productcatalog.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "shopping_carts") // Đặt tên bảng rõ ràng
@Data
public class ShoppingCart {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true) // Mỗi user chỉ có 1 giỏ hàng
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private User user;

    @OneToMany(mappedBy = "shoppingCart", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Set<CartItem> items = new HashSet<>();

    @UpdateTimestamp // Tự động cập nhật khi có thay đổi
    private LocalDateTime lastModifiedDate;

    // Helper methods để quản lý items
    public void addItem(CartItem item) {
        items.add(item);
        item.setShoppingCart(this);
    }

    public void removeItem(CartItem item) {
        items.remove(item);
        item.setShoppingCart(null);
    }

    // Constructor tiện ích (tùy chọn)
    public ShoppingCart(User user) {
        this.user = user;
    }

    public ShoppingCart() { // Lombok @Data cần constructor không tham số nếu có constructor khác
    }
}
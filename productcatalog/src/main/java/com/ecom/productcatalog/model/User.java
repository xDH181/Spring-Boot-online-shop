package com.ecom.productcatalog.model;

import jakarta.persistence.*;
import lombok.*;

import java.util.HashSet; // Thêm nếu dùng HashSet cho roles
import java.util.Set;

@Entity
@Table(name = "users") // Nên đặt tên bảng rõ ràng là "users" để tránh trùng với từ khóa "user" của SQL
@Data
@NoArgsConstructor
@AllArgsConstructor
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String username;

    @Column(unique = true, nullable = false)
    private String email;

    @Column(nullable = false)
    private String password;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(name = "user_roles",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "role_id"))
    private Set<Role> roles = new HashSet<>(); // Khởi tạo để tránh NullPointerException


    // --- THÊM MỐI QUAN HỆ MỚI VỚI SHOPPING CART ---
    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    @ToString.Exclude // Tránh vòng lặp toString với ShoppingCart
    @EqualsAndHashCode.Exclude // Tránh vòng lặp equals/hashCode với ShoppingCart
    private ShoppingCart shoppingCart;

    // Helper method để gán giỏ hàng cho user (đảm bảo tính hai chiều)
    public void setShoppingCart(ShoppingCart shoppingCart) {
        if (shoppingCart == null) {
            if (this.shoppingCart != null) {
                this.shoppingCart.setUser(null);
            }
        } else {
            shoppingCart.setUser(this);
        }
        this.shoppingCart = shoppingCart;
    }
}
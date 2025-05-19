package com.ecom.productcatalog.repository;

import com.ecom.productcatalog.model.CartItem;
import com.ecom.productcatalog.model.Product;
import com.ecom.productcatalog.model.ShoppingCart;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CartItemRepository extends JpaRepository<CartItem, Long> {
    // Tìm CartItem dựa trên ShoppingCart và Product (để kiểm tra sản phẩm đã có trong giỏ chưa)
    Optional<CartItem> findByShoppingCartAndProduct(ShoppingCart shoppingCart, Product product);

    // (Tùy chọn) Xóa tất cả CartItem của một ShoppingCart
    void deleteByShoppingCart(ShoppingCart shoppingCart);
}
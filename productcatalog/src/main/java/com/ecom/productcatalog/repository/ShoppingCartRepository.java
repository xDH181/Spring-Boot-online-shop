package com.ecom.productcatalog.repository;

import com.ecom.productcatalog.model.ShoppingCart;
import com.ecom.productcatalog.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ShoppingCartRepository extends JpaRepository<ShoppingCart, Long> {
    // Tìm ShoppingCart dựa trên User
    Optional<ShoppingCart> findByUser(User user);
    Optional<ShoppingCart> findByUserId(Long userId); // Hoặc tìm theo UserId trực tiếp
}
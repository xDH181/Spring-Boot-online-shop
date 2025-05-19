package com.ecom.productcatalog.repository;

import com.ecom.productcatalog.model.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional; // THÊM IMPORT NÀY

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {
    // THÊM PHƯƠNG THỨC NÀY:
    Optional<Category> findByName(String name);
}
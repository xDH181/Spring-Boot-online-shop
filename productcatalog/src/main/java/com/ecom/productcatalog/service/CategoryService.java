package com.ecom.productcatalog.service;

import com.ecom.productcatalog.model.Category;
import com.ecom.productcatalog.repository.CategoryRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CategoryService {
    private final CategoryRepository categoryRepository;

    public CategoryService(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    public List<Category> getAllCategories() {
        return categoryRepository.findAll();
    }

    // Thêm các phương thức khác nếu cần, ví dụ:
    public Category getCategoryById(Long id) {
        return categoryRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("Category not found with id: " + id));
    }
    public Category createCategory(Category category) {
        // Có thể thêm logic kiểm tra tên category đã tồn tại chưa
        return categoryRepository.save(category);
    }
    public Category updateCategory(Long id, Category categoryDetails) {
        Category category = getCategoryById(id);
        category.setName(categoryDetails.getName());
        // Cập nhật các trường khác nếu có
        return categoryRepository.save(category);
    }
    public void deleteCategory(Long id) {
        // Có thể thêm logic kiểm tra category có sản phẩm nào không trước khi xóa
        categoryRepository.deleteById(id);
    }
}
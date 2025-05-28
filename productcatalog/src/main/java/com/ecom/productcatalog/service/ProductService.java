package com.ecom.productcatalog.service;

import com.ecom.productcatalog.model.Category; // Thêm import này
import com.ecom.productcatalog.model.Product;
import com.ecom.productcatalog.repository.CategoryRepository; // Thêm import này
import com.ecom.productcatalog.repository.ProductRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ProductService {
    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository; // Thêm CategoryRepository

    // Cập nhật constructor
    public ProductService(ProductRepository productRepository, CategoryRepository categoryRepository){
        this.productRepository = productRepository;
        this.categoryRepository = categoryRepository; // Gán CategoryRepository
    }

    public List<Product> getAllProducts(){
        return productRepository.findAll(); // Có thể thêm Pageable để phân trang
    }

    public List<Product> getProductsByCategory(Long categoryId){
        // Kiểm tra xem categoryId có tồn tại không nếu cần
        if (!categoryRepository.existsById(categoryId)) {
            throw new EntityNotFoundException("Category not found with id: " + categoryId + " when fetching products.");
        }
        return productRepository.findByCategoryId(categoryId);
    }

    public Product getProductById(Long id) { // Thêm phương thức này
        return productRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Product not found with id: " + id));
    }

    @Transactional
    public Product addProduct(Product product) {
        // Kiểm tra xem category có tồn tại không trước khi gán
        if (product.getCategory() == null || product.getCategory().getId() == null) {
            throw new IllegalArgumentException("Product category cannot be null.");
        }
        Category category = categoryRepository.findById(product.getCategory().getId())
                .orElseThrow(() -> new EntityNotFoundException("Category not found with id: " + product.getCategory().getId()));
        product.setCategory(category); // Gán category đã được quản lý bởi JPA

        if (product.getStockQuantity() == null) { // Đảm bảo stockQuantity không null
            product.setStockQuantity(0);
        } else if (product.getStockQuantity() < 0) {
            throw new IllegalArgumentException("Stock quantity cannot be negative.");
        }
        return productRepository.save(product);
    }

    @Transactional
    public Product updateProduct(Long id, Product productDetails) {
        Product product = getProductById(id); // Sử dụng phương thức đã tạo để lấy product

        product.setName(productDetails.getName());
        product.setDescription(productDetails.getDescription());
        product.setImageUrl(productDetails.getImageUrl());
        product.setPrice(productDetails.getPrice());

        // Cập nhật category nếu được cung cấp
        if (productDetails.getCategory() != null && productDetails.getCategory().getId() != null) {
            Category category = categoryRepository.findById(productDetails.getCategory().getId())
                    .orElseThrow(() -> new EntityNotFoundException("Category not found with id: " + productDetails.getCategory().getId()));
            product.setCategory(category);
        } else if (productDetails.getCategory() != null && productDetails.getCategory().getName() != null) {
            // Nếu chỉ có tên category, thử tìm hoặc tạo mới (tùy logic)
            // Ở đây giả sử phải có ID hoặc category object hợp lệ từ client
            throw new IllegalArgumentException("Category ID must be provided for updating product category.");
        }


        if (productDetails.getStockQuantity() != null) {
            if (productDetails.getStockQuantity() < 0) {
                throw new IllegalArgumentException("Stock quantity cannot be negative.");
            }
            product.setStockQuantity(productDetails.getStockQuantity());
        }
        return productRepository.save(product);
    }

    @Transactional
    public void deleteProduct(Long id) {
        if (!productRepository.existsById(id)) {
            throw new EntityNotFoundException("Product not found with id: " + id + ", cannot delete.");
        }
        productRepository.deleteById(id);
    }

    @Transactional
    public Product updateStockQuantity(Long productId, Integer newQuantity) {
        if (newQuantity == null) {
            throw new IllegalArgumentException("New quantity cannot be null.");
        }
        if (newQuantity < 0) {
            throw new IllegalArgumentException("Stock quantity cannot be negative.");
        }
        Product product = getProductById(productId);
        product.setStockQuantity(newQuantity);
        return productRepository.save(product);
    }
}
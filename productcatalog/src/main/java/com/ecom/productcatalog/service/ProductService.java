package com.ecom.productcatalog.service;

import com.ecom.productcatalog.model.Product;
import com.ecom.productcatalog.repository.ProductRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ProductService {
    private final ProductRepository productRepository;

    public ProductService(ProductRepository productRepository){
        this.productRepository = productRepository;
    }

    public List<Product> getAllProducts(){
        return productRepository.findAll();
    }

    public List<Product> getProductsByCategory(Long categoryId){
        return productRepository.findByCategoryId(categoryId);
    }

    @Transactional
    public Product addProduct(Product product) {
        // stockQuantity đã là một phần của đối tượng Product được truyền vào
        return productRepository.save(product);
    }

    @Transactional
    public Product updateProduct(Long id, Product productDetails) {
        return productRepository.findById(id).map(product -> {
            product.setName(productDetails.getName());
            product.setDescription(productDetails.getDescription());
            product.setImageUrl(productDetails.getImageUrl());
            product.setPrice(productDetails.getPrice());
            product.setCategory(productDetails.getCategory());
            // Cập nhật số lượng tồn kho nếu được cung cấp và hợp lệ
            if (productDetails.getStockQuantity() != null) {
                if (productDetails.getStockQuantity() < 0) {
                    throw new IllegalArgumentException("Stock quantity cannot be negative.");
                }
                product.setStockQuantity(productDetails.getStockQuantity());
            }
            return productRepository.save(product);
        }).orElseThrow(() -> new EntityNotFoundException("Product not found with id: " + id));
    }

    @Transactional
    public void deleteProduct(Long id) {
        if (!productRepository.existsById(id)) {
            throw new EntityNotFoundException("Product not found with id: " + id);
        }
        productRepository.deleteById(id);
    }

    @Transactional
    public Product updateStockQuantity(Long productId, Integer newQuantity) {
        if (newQuantity < 0) {
            throw new IllegalArgumentException("Stock quantity cannot be negative.");
        }
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new EntityNotFoundException("Product not found with id: " + productId));
        product.setStockQuantity(newQuantity);
        return productRepository.save(product);
    }
}
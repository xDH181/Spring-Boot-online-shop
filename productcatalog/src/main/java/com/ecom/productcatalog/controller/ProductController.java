package com.ecom.productcatalog.controller;

import com.ecom.productcatalog.model.Product;
import com.ecom.productcatalog.service.ProductService;
import jakarta.validation.Valid; // Thêm để validate Product object nếu có annotation trong Product
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/products")
public class ProductController {
    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @GetMapping
    public List<Product> getAllProducts(){ // Đổi tên phương thức cho rõ ràng hơn
        return productService.getAllProducts();
    }

    @GetMapping("/category/{categoryId}")
    public List<Product> getAllProductsByCategory(@PathVariable Long categoryId){
        return productService.getProductsByCategory(categoryId);
    }

    // Endpoint để lấy chi tiết một sản phẩm (thêm nếu cần)
    @GetMapping("/{id}")
    public ResponseEntity<Product> getProductById(@PathVariable Long id) {
        // ProductService cần có phương thức getProductById(id)
        // Hoặc bạn có thể dùng productRepository.findById(id) trực tiếp nếu muốn
        // Ví dụ:
        try {
            // Giả sử ProductService có phương thức này, hoặc bạn tự thêm vào
            // Product product = productService.getProductById(id);
            // return ResponseEntity.ok(product);
            return productService.getAllProducts().stream() // Tạm thời lấy từ list all
                    .filter(p -> p.getId().equals(id)).findFirst()
                    .map(ResponseEntity::ok)
                    .orElseGet(() -> ResponseEntity.notFound().build());
        } catch (jakarta.persistence.EntityNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }


    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Product> createProduct(@Valid @RequestBody Product product) {
        // @Valid sẽ kiểm tra các annotation validation trong Product entity, ví dụ @Min(0) cho stockQuantity
        Product newProduct = productService.addProduct(product);
        return ResponseEntity.status(HttpStatus.CREATED).body(newProduct);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Product> updateProduct(@PathVariable Long id, @Valid @RequestBody Product productDetails) {
        Product updatedProduct = productService.updateProduct(id, productDetails);
        return ResponseEntity.ok(updatedProduct);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteProduct(@PathVariable Long id) {
        productService.deleteProduct(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/stock")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Product> updateProductStock(
            @PathVariable Long id,
            @RequestBody Map<String, Integer> stockUpdate
    ) {
        Integer newQuantity = stockUpdate.get("quantity");
        if (newQuantity == null) {
            // Trả về lỗi cụ thể hơn thay vì chỉ body(null)
            return ResponseEntity.badRequest().build(); // Hoặc: ResponseEntity.badRequest().body(Map.of("error", "Quantity field is missing or invalid"));
        }
        // try-catch đã được chuyển vào ProductService, controller không cần bắt lại nếu service đã throw
        Product updatedProduct = productService.updateStockQuantity(id, newQuantity);
        return ResponseEntity.ok(updatedProduct);
    }
}
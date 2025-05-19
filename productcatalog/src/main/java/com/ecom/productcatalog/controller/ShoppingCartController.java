package com.ecom.productcatalog.controller;

import com.ecom.productcatalog.dto.AddItemToCartRequestDTO;
import com.ecom.productcatalog.dto.ShoppingCartResponseDTO;
import com.ecom.productcatalog.dto.UpdateCartItemQuantityRequestDTO;
import com.ecom.productcatalog.service.ShoppingCartService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/cart")
@PreAuthorize("hasRole('USER')") // Tất cả các API trong controller này yêu cầu vai trò USER
public class ShoppingCartController {

    private final ShoppingCartService shoppingCartService;

    public ShoppingCartController(ShoppingCartService shoppingCartService) {
        this.shoppingCartService = shoppingCartService;
    }

    private String getCurrentUsername() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new IllegalStateException("User not authenticated"); // Hoặc xử lý khác
        }
        return authentication.getName();
    }

    // Lấy giỏ hàng của người dùng hiện tại
    @GetMapping
    public ResponseEntity<ShoppingCartResponseDTO> getMyCart() {
        String username = getCurrentUsername();
        ShoppingCartResponseDTO cartDTO = shoppingCartService.getCartByUsername(username);
        return ResponseEntity.ok(cartDTO);
    }

    // Thêm sản phẩm vào giỏ hàng
    @PostMapping("/items")
    public ResponseEntity<ShoppingCartResponseDTO> addItemToMyCart(@Valid @RequestBody AddItemToCartRequestDTO addItemRequest) {
        String username = getCurrentUsername();
        ShoppingCartResponseDTO updatedCart = shoppingCartService.addItemToCart(username, addItemRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(updatedCart);
    }

    // Cập nhật số lượng của một mục trong giỏ hàng
    @PutMapping("/items/{cartItemId}")
    public ResponseEntity<ShoppingCartResponseDTO> updateMyCartItem(
            @PathVariable Long cartItemId,
            @Valid @RequestBody UpdateCartItemQuantityRequestDTO updateRequest) {
        String username = getCurrentUsername();
        ShoppingCartResponseDTO updatedCart = shoppingCartService.updateCartItemQuantity(username, cartItemId, updateRequest);
        return ResponseEntity.ok(updatedCart);
    }

    // Xóa một mục khỏi giỏ hàng
    @DeleteMapping("/items/{cartItemId}")
    public ResponseEntity<ShoppingCartResponseDTO> removeItemFromMyCart(@PathVariable Long cartItemId) {
        String username = getCurrentUsername();
        ShoppingCartResponseDTO updatedCart = shoppingCartService.removeItemFromCart(username, cartItemId);
        return ResponseEntity.ok(updatedCart);
    }

    // Xóa tất cả các mục khỏi giỏ hàng (làm trống giỏ hàng)
    @DeleteMapping
    public ResponseEntity<ShoppingCartResponseDTO> clearMyCart() {
        String username = getCurrentUsername();
        ShoppingCartResponseDTO clearedCart = shoppingCartService.clearCart(username);
        return ResponseEntity.ok(clearedCart);
    }
}
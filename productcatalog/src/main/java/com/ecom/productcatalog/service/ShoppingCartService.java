package com.ecom.productcatalog.service;

import com.ecom.productcatalog.dto.*;
import com.ecom.productcatalog.exception.InsufficientStockException;
import com.ecom.productcatalog.model.*;
import com.ecom.productcatalog.repository.*;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class ShoppingCartService {

    private final ShoppingCartRepository shoppingCartRepository;
    private final CartItemRepository cartItemRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;

    public ShoppingCartService(ShoppingCartRepository shoppingCartRepository,
                               CartItemRepository cartItemRepository,
                               UserRepository userRepository,
                               ProductRepository productRepository) {
        this.shoppingCartRepository = shoppingCartRepository;
        this.cartItemRepository = cartItemRepository;
        this.userRepository = userRepository;
        this.productRepository = productRepository;
    }

    // Lấy hoặc tạo giỏ hàng cho người dùng
    @Transactional
    protected ShoppingCart getOrCreateCartForUser(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));

        return shoppingCartRepository.findByUser(user)
                .orElseGet(() -> {
                    ShoppingCart newCart = new ShoppingCart(user);
                    // user.setShoppingCart(newCart); // ShoppingCart constructor đã làm điều này nếu bạn có logic setUser trong constructor của ShoppingCart
                    return shoppingCartRepository.save(newCart);
                });
    }

    @Transactional
    public ShoppingCartResponseDTO addItemToCart(String username, AddItemToCartRequestDTO requestDTO) {
        ShoppingCart cart = getOrCreateCartForUser(username);
        Product product = productRepository.findById(requestDTO.productId())
                .orElseThrow(() -> new EntityNotFoundException("Product not found with ID: " + requestDTO.productId()));

        if (product.getStockQuantity() < requestDTO.quantity()) {
            throw new InsufficientStockException("Not enough stock for product: " + product.getName() +
                    ". Available: " + product.getStockQuantity() + ", Requested: " + requestDTO.quantity());
        }

        Optional<CartItem> existingCartItemOpt = cartItemRepository.findByShoppingCartAndProduct(cart, product);

        if (existingCartItemOpt.isPresent()) {
            // Sản phẩm đã có trong giỏ, cập nhật số lượng
            CartItem existingCartItem = existingCartItemOpt.get();
            int newQuantity = existingCartItem.getQuantity() + requestDTO.quantity();
            if (product.getStockQuantity() < newQuantity) {
                throw new InsufficientStockException("Not enough stock for product: " + product.getName() +
                        ". Available: " + product.getStockQuantity() + ", Requested total: " + newQuantity);
            }
            existingCartItem.setQuantity(newQuantity);
            cartItemRepository.save(existingCartItem);
        } else {
            // Sản phẩm chưa có trong giỏ, tạo mới CartItem
            CartItem newCartItem = new CartItem(cart, product, requestDTO.quantity());
            cart.addItem(newCartItem); // cartItemRepository.save(newCartItem) sẽ được thực hiện bởi cascade từ cart
        }
        // cart.setLastModifiedDate(LocalDateTime.now()); // @UpdateTimestamp sẽ tự động làm việc này
        ShoppingCart updatedCart = shoppingCartRepository.save(cart);
        return mapToShoppingCartResponseDTO(updatedCart);
    }

    @Transactional(readOnly = true)
    public ShoppingCartResponseDTO getCartByUsername(String username) {
        ShoppingCart cart = getOrCreateCartForUser(username); // Đảm bảo user luôn có cart khi truy cập
        return mapToShoppingCartResponseDTO(cart);
    }

    @Transactional
    public ShoppingCartResponseDTO updateCartItemQuantity(String username, Long cartItemId, UpdateCartItemQuantityRequestDTO requestDTO) {
        ShoppingCart cart = getOrCreateCartForUser(username);
        CartItem cartItem = cartItemRepository.findById(cartItemId)
                .orElseThrow(() -> new EntityNotFoundException("CartItem not found with ID: " + cartItemId));

        if (!cartItem.getShoppingCart().getId().equals(cart.getId())) {
            throw new SecurityException("CartItem does not belong to the current user's cart.");
        }

        Product product = cartItem.getProduct();
        int newQuantity = requestDTO.quantity();

        if (newQuantity < 0) {
            throw new IllegalArgumentException("Quantity cannot be negative.");
        }

        if (newQuantity == 0) {
            // Nếu số lượng là 0, xóa CartItem
            cart.removeItem(cartItem); // Điều này sẽ kích hoạt orphanRemoval
            cartItemRepository.delete(cartItem); // Xóa rõ ràng nếu orphanRemoval không được cấu hình hoàn hảo hoặc để chắc chắn
        } else {
            if (product.getStockQuantity() < newQuantity) {
                throw new InsufficientStockException("Not enough stock for product: " + product.getName() +
                        ". Available: " + product.getStockQuantity() + ", Requested: " + newQuantity);
            }
            cartItem.setQuantity(newQuantity);
            cartItemRepository.save(cartItem);
        }
        // cart.setLastModifiedDate(LocalDateTime.now());
        ShoppingCart updatedCart = shoppingCartRepository.save(cart); // Lưu lại cart để cập nhật lastModifiedDate
        return mapToShoppingCartResponseDTO(updatedCart);
    }

    @Transactional
    public ShoppingCartResponseDTO removeItemFromCart(String username, Long cartItemId) {
        ShoppingCart cart = getOrCreateCartForUser(username);
        CartItem cartItem = cartItemRepository.findById(cartItemId)
                .orElseThrow(() -> new EntityNotFoundException("CartItem not found with ID: " + cartItemId));

        if (!cartItem.getShoppingCart().getId().equals(cart.getId())) {
            throw new SecurityException("CartItem does not belong to the current user's cart.");
        }

        cart.removeItem(cartItem); // Sẽ kích hoạt orphanRemoval nếu cascade đúng
        cartItemRepository.delete(cartItem); // Xóa rõ ràng để chắc chắn
        // cart.setLastModifiedDate(LocalDateTime.now());
        ShoppingCart updatedCart = shoppingCartRepository.save(cart);
        return mapToShoppingCartResponseDTO(updatedCart);
    }

    @Transactional
    public ShoppingCartResponseDTO clearCart(String username) {
        ShoppingCart cart = getOrCreateCartForUser(username);
        // Xóa tất cả CartItems liên quan đến giỏ hàng này
        // Cách 1: Dùng orphanRemoval (nếu items được clear từ Set trong ShoppingCart entity)
        // cart.getItems().clear(); // Điều này sẽ kích hoạt orphanRemoval
        // Cách 2: Xóa trực tiếp qua cartItemRepository (an toàn hơn và rõ ràng hơn)
        cartItemRepository.deleteByShoppingCart(cart); // Cần thêm phương thức này vào CartItemRepository
        cart.getItems().clear(); // Đồng bộ hóa collection trong memory với DB state
        // cart.setLastModifiedDate(LocalDateTime.now());
        ShoppingCart updatedCart = shoppingCartRepository.save(cart);
        return mapToShoppingCartResponseDTO(updatedCart); // Giỏ hàng giờ sẽ trống
    }

    // Helper method để map ShoppingCart entity sang ShoppingCartResponseDTO
    private ShoppingCartResponseDTO mapToShoppingCartResponseDTO(ShoppingCart cart) {
        if (cart == null) return null; // Hoặc trả về một giỏ hàng rỗng DTO

        List<CartItemResponseDTO> itemResponseDTOs = cart.getItems().stream()
                .map(cartItem -> new CartItemResponseDTO(
                        cartItem.getId(),
                        new ProductInfoDTO( // Giả sử ProductInfoDTO đã được định nghĩa
                                cartItem.getProduct().getId(),
                                cartItem.getProduct().getName(),
                                cartItem.getProduct().getImageUrl()
                        ),
                        cartItem.getQuantity(),
                        cartItem.getProduct().getPrice(), // Lấy giá hiện tại của sản phẩm
                        cartItem.getQuantity() * cartItem.getProduct().getPrice() // Subtotal
                ))
                .collect(Collectors.toList());

        double totalAmount = itemResponseDTOs.stream()
                .mapToDouble(CartItemResponseDTO::subtotal)
                .sum();

        int totalItemsCount = itemResponseDTOs.stream()
                .mapToInt(CartItemResponseDTO::quantity)
                .sum();

        User user = cart.getUser(); // Lấy user từ cart
        String username = (user != null) ? user.getUsername() : "Unknown";
        Long userId = (user != null) ? user.getId() : null;


        return new ShoppingCartResponseDTO(
                cart.getId(),
                userId,
                username,
                itemResponseDTOs,
                totalAmount,
                totalItemsCount,
                cart.getLastModifiedDate() != null ? cart.getLastModifiedDate() : LocalDateTime.now() // Đảm bảo không null
        );
    }
}
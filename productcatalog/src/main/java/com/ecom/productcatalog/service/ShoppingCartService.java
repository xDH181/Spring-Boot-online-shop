package com.ecom.productcatalog.service;

import com.ecom.productcatalog.dto.*;
import com.ecom.productcatalog.exception.InsufficientStockException;
import com.ecom.productcatalog.model.*;
import com.ecom.productcatalog.repository.*;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.security.core.userdetails.UsernameNotFoundException; // Đảm bảo import đúng
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.slf4j.Logger; // Thêm logger
import org.slf4j.LoggerFactory; // Thêm logger

import java.time.LocalDateTime;
// import java.util.ArrayList; // Không dùng
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class ShoppingCartService {
    private static final Logger logger = LoggerFactory.getLogger(ShoppingCartService.class); // Logger

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

    @Transactional
    protected ShoppingCart getOrCreateCartForUser(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> {
                    logger.warn("User not found while trying to get or create cart: {}", username);
                    return new UsernameNotFoundException("User not found: " + username);
                });

        return shoppingCartRepository.findByUser(user)
                .orElseGet(() -> {
                    logger.info("Creating new shopping cart for user: {}", username);
                    ShoppingCart newCart = new ShoppingCart(user);
                    // user.setShoppingCart(newCart); // Đã được xử lý trong constructor của ShoppingCart hoặc setter của User
                    return shoppingCartRepository.save(newCart);
                });
    }

    @Transactional
    public ShoppingCartResponseDTO addItemToCart(String username, AddItemToCartRequestDTO requestDTO) {
        if (requestDTO.quantity() <= 0) {
            throw new IllegalArgumentException("Quantity to add must be positive.");
        }
        ShoppingCart cart = getOrCreateCartForUser(username);
        Product product = productRepository.findById(requestDTO.productId())
                .orElseThrow(() -> new EntityNotFoundException("Product not found with ID: " + requestDTO.productId()));

        Optional<CartItem> existingCartItemOpt = cartItemRepository.findByShoppingCartAndProduct(cart, product);

        if (existingCartItemOpt.isPresent()) {
            CartItem existingCartItem = existingCartItemOpt.get();
            int newQuantity = existingCartItem.getQuantity() + requestDTO.quantity();
            if (product.getStockQuantity() < newQuantity) { // Kiểm tra lại tổng số lượng mới với kho
                throw new InsufficientStockException("Not enough stock for product: " + product.getName() +
                        ". Available: " + product.getStockQuantity() + ", Requested total: " + newQuantity);
            }
            existingCartItem.setQuantity(newQuantity);
            logger.info("Updated quantity for product {} in cart {} to {}", product.getName(), cart.getId(), newQuantity);
            cartItemRepository.save(existingCartItem);
        } else {
            if (product.getStockQuantity() < requestDTO.quantity()) { // Kiểm tra kho cho sản phẩm mới
                throw new InsufficientStockException("Not enough stock for product: " + product.getName() +
                        ". Available: " + product.getStockQuantity() + ", Requested: " + requestDTO.quantity());
            }
            CartItem newCartItem = new CartItem(cart, product, requestDTO.quantity());
            cart.addItem(newCartItem);
            logger.info("Added new product {} to cart {}", product.getName(), cart.getId());
            // cartItemRepository.save(newCartItem) sẽ được cascade từ cart
        }
        // cart.setLastModifiedDate(LocalDateTime.now()); // @UpdateTimestamp sẽ tự động làm
        ShoppingCart updatedCart = shoppingCartRepository.save(cart); // Lưu cart để kích hoạt @UpdateTimestamp
        return mapToShoppingCartResponseDTO(updatedCart);
    }

    @Transactional(readOnly = true)
    public ShoppingCartResponseDTO getCartByUsername(String username) {
        ShoppingCart cart = getOrCreateCartForUser(username);
        return mapToShoppingCartResponseDTO(cart);
    }

    @Transactional
    public ShoppingCartResponseDTO updateCartItemQuantity(String username, Long cartItemId, UpdateCartItemQuantityRequestDTO requestDTO) {
        ShoppingCart cart = getOrCreateCartForUser(username); // Lấy giỏ hàng của user
        CartItem cartItem = cartItemRepository.findById(cartItemId)
                .orElseThrow(() -> new EntityNotFoundException("CartItem not found with ID: " + cartItemId));

        // Kiểm tra xem cartItem có thuộc giỏ hàng của user không
        if (!cartItem.getShoppingCart().getId().equals(cart.getId())) {
            // Ném lỗi bảo mật hoặc nghiệp vụ phù hợp
            throw new SecurityException("CartItem does not belong to the current user's cart.");
        }

        Product product = cartItem.getProduct();
        int newQuantity = requestDTO.quantity();

        if (newQuantity < 0) {
            throw new IllegalArgumentException("Quantity cannot be negative.");
        }

        if (newQuantity == 0) {
            logger.info("Removing cartItem {} (product {}) from cart {} due to zero quantity", cartItemId, product.getName(), cart.getId());
            cart.removeItem(cartItem); // Kích hoạt orphanRemoval
            // cartItemRepository.delete(cartItem); // Có thể không cần nếu orphanRemoval hoạt động đúng
        } else {
            if (product.getStockQuantity() < newQuantity) { // Kiểm tra lại với số lượng hiện có trong kho
                throw new InsufficientStockException("Not enough stock for product: " + product.getName() +
                        ". Available: " + product.getStockQuantity() + ", Requested: " + newQuantity);
            }
            cartItem.setQuantity(newQuantity);
            logger.info("Updated quantity for cartItem {} (product {}) in cart {} to {}", cartItemId, product.getName(), cart.getId(), newQuantity);
            cartItemRepository.save(cartItem);
        }
        ShoppingCart updatedCart = shoppingCartRepository.save(cart);
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

        logger.info("Removing cartItem {} (product {}) from cart {}", cartItemId, cartItem.getProduct().getName(), cart.getId());
        cart.removeItem(cartItem);
        // cartItemRepository.delete(cartItem); // Có thể không cần nếu orphanRemoval hoạt động
        ShoppingCart updatedCart = shoppingCartRepository.save(cart);
        return mapToShoppingCartResponseDTO(updatedCart);
    }

    @Transactional
    public ShoppingCartResponseDTO clearCart(String username) {
        ShoppingCart cart = getOrCreateCartForUser(username);
        if (cart.getItems().isEmpty()) {
            logger.info("Cart for user {} is already empty. No action taken.", username);
            return mapToShoppingCartResponseDTO(cart); // Trả về giỏ hàng rỗng
        }
        logger.info("Clearing all items from cart for user: {}", username);
        // cart.getItems().clear(); // Điều này sẽ kích hoạt orphanRemoval cho từng item
        // Hoặc xóa trực tiếp qua repository (có thể hiệu quả hơn cho nhiều items)
        cartItemRepository.deleteByShoppingCart(cart); // Cần phương thức này trong CartItemRepository
        cart.getItems().clear(); // Quan trọng: đồng bộ collection trong memory

        ShoppingCart updatedCart = shoppingCartRepository.save(cart);
        return mapToShoppingCartResponseDTO(updatedCart);
    }

    private ShoppingCartResponseDTO mapToShoppingCartResponseDTO(ShoppingCart cart) {
        if (cart == null) {
            logger.warn("Attempted to map a null shopping cart to DTO.");
            // Trả về một DTO giỏ hàng rỗng mặc định hoặc ném lỗi tùy theo logic nghiệp vụ
            return new ShoppingCartResponseDTO(null, null, "Unknown", List.of(), 0.0, 0, LocalDateTime.now());
        }

        List<CartItemResponseDTO> itemResponseDTOs = cart.getItems().stream()
                .map(cartItem -> {
                    Product product = cartItem.getProduct();
                    if (product == null) {
                        logger.error("CartItem {} in cart {} has a null product. This should not happen.", cartItem.getId(), cart.getId());
                        // Xử lý trường hợp product bị null (ví dụ: bỏ qua item này hoặc trả về DTO với thông tin lỗi)
                        return null; // Hoặc một CartItemResponseDTO với thông tin lỗi
                    }
                    return new CartItemResponseDTO(
                            cartItem.getId(),
                            new ProductInfoDTO(
                                    product.getId(),
                                    product.getName(),
                                    product.getImageUrl()
                            ),
                            cartItem.getQuantity(),
                            product.getPrice(),
                            cartItem.getQuantity() * product.getPrice()
                    );
                })
                .filter(java.util.Objects::nonNull) // Loại bỏ các item null nếu có lỗi product null
                .collect(Collectors.toList());

        double totalAmount = itemResponseDTOs.stream()
                .mapToDouble(CartItemResponseDTO::subtotal)
                .sum();

        int totalItemsCount = itemResponseDTOs.stream()
                .mapToInt(CartItemResponseDTO::quantity)
                .sum();

        User user = cart.getUser();
        String cartUsername = (user != null) ? user.getUsername() : "UnknownUser"; // Đổi tên biến để tránh trùng
        Long userId = (user != null) ? user.getId() : null;


        return new ShoppingCartResponseDTO(
                cart.getId(),
                userId,
                cartUsername,
                itemResponseDTOs,
                totalAmount,
                totalItemsCount,
                cart.getLastModifiedDate() != null ? cart.getLastModifiedDate() : LocalDateTime.now()
        );
    }
}
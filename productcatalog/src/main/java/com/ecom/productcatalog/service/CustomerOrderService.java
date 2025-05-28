package com.ecom.productcatalog.service;

import com.ecom.productcatalog.dto.OrderItemRequestDTO;
import com.ecom.productcatalog.dto.OrderRequestDTO;
import com.ecom.productcatalog.dto.OrderResponseDTO;
import com.ecom.productcatalog.dto.OrderItemResponseDTO;
import com.ecom.productcatalog.dto.ProductInfoDTO;
import com.ecom.productcatalog.exception.InsufficientStockException;
import com.ecom.productcatalog.model.CustomerOrder;
import com.ecom.productcatalog.model.OrderItem;
import com.ecom.productcatalog.model.OrderStatus;
import com.ecom.productcatalog.model.Product;
import com.ecom.productcatalog.model.User;
import com.ecom.productcatalog.repository.CustomerOrderRepository;
import com.ecom.productcatalog.repository.ProductRepository;
import com.ecom.productcatalog.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException; // Đảm bảo import đúng
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

// import java.time.LocalDateTime; // Không cần thiết ở đây vì CustomerOrder tự quản lý
import java.util.List;
import java.util.stream.Collectors;

@Service
public class CustomerOrderService {

    private final CustomerOrderRepository customerOrderRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    // private final ShoppingCartService shoppingCartService; // Cân nhắc inject nếu cần xóa cart sau khi order

    public CustomerOrderService(CustomerOrderRepository customerOrderRepository,
                                ProductRepository productRepository,
                                UserRepository userRepository
            /*, ShoppingCartService shoppingCartService */) {
        this.customerOrderRepository = customerOrderRepository;
        this.productRepository = productRepository;
        this.userRepository = userRepository;
        // this.shoppingCartService = shoppingCartService;
    }

    @Transactional
    public OrderResponseDTO createOrder(OrderRequestDTO orderRequestDTO, String username) {
        User currentUser = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with username: " + username));

        CustomerOrder customerOrder = new CustomerOrder();
        customerOrder.setUser(currentUser);
        // orderDate và status sẽ được tự động thiết lập bởi @PrePersist trong CustomerOrder

        double totalAmount = 0.0;

        if (orderRequestDTO.items() == null || orderRequestDTO.items().isEmpty()) {
            throw new IllegalArgumentException("Order must contain at least one item.");
        }

        for (OrderItemRequestDTO itemDTO : orderRequestDTO.items()) {
            Product product = productRepository.findById(itemDTO.productId())
                    .orElseThrow(() -> new EntityNotFoundException("Product not found with ID: " + itemDTO.productId()));

            if (product.getStockQuantity() < itemDTO.quantity()) {
                throw new InsufficientStockException("Insufficient stock for product: " + product.getName() +
                        ". Available: " + product.getStockQuantity() + ", Requested: " + itemDTO.quantity());
            }
            if (itemDTO.quantity() <= 0) {
                throw new IllegalArgumentException("Item quantity must be positive for product: " + product.getName());
            }

            OrderItem orderItem = new OrderItem();
            orderItem.setProduct(product);
            orderItem.setQuantity(itemDTO.quantity());
            orderItem.setPriceAtPurchase(product.getPrice()); // Lưu giá tại thời điểm mua

            customerOrder.addOrderItem(orderItem); // Phương thức helper trong CustomerOrder sẽ set quan hệ hai chiều
            totalAmount += (product.getPrice() * itemDTO.quantity());

            // Giảm số lượng tồn kho
            product.setStockQuantity(product.getStockQuantity() - itemDTO.quantity());
            productRepository.save(product); // Lưu thay đổi của sản phẩm
        }

        customerOrder.setTotalAmount(totalAmount);
        // customerOrder.setStatus(OrderStatus.PENDING); // Đã được xử lý bởi @PrePersist
        CustomerOrder savedOrder = customerOrderRepository.save(customerOrder);

        // Optional: Xóa giỏ hàng sau khi đặt hàng thành công
        // shoppingCartService.clearCart(username);

        return mapToOrderResponseDTO(savedOrder);
    }

    public List<OrderResponseDTO> getOrdersByUsername(String username) {
        User currentUser = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with username: " + username));
        List<CustomerOrder> orders = customerOrderRepository.findByUserOrderByIdDesc(currentUser);
        return orders.stream()
                .map(this::mapToOrderResponseDTO)
                .collect(Collectors.toList());
    }

    public OrderResponseDTO getOrderDetailsByIdAndUsername(Long orderId, String username) {
        User currentUser = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with username: " + username));
        CustomerOrder order = customerOrderRepository.findById(orderId)
                .orElseThrow(() -> new EntityNotFoundException("Order not found with ID: " + orderId));

        if (!order.getUser().getId().equals(currentUser.getId())) {
            // Dùng SecurityException hoặc AccessDeniedException của Spring
            throw new org.springframework.security.access.AccessDeniedException("User does not have permission to view this order.");
        }
        return mapToOrderResponseDTO(order);
    }

    // --- Admin specific methods ---

    public List<OrderResponseDTO> getAllOrdersForAdmin() {
        // Cân nhắc thêm Pageable để phân trang nếu số lượng đơn hàng lớn
        return customerOrderRepository.findAll().stream() // Có thể sắp xếp theo ngày đặt hàng hoặc ID
                .map(this::mapToOrderResponseDTO)
                .collect(Collectors.toList());
    }

    public OrderResponseDTO getOrderByIdForAdmin(Long id) {
        CustomerOrder order = customerOrderRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Order not found with ID: " + id));
        return mapToOrderResponseDTO(order);
    }

    @Transactional
    public OrderResponseDTO updateOrderStatusForAdmin(Long orderId, OrderStatus newStatus) {
        CustomerOrder order = customerOrderRepository.findById(orderId)
                .orElseThrow(() -> new EntityNotFoundException("Order not found with id: " + orderId));

        // Thêm logic kiểm tra chuyển đổi trạng thái hợp lệ nếu cần
        // Ví dụ: không thể chuyển từ COMPLETED sang PENDING
        // if (order.getStatus() == OrderStatus.COMPLETED && newStatus == OrderStatus.PENDING) {
        //     throw new IllegalArgumentException("Cannot revert a completed order to pending.");
        // }

        order.setStatus(newStatus);
        CustomerOrder updatedOrder = customerOrderRepository.save(order);
        return mapToOrderResponseDTO(updatedOrder);
    }

    // Helper method để map CustomerOrder sang OrderResponseDTO
    private OrderResponseDTO mapToOrderResponseDTO(CustomerOrder order) {
        List<OrderItemResponseDTO> itemDTOs = order.getOrderItems().stream()
                .map(item -> new OrderItemResponseDTO(
                        item.getId(),
                        new ProductInfoDTO( // ProductInfoDTO cần được định nghĩa
                                item.getProduct().getId(),
                                item.getProduct().getName(),
                                item.getProduct().getImageUrl()
                                // Thêm các thông tin sản phẩm khác nếu cần
                        ),
                        item.getQuantity(),
                        item.getPriceAtPurchase(),
                        item.getQuantity() * item.getPriceAtPurchase() // Tính toán subtotal cho item
                ))
                .collect(Collectors.toList());

        return new OrderResponseDTO(
                order.getId(),
                order.getUser().getUsername(), // Hoặc ID của user tùy theo nhu cầu hiển thị
                order.getOrderDate(),
                order.getStatus(),
                order.getTotalAmount(),
                itemDTOs
        );
    }
}
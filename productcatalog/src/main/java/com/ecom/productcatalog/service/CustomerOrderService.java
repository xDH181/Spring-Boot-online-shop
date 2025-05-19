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
import jakarta.persistence.EntityNotFoundException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class CustomerOrderService {

    private final CustomerOrderRepository customerOrderRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;

    public CustomerOrderService(CustomerOrderRepository customerOrderRepository,
                                ProductRepository productRepository,
                                UserRepository userRepository) {
        this.customerOrderRepository = customerOrderRepository;
        this.productRepository = productRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public OrderResponseDTO createOrder(OrderRequestDTO orderRequestDTO, String username) {
        User currentUser = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with username: " + username));

        CustomerOrder customerOrder = new CustomerOrder();
        customerOrder.setUser(currentUser);
        // orderDate và status sẽ được tự động thiết lập bởi @PrePersist trong CustomerOrder

        double totalAmount = 0.0;

        for (OrderItemRequestDTO itemDTO : orderRequestDTO.items()) {
            Product product = productRepository.findById(itemDTO.productId())
                    .orElseThrow(() -> new EntityNotFoundException("Product not found with ID: " + itemDTO.productId()));

            if (product.getStockQuantity() < itemDTO.quantity()) {
                throw new InsufficientStockException("Insufficient stock for product: " + product.getName() +
                        ". Available: " + product.getStockQuantity() + ", Requested: " + itemDTO.quantity());
            }

            OrderItem orderItem = new OrderItem();
            orderItem.setProduct(product);
            orderItem.setQuantity(itemDTO.quantity());
            orderItem.setPriceAtPurchase(product.getPrice());

            customerOrder.addOrderItem(orderItem);
            totalAmount += (product.getPrice() * itemDTO.quantity());

            product.setStockQuantity(product.getStockQuantity() - itemDTO.quantity());
            productRepository.save(product);
        }

        customerOrder.setTotalAmount(totalAmount);
        CustomerOrder savedOrder = customerOrderRepository.save(customerOrder);
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
            throw new SecurityException("User does not have permission to view this order.");
        }
        return mapToOrderResponseDTO(order);
    }

    public List<OrderResponseDTO> getAllOrdersForAdmin() {
        return customerOrderRepository.findAll().stream()
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
        order.setStatus(newStatus);
        CustomerOrder updatedOrder = customerOrderRepository.save(order);
        return mapToOrderResponseDTO(updatedOrder);
    }

    private OrderResponseDTO mapToOrderResponseDTO(CustomerOrder order) {
        List<OrderItemResponseDTO> itemDTOs = order.getOrderItems().stream()
                .map(item -> new OrderItemResponseDTO(
                        item.getId(),
                        new ProductInfoDTO(
                                item.getProduct().getId(),
                                item.getProduct().getName(),
                                item.getProduct().getImageUrl()
                        ),
                        item.getQuantity(),
                        item.getPriceAtPurchase(),
                        item.getQuantity() * item.getPriceAtPurchase()
                ))
                .collect(Collectors.toList());

        return new OrderResponseDTO(
                order.getId(),
                order.getUser().getUsername(),
                order.getOrderDate(),
                order.getStatus(),
                order.getTotalAmount(),
                itemDTOs
        );
    }
}
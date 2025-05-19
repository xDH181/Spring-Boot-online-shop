package com.ecom.productcatalog.controller;

import com.ecom.productcatalog.dto.OrderRequestDTO;
import com.ecom.productcatalog.dto.OrderResponseDTO;
import com.ecom.productcatalog.model.OrderStatus; // Import OrderStatus
import com.ecom.productcatalog.service.CustomerOrderService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize; // Cho phép phân quyền dựa trên role/authority
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map; // Cho update status

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private final CustomerOrderService orderService;

    public OrderController(CustomerOrderService orderService) {
        this.orderService = orderService;
    }

    // Endpoint cho USER tạo đơn hàng mới
    @PostMapping
    @PreAuthorize("hasRole('USER')") // Chỉ user có vai trò USER mới được tạo đơn hàng
    public ResponseEntity<OrderResponseDTO> createOrder(@Valid @RequestBody OrderRequestDTO orderRequestDTO) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String currentUsername = authentication.getName(); // Lấy username của người dùng đang đăng nhập

        OrderResponseDTO createdOrder = orderService.createOrder(orderRequestDTO, currentUsername);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdOrder);
    }

    // Endpoint cho USER xem lịch sử đơn hàng của chính họ
    @GetMapping("/my-orders")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<List<OrderResponseDTO>> getMyOrders() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String currentUsername = authentication.getName();

        List<OrderResponseDTO> orders = orderService.getOrdersByUsername(currentUsername);
        return ResponseEntity.ok(orders);
    }

    // Endpoint cho USER xem chi tiết một đơn hàng cụ thể của họ
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('USER')") // User chỉ xem được đơn của mình
    public ResponseEntity<OrderResponseDTO> getMyOrderById(@PathVariable Long id) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String currentUsername = authentication.getName();
        try {
            OrderResponseDTO order = orderService.getOrderDetailsByIdAndUsername(id, currentUsername);
            return ResponseEntity.ok(order);
        } catch (SecurityException e) { // Bắt lỗi nếu user cố xem đơn hàng không phải của họ
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(null); // Hoặc thông báo lỗi cụ thể
        } catch (jakarta.persistence.EntityNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }


    // ---- Các Endpoints cho ADMIN ----

    // Endpoint cho ADMIN xem tất cả đơn hàng
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<OrderResponseDTO>> getAllOrdersForAdmin() {
        List<OrderResponseDTO> orders = orderService.getAllOrdersForAdmin();
        return ResponseEntity.ok(orders);
    }

    // Endpoint cho ADMIN xem chi tiết một đơn hàng bất kỳ
    @GetMapping("/admin/{id}") // Phân biệt với endpoint của user
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<OrderResponseDTO> getOrderByIdForAdmin(@PathVariable Long id) {
        try {
            OrderResponseDTO order = orderService.getOrderByIdForAdmin(id);
            return ResponseEntity.ok(order);
        } catch (jakarta.persistence.EntityNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }

    // Endpoint cho ADMIN cập nhật trạng thái đơn hàng
    @PutMapping("/admin/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<OrderResponseDTO> updateOrderStatus(
            @PathVariable Long id,
            @RequestBody Map<String, String> statusUpdate // Nhận JSON вида {"status": "SHIPPED"}
    ) {
        try {
            String newStatusString = statusUpdate.get("status");
            if (newStatusString == null) {
                return ResponseEntity.badRequest().build(); // Hoặc trả về lỗi cụ thể hơn
            }
            OrderStatus newStatus = OrderStatus.valueOf(newStatusString.toUpperCase()); // Chuyển string thành enum
            OrderResponseDTO updatedOrder = orderService.updateOrderStatusForAdmin(id, newStatus);
            return ResponseEntity.ok(updatedOrder);
        } catch (IllegalArgumentException e) { // Nếu tên status không hợp lệ
            return ResponseEntity.badRequest().body(null); // Thông báo lỗi về status không hợp lệ
        } catch (jakarta.persistence.EntityNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }

    // Endpoint DELETE đơn hàng có thể không cần thiết hoặc chỉ dành cho Admin với các điều kiện nghiêm ngặt
    // @DeleteMapping("/{id}")
    // @PreAuthorize("hasRole('ADMIN')")
    // public ResponseEntity<Void> deleteOrder(@PathVariable Long id) {
    //     // Cân nhắc kỹ logic xóa đơn hàng, có thể chỉ nên đánh dấu là "CANCELLED"
    //     // orderService.deleteOrder(id); // Nếu vẫn muốn xóa vật lý
    //     return ResponseEntity.noContent().build();
    // }
}
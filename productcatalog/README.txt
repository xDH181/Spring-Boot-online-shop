PHẦN 1: XÁC THỰC (Authentication)
// 1.1. Đăng nhập với tài khoản USER
// Mục đích: Lấy USER_JWT_TOKEN.
POST http://localhost:8080/api/auth/login
Content-Type: application/json

{
"username": "user",
"password": "user123"
}
// >> Lưu token lại.

// 1.2. Đăng nhập với tài khoản ADMIN
// Mục đích: Lấy ADMIN_JWT_TOKEN.
POST http://localhost:8080/api/auth/login
Content-Type: application/json

{
"username": "admin",
"password": "admin123"
}
// >> Lưu token lại.

PHẦN 2: GIỎ HÀNG (Shopping Cart)
// Yêu cầu Header: Authorization: Bearer <USER_JWT_TOKEN> cho tất cả request trong phần này.

// 2.1. Xem Giỏ Hàng (Rỗng)
GET http://localhost:8080/api/cart

// 2.2. Thêm Sản phẩm A (productId: 1, quantity: 2) vào Giỏ
POST http://localhost:8080/api/cart/items
Content-Type: application/json

{ "productId": 1, "quantity": 2 }
// >> Ghi lại cartItemId của mục này (ví dụ: 1).

// 2.3. Thêm Sản phẩm B (productId: 2, quantity: 1) vào Giỏ
POST http://localhost:8080/api/cart/items
Content-Type: application/json

{ "productId": 2, "quantity": 1 }
// >> Ghi lại cartItemId của mục này (ví dụ: 2).

// 2.4. Thêm tiếp Sản phẩm A (đã có, productId: 1, thêm quantity: 1)
POST http://localhost:8080/api/cart/items
Content-Type: application/json

{ "productId": 1, "quantity": 1 }
// >> Mục sản phẩm A giờ có quantity = 3.

// 2.5. Cập nhật Số lượng Sản phẩm A (cartItemId: 1, thành quantity: 4)
PUT http://localhost:8080/api/cart/items/1
Content-Type: application/json

{ "quantity": 4 }

// 2.6. Cập nhật Số lượng Sản phẩm A (cartItemId: 1, thành quantity: 0 để xóa)
PUT http://localhost:8080/api/cart/items/1
Content-Type: application/json

{ "quantity": 0 }
// >> Mục sản phẩm A bị xóa.

// 2.7. Xóa Sản phẩm B (cartItemId: 2) khỏi Giỏ
DELETE http://localhost:8080/api/cart/items/2

// 2.8. Xem lại Giỏ Hàng (Sau khi đã thêm/sửa/xóa)
GET http://localhost:8080/api/cart

// 2.9. Xóa Toàn bộ Giỏ Hàng
DELETE http://localhost:8080/api/cart

PHẦN 3: ĐẶT HÀNG (Order) & TỒN KHO
// --- Thao tác của ADMIN ---
// Yêu cầu Header: Authorization: Bearer <ADMIN_JWT_TOKEN>

// 3.1. Admin: Cập nhật Tồn kho Sản phẩm (productId: 1, thành quantity: 20)
PATCH http://localhost:8080/api/products/1/stock
Content-Type: application/json

{ "quantity": 20 }

// --- Thao tác của USER ---
// Yêu cầu Header: Authorization: Bearer <USER_JWT_TOKEN>

// 3.2. User: Tạo Đơn hàng (Sản phẩm productId: 1, quantity: 5)
// (Sản phẩm 1 đang có tồn kho là 20)
POST http://localhost:8080/api/orders
Content-Type: application/json

{
"items": [ { "productId": 1, "quantity": 5 } ]
}
// >> Ghi lại orderId trả về.
// >> Kiểm tra DB: stock_quantity của sản phẩm 1 giảm còn 15.

// 3.3. User: Tạo Đơn hàng (Không đủ hàng - Sad Path)
// (Sản phẩm 1 đang có tồn kho là 15. Thử đặt 20)
POST http://localhost:8080/api/orders
Content-Type: application/json

{
"items": [ { "productId": 1, "quantity": 20 } ]
}
// >> Mong đợi lỗi 400 Bad Request (InsufficientStockException).

// 3.4. User: Xem Đơn hàng Của Tôi
GET http://localhost:8080/api/orders/my-orders

// 3.5. User: Xem Chi tiết Đơn hàng (Sử dụng orderId từ 3.2)
GET http://localhost:8080/api/orders/<orderIdFromStep3.2>

// --- Thao tác của ADMIN ---
// Yêu cầu Header: Authorization: Bearer <ADMIN_JWT_TOKEN>

// 3.6. Admin: Xem Tất cả Đơn hàng
GET http://localhost:8080/api/orders

// 3.7. Admin: Xem Chi tiết Đơn hàng Bất kỳ (Sử dụng orderId từ 3.2)
GET http://localhost:8080/api/orders/admin/<orderIdFromStep3.2>

// 3.8. Admin: Cập nhật Trạng thái Đơn hàng (Sử dụng orderId từ 3.2)
PUT http://localhost:8080/api/orders/admin/<orderIdFromStep3.2>/status
Content-Type: application/json

{ "status": "SHIPPED" } 

test google : http://localhost:8080/oauth2/authorization/google
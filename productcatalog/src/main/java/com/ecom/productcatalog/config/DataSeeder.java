package com.ecom.productcatalog.config;

import com.ecom.productcatalog.model.Category;
import com.ecom.productcatalog.model.Product;
import com.ecom.productcatalog.model.Role;
import com.ecom.productcatalog.model.User;
import com.ecom.productcatalog.repository.CategoryRepository;
import com.ecom.productcatalog.repository.ProductRepository;
import com.ecom.productcatalog.repository.RoleRepository;
import com.ecom.productcatalog.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional; // Quan trọng cho seeder

import java.util.Arrays;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

@Component
public class DataSeeder implements CommandLineRunner {
    private static final Logger logger = LoggerFactory.getLogger(DataSeeder.class);

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;

    public DataSeeder(ProductRepository productRepository,
                      CategoryRepository categoryRepository,
                      RoleRepository roleRepository,
                      UserRepository userRepository,
                      PasswordEncoder passwordEncoder) {
        this.productRepository = productRepository;
        this.categoryRepository = categoryRepository;
        this.roleRepository = roleRepository;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional // Chạy seeder trong một transaction
    public void run(String... args) throws Exception {
        logger.info("Starting data seeding process...");

        // Create Roles if not exists
        Role roleUser = createRoleIfNotFound(Role.RoleName.ROLE_USER);
        Role roleAdmin = createRoleIfNotFound(Role.RoleName.ROLE_ADMIN);

        // Create sample users if not exist
        createUserIfNotFound("user", "user@example.com", "user123", Set.of(roleUser));
        createUserIfNotFound("admin", "admin@example.com", "admin123", Set.of(roleAdmin, roleUser));

        // Create Categories if they don't exist
        Category electronics = createCategoryIfNotFound("Electronics");
        Category clothing = createCategoryIfNotFound("Clothing");
        Category home = createCategoryIfNotFound("Home and Kitchen");

        // Create Products if the repository is empty (hoặc logic kiểm tra khác)
        if (productRepository.count() == 0) {
            logger.info("No products found, seeding sample products...");
            Product phone = new Product();
            phone.setName("iPhone 15 Pro");
            phone.setDescription("Latest iPhone model with stunning features and A17 Bionic chip.");
            phone.setImageUrl("https://cdn2.fptshop.com.vn/unsafe/828x0/filters:format(webp):quality(75)/2022_10_28_638025679600546363_iPhone%2014%20(16).jpg");
            phone.setPrice(1099.00);
            phone.setCategory(electronics);
            phone.setStockQuantity(50);

            Product laptop = new Product();
            laptop.setName("MacBook Pro 16\" M3 Max");
            laptop.setDescription("Powerful laptop with M3 Max chip for professionals.");
            laptop.setImageUrl("https://shopdunk.com/images/thumbs/0027584_macbook-pro-16-m1-pro-16-core16gb512gb-chinh-hang-cu-dep.png");
            laptop.setPrice(2999.00);
            laptop.setCategory(electronics);
            laptop.setStockQuantity(30);

            // Thêm các sản phẩm khác tương tự...
            // Product jacket = new Product(); ... jacket.setStockQuantity(100);
            // Product bed = new Product(); ... bed.setStockQuantity(10);
            // Product tv = new Product(); ... tv.setStockQuantity(25);
            // Product headphones = new Product(); ... headphones.setStockQuantity(75);
            // Product sneakers = new Product(); ... sneakers.setStockQuantity(120);
            // Product sofa = new Product(); ... sofa.setStockQuantity(5);
            // Product camera = new Product(); ... camera.setStockQuantity(15);

            productRepository.saveAll(Arrays.asList(phone, laptop /*, jacket, bed, tv, headphones, sneakers, sofa, camera */));
            logger.info("Sample products seeded.");
        } else {
            logger.info("Products already exist, skipping product seeding.");
        }

        logger.info("Data seeding process completed.");
    }

    private Role createRoleIfNotFound(Role.RoleName roleName) {
        Optional<Role> roleOpt = roleRepository.findByName(roleName);
        if (roleOpt.isEmpty()) {
            Role newRole = new Role(null, roleName);
            logger.info("Creating role: {}", roleName);
            return roleRepository.save(newRole);
        }
        return roleOpt.get();
    }

    private void createUserIfNotFound(String username, String email, String password, Set<Role> roles) {
        if (!userRepository.existsByUsername(username)) {
            User user = new User();
            user.setUsername(username);
            user.setEmail(email);
            user.setPassword(passwordEncoder.encode(password));
            user.setRoles(new HashSet<>(roles)); // Tạo một Set mới để đảm bảo tính thay đổi (modifiable)
            logger.info("Creating user: {}", username);
            userRepository.save(user);
        }
    }

    private Category createCategoryIfNotFound(String categoryName) {
        Optional<Category> categoryOpt = categoryRepository.findByName(categoryName); // Giả sử bạn đã thêm findByName vào CategoryRepository
        if (categoryOpt.isEmpty()) {
            Category newCategory = new Category();
            newCategory.setName(categoryName);
            logger.info("Creating category: {}", categoryName);
            return categoryRepository.save(newCategory);
        }
        return categoryOpt.get();
    }
}
package com.ecom.productcatalog.config;

import com.ecom.productcatalog.model.*;
import com.ecom.productcatalog.repository.*;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Set;

@Component
public class DataSeeder implements CommandLineRunner {
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
    public void run(String... args) throws Exception {
        // Clear all existing data
        productRepository.deleteAll();
        categoryRepository.deleteAll();

        // Create Categories
        Category electronics = new Category();
        electronics.setName("Electronics");

        Category clothing = new Category();
        clothing.setName("Clothing");

        Category home = new Category();
        home.setName("Home and Kitchen");

        categoryRepository.save(electronics);
        categoryRepository.save(clothing);
        categoryRepository.save(home);

        // Create Products
        Product phone = new Product();
        phone.setName("iPhone 14");
        phone.setDescription("Latest iPhone model with stunning features.");
        phone.setImageUrl("https://cdn2.fptshop.com.vn/unsafe/828x0/filters:format(webp):quality(75)/2022_10_28_638025679600546363_iPhone%2014%20(16).jpg");
        phone.setPrice(999.00);
        phone.setCategory(electronics);

        Product laptop = new Product();
        laptop.setName("MacBook Pro 16\"");
        laptop.setDescription("Powerful laptop with M1 Pro chip.");
        laptop.setImageUrl("https://shopdunk.com/images/thumbs/0027584_macbook-pro-16-m1-pro-16-core16gb512gb-chinh-hang-cu-dep.png");
        laptop.setPrice(2499.00);
        laptop.setCategory(electronics);

        Product jacket = new Product();
        jacket.setName("North Face Jacket");
        jacket.setDescription("Waterproof and insulated jacket for winter.");
        jacket.setImageUrl("https://cdn-images.farfetch-contents.com/26/05/10/11/26051011_56214957_1000.jpg");
        jacket.setPrice(199.00);
        jacket.setCategory(clothing);

        Product bed = new Product();
        bed.setName("King Size Bed");
        bed.setDescription("Comfortable king size bed with memory foam mattress.");
        bed.setImageUrl("https://www.laura-james.co.uk/cdn/shop/files/cavill-grey-fabric-king-size-bed-and-mattress-laura-james-1.png?v=1713535674");
        bed.setPrice(1500.00);
        bed.setCategory(home);

        Product tv = new Product();
        tv.setName("Samsung 55\" 4K TV");
        tv.setDescription("Smart TV with UHD resolution and built-in streaming apps.");
        tv.setImageUrl("https://cdn.mediamart.vn/images/product/smart-tivi-samsung-4k-55-inch-55au7002-uhd_972fc278.jpg");
        tv.setPrice(899.00);
        tv.setCategory(electronics);

        Product headphones = new Product();
        headphones.setName("Sony WH-1000XM4");
        headphones.setDescription("Noise-canceling wireless headphones.");
        headphones.setImageUrl("https://bcec.vn/upload/original-image/cdn1/images/202203/source_img/tai-nghe-sony-wh-1000xm4-P6707-1647506169415.jpg");
        headphones.setPrice(348.00);
        headphones.setCategory(electronics);

        Product sneakers = new Product();
        sneakers.setName("Nike Air Max 270");
        sneakers.setDescription("Iconic running shoes with cushioned sole.");
        sneakers.setImageUrl("https://ash.vn/cdn/shop/files/3cf5361fd2872a0fa2deec7d7eba8375_1800x.jpg?v=1734423911");
        sneakers.setPrice(150.00);
        sneakers.setCategory(clothing);

        Product sofa = new Product();
        sofa.setName("Modern Sofa");
        sofa.setDescription("Comfortable and stylish 3-seat sofa.");
        sofa.setImageUrl("https://product.hstatic.net/200000619773/product/sofa_cong_hien_dai_-_modern_curved_sofa_1_c05b7f4f996b458ea1e627920ff8e5b4_master.png");
        sofa.setPrice(1200.00);
        sofa.setCategory(home);

        Product camera = new Product();
        camera.setName("Canon EOS R5");
        camera.setDescription("Full-frame mirrorless camera with 8K video.");
        camera.setImageUrl("https://tokyocamera.vn/wp-content/uploads/2023/04/Canon-EOS-R5.jpg");
        camera.setPrice(3899.00);
        camera.setCategory(electronics);

        productRepository.saveAll(Arrays.asList(phone, laptop, jacket, bed, tv, headphones, sneakers, sofa, camera));

        // Create Roles if not exists
        if (roleRepository.findAll().isEmpty()) {
            roleRepository.saveAll(Arrays.asList(
                    new Role(null, Role.RoleName.ROLE_USER),
                    new Role(null, Role.RoleName.ROLE_ADMIN)
            ));
        }

        // Create sample users if not exist
        if (userRepository.findAll().isEmpty()) {
            Role userRole = roleRepository.findByName(Role.RoleName.ROLE_USER).orElseThrow();
            Role adminRole = roleRepository.findByName(Role.RoleName.ROLE_ADMIN).orElseThrow();

            User user = new User();
            user.setUsername("user");
            user.setEmail("user@example.com");
            user.setPassword(passwordEncoder.encode("user123"));
            user.setRoles(Set.of(userRole));

            User admin = new User();
            admin.setUsername("admin");
            admin.setEmail("admin@example.com");
            admin.setPassword(passwordEncoder.encode("admin123"));
            admin.setRoles(Set.of(adminRole));

            userRepository.saveAll(Arrays.asList(user, admin));
        }
    }
}

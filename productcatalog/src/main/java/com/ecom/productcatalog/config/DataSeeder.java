package com.ecom.productcatalog.config;

import com.ecom.productcatalog.model.*;
import com.ecom.productcatalog.model.Product;
import com.ecom.productcatalog.model.Role;
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
        //clear all existing data
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

        //Create Products
        Product phone = new Product();
        phone.setName("iPhone");
        phone.setDescription("iPhone XXX");
        phone.setImageUrl("https://placehold.co/600x400");
        phone.setPrice(50.00);
        phone.setCategory(electronics);

        Product laptop = new Product();
        laptop.setName("Rog zephyrus");
        laptop.setDescription("Latest Model");
        laptop.setImageUrl("https://placehold.co/600x400");
        laptop.setPrice(180.00);
        laptop.setCategory(electronics);

        Product jacket = new Product();
        jacket.setName("Woman jacket");
        jacket.setDescription("super fashionable jacket");
        jacket.setImageUrl("https://placehold.co/600x400");
        jacket.setPrice(30.00);
        jacket.setCategory(clothing);

        Product bed = new Product();
        bed.setName("Bed");
        bed.setDescription("King size bed");
        bed.setImageUrl("https://placehold.co/600x400");
        bed.setPrice(500.00);
        bed.setCategory(electronics);

        productRepository.saveAll(Arrays.asList(phone, laptop, jacket, bed));

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

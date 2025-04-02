package com.ecom.productcatalog.config;

import com.ecom.productcatalog.model.Category;
import com.ecom.productcatalog.model.Product;
import com.ecom.productcatalog.repository.CategoryRepository;
import com.ecom.productcatalog.repository.ProductRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.Arrays;

@Component
public class DataSeeder implements CommandLineRunner {
    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;

    public DataSeeder(ProductRepository productRepository, CategoryRepository categoryRepository) {
        this.productRepository = productRepository;
        this.categoryRepository = categoryRepository;
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

    }
}

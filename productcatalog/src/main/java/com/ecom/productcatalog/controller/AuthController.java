package com.ecom.productcatalog.controller;

import com.ecom.productcatalog.dto.LoginRequest;
import com.ecom.productcatalog.dto.RegisterRequest;
import com.ecom.productcatalog.model.Role;
import com.ecom.productcatalog.model.User;
import com.ecom.productcatalog.repository.RoleRepository;
import com.ecom.productcatalog.repository.UserRepository;
import com.ecom.productcatalog.security.JwtUtils;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
// import org.springframework.http.HttpStatus; // Dành cho việc comment out oauth2Success

import java.util.Collections;
// import java.util.Optional; // Dành cho việc comment out oauth2Success
import java.util.Set;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtUtils jwtUtils;

    public AuthController(UserRepository userRepository, RoleRepository roleRepository,
                          PasswordEncoder passwordEncoder, AuthenticationManager authenticationManager,
                          JwtUtils jwtUtils) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.jwtUtils = jwtUtils;
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequest request) {
        if (userRepository.existsByUsername(request.username())) {
            return ResponseEntity.badRequest().body("Username is taken");
        }

        User user = new User();
        user.setUsername(request.username());
        user.setEmail(request.email());
        user.setPassword(passwordEncoder.encode(request.password()));

        Role userRole = roleRepository.findByName(Role.RoleName.ROLE_USER)
                .orElseThrow(() -> new RuntimeException("Role not found"));
        user.setRoles(Set.of(userRole));

        userRepository.save(user);
        return ResponseEntity.ok("User registered successfully");
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.username(), request.password())
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);
        String jwt = jwtUtils.generateToken((UserDetails) authentication.getPrincipal());

        return ResponseEntity.ok(Collections.singletonMap("token", jwt));
    }

    /* <<<< PHẦN NÀY ĐÃ ĐƯỢC VÔ HIỆU HÓA HOẶC XÓA TRONG QUÁ TRÌNH GỠ LỖI >>>>
    @GetMapping("/oauth2-success")
    public ResponseEntity<?> oauth2Success(Authentication authentication) {
        // SecurityContextHolder.getContext().setAuthentication(authentication);

        // Lấy UserDetails từ Authentication
        // UserDetails userDetails = (UserDetails) authentication.getPrincipal();

        // Kiểm tra người dùng có tồn tại không
        // Optional<User> existingUser = userRepository.findByUsername(userDetails.getUsername());
        // if (existingUser.isEmpty()) {
            // Nếu chưa tồn tại, tạo người dùng mới
        //     User user = new User();
        //     user.setUsername(userDetails.getUsername());
        //     user.setEmail(userDetails.getUsername()); // Sử dụng username làm email
        //     user.setPassword(passwordEncoder.encode("oauth2-user")); // Mật khẩu placeholder

        //     Role userRole = roleRepository.findByName(Role.RoleName.ROLE_USER)
        //             .orElseThrow(() -> new RuntimeException("Role not found"));
        //     user.setRoles(Set.of(userRole));

        //     userRepository.save(user);
        // }

        // Tạo JWT và trả về
        // String jwt = jwtUtils.generateToken(userDetails);
        // return ResponseEntity.ok(Collections.singletonMap("token", jwt));
        // return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("OAuth2 success endpoint under maintenance due to previous error. Should be handled by successHandler.");
    }
    */
    
    @GetMapping("/oauth2-failure")
    public ResponseEntity<?> oauth2Failure() {
        return ResponseEntity.badRequest().body("OAuth2 authentication failed");
    }
}
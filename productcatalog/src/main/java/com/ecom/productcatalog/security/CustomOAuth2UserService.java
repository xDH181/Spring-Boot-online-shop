package com.ecom.productcatalog.security;

import com.ecom.productcatalog.model.Role;
import com.ecom.productcatalog.model.User;
import com.ecom.productcatalog.repository.RoleRepository;
import com.ecom.productcatalog.repository.UserRepository;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2UserAuthority;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired; // Thêm nếu dùng Autowired
import org.springframework.security.crypto.password.PasswordEncoder; // Thêm để mã hóa mật khẩu

import java.util.Collections;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;


@Service
public class CustomOAuth2UserService implements OAuth2UserService<OAuth2UserRequest, OAuth2User> {
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder; // Thêm PasswordEncoder

    // Cập nhật constructor
    @Autowired // Có thể không cần thiết nếu chỉ có 1 constructor và dùng Spring Boot mới nhất
    public CustomOAuth2UserService(UserRepository userRepository,
                                   RoleRepository roleRepository,
                                   PasswordEncoder passwordEncoder) { // Inject PasswordEncoder
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder; // Gán PasswordEncoder
    }

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2UserService<OAuth2UserRequest, OAuth2User> delegate = new DefaultOAuth2UserService();
        OAuth2User oAuth2User = delegate.loadUser(userRequest);

        String email = oAuth2User.getAttribute("email");
        // Một số OAuth provider có thể không trả về 'email', bạn có thể cần dùng 'login' hoặc ID khác
        // String username = oAuth2User.getAttribute("login"); // Ví dụ cho GitHub
        // if (email == null && username != null) email = username; // Fallback

        if (email == null) {
            // Lấy thuộc tính định danh người dùng phù hợp, ví dụ 'id' hoặc 'login'
            // và tạo một email giả hoặc xử lý khác.
            // Đối với Google, 'email' thường có.
            // Nếu không có email, bạn cần một chiến lược khác để tạo username/email duy nhất.
            // Ví dụ: lấy ID từ provider + "@provider.com"
            String userIdFromProvider = oAuth2User.getName(); // .getName() thường trả về ID của user từ provider
            email = userIdFromProvider + "@" + userRequest.getClientRegistration().getRegistrationId() + ".temp";
            // Hoặc ném ra lỗi nếu email là bắt buộc và không có
            // throw new OAuth2AuthenticationException("Email not found from OAuth2 provider");
        }


        Optional<User> userOptional = userRepository.findByUsername(email); // Hoặc findByEmail(email) nếu bạn dùng email làm định danh chính

        User user;
        if (userOptional.isPresent()) {
            user = userOptional.get();
            // TODO: Cập nhật thông tin người dùng nếu cần (ví dụ: tên, ảnh đại diện)
        } else {
            user = new User();
            user.setUsername(email); // Hoặc một username duy nhất khác nếu email không phải là username chính
            user.setEmail(email);
            // Tạo mật khẩu ngẫu nhiên hoặc một placeholder đã mã hóa cho người dùng OAuth2
            // vì họ sẽ không đăng nhập bằng mật khẩu này.
            user.setPassword(passwordEncoder.encode("OAuth2UserP@$$wOrd")); // Mật khẩu dummy được mã hóa

            Role userRole = roleRepository.findByName(Role.RoleName.ROLE_USER)
                    .orElseGet(() -> {
                        // Tạo role nếu chưa tồn tại (logic này có thể nằm trong DataSeeder)
                        Role newRole = new Role(null, Role.RoleName.ROLE_USER);
                        return roleRepository.save(newRole);
                    });
            user.setRoles(new HashSet<>(Collections.singleton(userRole)));

            userRepository.save(user);
        }

        // Trả về một DefaultOAuth2User với các thuộc tính và authorities đã được xử lý.
        // "email" (hoặc thuộc tính bạn dùng làm username) phải là key trong oAuth2User.getAttributes()
        // và là giá trị cho nameAttributeKey
        // Authorities ở đây có thể là các OAuth2 scope hoặc các GrantedAuthority tùy chỉnh
        // Đối với JWT sau này, vai trò thực sự sẽ được lấy từ UserDetailsServiceImpl
        return new DefaultOAuth2User(
                Collections.singleton(new OAuth2UserAuthority(oAuth2User.getAttributes())),
                oAuth2User.getAttributes(),
                "email" // Hoặc thuộc tính bạn dùng làm nameAttributeKey (ví dụ: "login" cho GitHub, "sub" hoặc "email" cho Google)
        );
    }
}
package com.ecom.productcatalog.config;

import com.ecom.productcatalog.security.JwtFilter;
import com.ecom.productcatalog.security.JwtUtils; 
import com.ecom.productcatalog.security.UserDetailsServiceImpl;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.util.UriComponentsBuilder; 

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    private final JwtFilter jwtFilter;
    private final UserDetailsServiceImpl userDetailsService;
    private final JwtUtils jwtUtils; 

    public SecurityConfig(JwtFilter jwtFilter,
                          UserDetailsServiceImpl userDetailsService,
                          JwtUtils jwtUtils ) {
        this.jwtFilter = jwtFilter;
        this.userDetailsService = userDetailsService;
        this.jwtUtils = jwtUtils;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http.csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/auth/**").permitAll()
                        .requestMatchers("/oauth2/**").permitAll() 
                        .requestMatchers(HttpMethod.GET, "/api/products/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/categories/**").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/orders").hasRole("USER")
                        .requestMatchers(HttpMethod.GET, "/api/orders/my-orders").hasRole("USER")
                        .requestMatchers(HttpMethod.GET, "/api/orders/{id}").hasRole("USER")
                        .requestMatchers("/api/cart/**").hasRole("USER")
                        .requestMatchers(HttpMethod.GET, "/api/orders").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.GET, "/api/orders/admin/{id}").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/api/orders/admin/{id}/status").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.POST, "/api/products").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/api/products/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/products/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PATCH, "/api/products/{id}/stock").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.POST, "/api/categories").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/api/categories/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/categories/**").hasRole("ADMIN")
                        .anyRequest().authenticated()
                )
                .oauth2Login(oauth2 -> oauth2
                                .loginPage("/oauth2/authorization/google") 
                                .successHandler(oAuth2AuthenticationSuccessHandler()) 
                                .failureUrl("/api/auth/oauth2-failure")
                )
                .sessionManagement(sess -> sess.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public AuthenticationSuccessHandler oAuth2AuthenticationSuccessHandler() {
        // Nếu bạn có cấu hình logger SLF4J, hãy sử dụng nó:
        // private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(SecurityConfig.class);

        return (request, response, authentication) -> {
            System.out.println("!!!! oAuth2AuthenticationSuccessHandler ENTERED !!!!");
            try {
                OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();
                String email = oAuth2User.getAttribute("email");
                System.out.println("oAuth2AuthenticationSuccessHandler: Email from OAuth2User: " + email);

                if (userDetailsService == null) {
                    System.err.println("oAuth2AuthenticationSuccessHandler: userDetailsService IS NULL!");
                    response.sendRedirect("/api/auth/oauth2-failure?error=handler_config_error");
                    return;
                }
                UserDetails userDetails = userDetailsService.loadUserByUsername(email);
                System.out.println("oAuth2AuthenticationSuccessHandler: UserDetails loaded: " + userDetails.getUsername());

                if (jwtUtils == null) {
                    System.err.println("oAuth2AuthenticationSuccessHandler: jwtUtils IS NULL!");
                    response.sendRedirect("/api/auth/oauth2-failure?error=handler_config_error");
                    return;
                }
                String jwt = jwtUtils.generateToken(userDetails);
                System.out.println("oAuth2AuthenticationSuccessHandler: JWT generated: " + (jwt != null && jwt.length() > 30 ? jwt.substring(0, 30) + "..." : "null or short jwt"));

                String targetUrl = UriComponentsBuilder.fromUriString("http://localhost:3000/oauth2/redirect")
                        .queryParam("token", jwt)
                        .build().toUriString();
                System.out.println("oAuth2AuthenticationSuccessHandler: Attempting to redirect to: " + targetUrl);

                if (response.isCommitted()) {
                    System.out.println("oAuth2AuthenticationSuccessHandler: Response already committed before redirect to " + targetUrl);
                    return; 
                }
                response.sendRedirect(targetUrl);
                System.out.println("oAuth2AuthenticationSuccessHandler: Redirect sent to " + targetUrl);

            } catch (Exception e) {
                System.err.println("!!!! EXCEPTION in oAuth2AuthenticationSuccessHandler !!!!");
                e.printStackTrace(System.err); 

                if (!response.isCommitted()) {
                    try {
                        response.sendRedirect("/api/auth/oauth2-failure?error=handler_exception&message=" + java.net.URLEncoder.encode(e.getMessage(), "UTF-8"));
                    } catch (java.io.IOException ex) {
                        System.err.println("oAuth2AuthenticationSuccessHandler: Could not redirect to failure page after exception: " + ex.getMessage());
                    }
                }
            }
        };
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder());
        return new ProviderManager(provider);
    }
}
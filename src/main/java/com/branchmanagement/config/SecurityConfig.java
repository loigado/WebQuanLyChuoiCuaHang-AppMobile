package com.branchmanagement.config;

import com.branchmanagement.security.CustomUserDetailsService;
import com.branchmanagement.security.JwtAuthenticationFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity 
public class SecurityConfig {

    private final CustomUserDetailsService userDetailsService;
    private final JwtAuthenticationFilter jwtAuthFilter;

    public SecurityConfig(CustomUserDetailsService userDetailsService, JwtAuthenticationFilter jwtAuthFilter) {
        this.userDetailsService = userDetailsService;
        this.jwtAuthFilter = jwtAuthFilter;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }

    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            // 1. Tắt CSRF vì App sử dụng Token (Stateless)
            .csrf(AbstractHttpConfigurer::disable)
            
            // 2. Kích hoạt CORS với cấu hình bên dưới
            .cors(customizer -> customizer.configurationSource(corsConfigurationSource()))
            
            // 3. Không sử dụng Session
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            
            .authorizeHttpRequests(auth -> auth
                // ✅ ƯU TIÊN CAO NHẤT: Cho phép App Mobile truy cập không cần Token để Debug
                .requestMatchers("/api/mobile/**").permitAll() 
                
                // Cho phép công khai các API quan trọng khác
                .requestMatchers("/api/auth/**", "/error", "/ws-notifications/**").permitAll()
                .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll() // ✅ CHO PHÉP CORS PRE-FLIGHT
                
                // ✅ ĐÃ THÊM DÒNG NÀY: Cho phép truy cập công khai để xem ảnh chấm công
                .requestMatchers("/uploads/**").permitAll() 
                
                // Phân quyền cho Dashboard và các chức năng Admin/Manager
                .requestMatchers("/api/admin/dashboard/**")
                    .hasAnyAuthority("ROLE_ADMIN", "ADMIN", "ROLE_MANAGER", "MANAGER", "ROLE_ACCOUNTANT", "ACCOUNTANT")
                
                .requestMatchers(HttpMethod.GET, "/api/admin/branches/**")
                    .hasAnyAuthority("ROLE_ADMIN", "ADMIN", "ROLE_MANAGER", "MANAGER", "ROLE_ACCOUNTANT", "ACCOUNTANT")
                
                .requestMatchers("/api/admin/users/**")
                    .hasAnyAuthority("ROLE_ADMIN", "ADMIN", "ROLE_MANAGER", "MANAGER")
                
                .requestMatchers("/api/admin/stock/approve/**")
                    .hasAnyAuthority("ROLE_ADMIN", "ADMIN", "ROLE_ACCOUNTANT", "ACCOUNTANT")
                
                .requestMatchers("/api/admin/**").hasAnyAuthority("ROLE_ADMIN", "ADMIN")
                
                .requestMatchers("/api/manager/**")
                    .hasAnyAuthority("ROLE_MANAGER", "MANAGER", "ROLE_ADMIN", "ADMIN", "ROLE_ACCOUNTANT", "ACCOUNTANT")
                
                .requestMatchers("/api/accountant/**", "/api/reports/**")
                    .hasAnyRole("ADMIN", "ACCOUNTANT", "MANAGER")
                
                .requestMatchers("/api/employee/**").authenticated()
                
                .anyRequest().authenticated()
            );

        // 4. Thiết lập Provider và Filter
        http.authenticationProvider(authenticationProvider());
        http.addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        
        // 1. SỬA CHỖ NÀY: Dùng Pattern để cho phép mọi IP/Domain
        configuration.setAllowedOriginPatterns(Arrays.asList("*")); 
        
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));
        configuration.setAllowedHeaders(Arrays.asList("*"));
        configuration.setAllowCredentials(true);
        configuration.setMaxAge(3600L);
        
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
package com.branchmanagement.controller;

import org.springframework.security.crypto.password.PasswordEncoder;
import com.branchmanagement.dto.ApiResponse;
import com.branchmanagement.entity.User;
import com.branchmanagement.repository.UserRepository;
import com.branchmanagement.security.CustomUserDetails;
import com.branchmanagement.security.JwtUtils;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtUtils jwtUtils;
    private final PasswordEncoder passwordEncoder; 
    private final UserRepository userRepository; // ✅ THÊM REPOSITORY ĐỂ LẤY TÊN THẬT

    public AuthController(AuthenticationManager authenticationManager, 
                          JwtUtils jwtUtils, 
                          PasswordEncoder passwordEncoder,
                          UserRepository userRepository) {
        this.authenticationManager = authenticationManager;
        this.jwtUtils = jwtUtils;
        this.passwordEncoder = passwordEncoder; 
        this.userRepository = userRepository;
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<JwtResponse>> authenticateUser(@Valid @RequestBody LoginRequest loginRequest) {
        
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginRequest.getUsername(), loginRequest.getPassword()));

        SecurityContextHolder.getContext().setAuthentication(authentication);
        String jwt = jwtUtils.generateJwtToken(authentication);
        
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        
        // ✅ TÌM USER ĐỂ LẤY FULL NAME
        User user = userRepository.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("Lỗi: Không tìm thấy User"));

        // ✅ TRUYỀN THÊM FULL NAME VÀO RESPONSE
        JwtResponse response = new JwtResponse(jwt, userDetails.getUserId(), userDetails.getUsername(), 
                user.getFullName(), // <-- Truyền tên thật ở đây
                userDetails.getAuthorities().iterator().next().getAuthority(), userDetails.getBranchId());

        return ResponseEntity.ok(ApiResponse.ok("Đăng nhập thành công", response));
    }
}

@Data
class LoginRequest {
    @NotBlank(message = "Tài khoản không được để trống")
    private String username;
    
    @NotBlank(message = "Mật khẩu không được để trống")
    private String password;
}

@Data
class JwtResponse {
    private String token;
    private String type = "Bearer";
    private Integer id;
    private String username;
    private String fullName; // ✅ THÊM TRƯỜNG FULL NAME VÀO ĐÂY
    private String role;
    private Integer branchId;

    public JwtResponse(String token, Integer id, String username, String fullName, String role, Integer branchId) {
        this.token = token;
        this.id = id;
        this.username = username;
        this.fullName = fullName; // ✅ GÁN GIÁ TRỊ
        this.role = role;
        this.branchId = branchId;
    }
}
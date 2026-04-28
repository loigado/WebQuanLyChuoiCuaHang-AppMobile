package com.branchmanagement.security;

import com.branchmanagement.entity.User;
import com.branchmanagement.repository.UserRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    public CustomUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    @Transactional // Đảm bảo lấy được thông tin Branch nếu để Lazy Loading
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User Not Found with username: " + username));

        // ✅ GIẢI PHÁP TỐI ƯU NHẤT:
        // Chỉ cần truyền toàn bộ object User vào đây!
        // Việc cộng tiền tố "ROLE_" và kiểm tra tài khoản "ACTIVE" 
        // ĐÃ ĐƯỢC XỬ LÝ tự động bên trong class CustomUserDetails.
        return new CustomUserDetails(user);
    }
}
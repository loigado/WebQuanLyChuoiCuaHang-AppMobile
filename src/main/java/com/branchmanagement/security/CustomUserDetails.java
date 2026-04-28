package com.branchmanagement.security;

import com.branchmanagement.entity.User;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

@Getter
public class CustomUserDetails implements UserDetails {

    private final Integer userId;
    private final String username;

    @JsonIgnore
    private final String password;

    private final Integer branchId;

    private final Collection<? extends GrantedAuthority> authorities;
    
    // ✅ ĐÃ THÊM: Khai báo biến isActive để sửa lỗi "isActive cannot be resolved"
    private final boolean isActive;

    // 1. Constructor khởi tạo từ đối tượng User
    public CustomUserDetails(User user) {
        this.userId = user.getUserId();
        this.branchId = user.getBranch() != null ? user.getBranch().getBranchId() : null;
        this.username = user.getUsername();
        this.password = user.getPassword();
        
        // Kiểm tra an toàn, chấp nhận cả ACTIVE và active
        this.isActive = user.getStatus() != null && "ACTIVE".equalsIgnoreCase(user.getStatus());
        
        this.authorities = Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + user.getRole().name()));
    }

    // 2. ✅ ĐÃ THÊM: Constructor nhiều tham số để sửa lỗi "Constructor is undefined" ở các file khác
    public CustomUserDetails(Integer userId, String username, String password, 
                           Collection<? extends GrantedAuthority> authorities, 
                           Integer branchId, boolean isActive) {
        this.userId = userId;
        this.username = username;
        this.password = password;
        this.authorities = authorities;
        this.branchId = branchId;
        this.isActive = isActive;
    }

    // Giữ lại hàm build cũ để không làm lỗi file JwtUtils (nếu có dùng)
    public static CustomUserDetails build(User user) {
        // Tái sử dụng lại Constructor 1 cho gọn và chính xác
        return new CustomUserDetails(user);
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return username;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return isActive; // ✅ ĐÃ SỬA: Trả về trạng thái thật thay vì luôn true
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return isActive; // ✅ ĐÃ SỬA: Trả về trạng thái thật thay vì luôn true
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CustomUserDetails user = (CustomUserDetails) o;
        return Objects.equals(userId, user.userId);
    }

    // ✅ ĐÃ THÊM: hashCode() để đảm bảo contract với equals()
    @Override
    public int hashCode() {
        return Objects.hash(userId);
    }
}
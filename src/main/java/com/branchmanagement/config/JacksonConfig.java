package com.branchmanagement.config;
import com.fasterxml.jackson.datatype.hibernate6.Hibernate6Module;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class JacksonConfig {

    @Bean
    public Hibernate6Module hibernate6Module() {
        Hibernate6Module module = new Hibernate6Module();
        // FORCE_LAZY_LOADING = false: Jackson sẽ bỏ qua proxy chưa được fetch thay vì query thêm hoặc văng lỗi.
        // Điều này giúp tối ưu performance DB (tránh N+1 query không mong muốn).
        module.configure(Hibernate6Module.Feature.FORCE_LAZY_LOADING, false);
        return module;
    }
}
package com.branchmanagement.demo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
@ComponentScan(basePackages = "com.branchmanagement")
@EnableJpaRepositories(basePackages = "com.branchmanagement.repository")
@EntityScan(basePackages = "com.branchmanagement.entity")
public class DoAnTotNghiepApplication {
    public static void main(String[] args) {
        SpringApplication.run(DoAnTotNghiepApplication.class, args);
    }
}
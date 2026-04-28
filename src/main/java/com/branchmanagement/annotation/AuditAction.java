package com.branchmanagement.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface AuditAction {
    // Mô tả hành động, ví dụ: "DUYET_PHIEU_KHO", "TAO_SAN_PHAM"
    String action(); 
}
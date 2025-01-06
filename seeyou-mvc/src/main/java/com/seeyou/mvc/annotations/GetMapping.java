package com.seeyou.mvc.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author Septian Yudhistira
 * @version 1.0
 * @since 2024-12-08
 */

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD})
@Verb("GET")
public @interface GetMapping {
    String[] value();
}

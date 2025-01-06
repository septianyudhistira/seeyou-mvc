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

@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Verb("POST")
public @interface PostMapping {
    String[] value();
}

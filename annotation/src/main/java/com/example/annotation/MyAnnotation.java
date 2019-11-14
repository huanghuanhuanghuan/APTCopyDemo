package com.example.annotation;

import java.lang.annotation.*;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.CLASS)
//@Documented
//@Inherited
public @interface MyAnnotation {
    int type() default 0;
}

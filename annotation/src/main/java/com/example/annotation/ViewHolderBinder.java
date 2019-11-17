package com.example.annotation;

import java.lang.annotation.*;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.SOURCE)
//@Documented
//@Inherited
public @interface ViewHolderBinder {
    int xml() default 0;
}

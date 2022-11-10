package com.atguigu.gmall.common.cache;

import org.springframework.beans.factory.annotation.Value;

import java.lang.annotation.*;


/**
 * 元注解：修饰注解的注解
 *
 * @Target({ElementType.TYPE, ElementType.METHOD})
 * 目标：当前被修饰的注解可以修饰的位置
 * @Retention(RetentionPolicy.RUNTIME)
 * 声明周期：当前注解存在过程
 *  (SOURCE)java -(CLASS)class--(RUNTIME)runtime
 *  @Documented
 *  javadoc 生成文档
 * @Inherited
 *  子类可继承该注解
 *
 *
 *
 */
@Target({ ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@Documented
public @interface GmallCache {

    //前缀
    String prefix() default "cache:";
    //后缀
    String suffix() default ":info";


}

package com.jiage.spring.core;

public interface BeanPostProcessor {
    Object postProcessBeforeInitialization(String beanName, Object bean);

    Object postProcessAfterInitialization(String beanName, Object bean);
}

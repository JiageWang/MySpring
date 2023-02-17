package com.jiage.demo.config;

import com.jiage.spring.annotation.Component;
import com.jiage.spring.core.BeanPostProcessor;

@Component
public class MyBeanPostProcessor implements BeanPostProcessor {
    @Override
    public Object postProcessBeforeInitialization(String beanName, Object bean) {
        System.out.println(String.format("%s pocess before init", beanName));
        return bean;
    }

    @Override
    public Object postProcessAfterInitialization(String beanName, Object bean) {
        System.out.println(String.format("%s pocess after init", beanName));
        return bean;
    }
}

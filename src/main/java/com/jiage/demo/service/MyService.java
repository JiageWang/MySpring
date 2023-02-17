package com.jiage.demo.service;

import com.jiage.spring.annotation.Autowired;
import com.jiage.spring.annotation.Component;
import com.jiage.spring.annotation.Scope;
import com.jiage.demo.dao.MyDao;
import com.jiage.spring.core.BeanNameAware;
import com.jiage.spring.core.InitializingBean;

@Scope("singleton")
@Component
public class MyService implements BeanNameAware, InitializingBean {

    @Autowired
    private MyDao myDao;

    private String initValue;

    private String beanName;

    public void run() {
        System.out.println("service run ...");
        myDao.run();
    }

    @Override
    public void setBeanName(String benaName) {
        this.beanName = benaName;
    }

    @Override
    public void afterPropertiesSet() {
        initValue = "init";
        System.out.println("myservice init");
    }
}

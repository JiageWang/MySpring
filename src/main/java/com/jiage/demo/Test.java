package com.jiage.demo;

import com.jiage.demo.config.MyConfig;
import com.jiage.spring.core.ApplicationContext;
import com.jiage.demo.service.MyService;

public class Test {
    public static void main(String[] args) throws Exception {
        ApplicationContext applicationContext = new ApplicationContext(MyConfig.class);

        // 测试单例
        System.out.println(applicationContext.getBean("myService"));
        System.out.println(applicationContext.getBean("myService"));

        // 测试注入
        MyService myService = (MyService) applicationContext.getBean("myService");
        myService.run();
    }
}

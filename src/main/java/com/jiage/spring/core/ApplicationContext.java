package com.jiage.spring.core;

import com.jiage.spring.annotation.Autowired;
import com.jiage.spring.annotation.Component;
import com.jiage.spring.annotation.ComponentScan;
import com.jiage.spring.annotation.Scope;

import java.beans.Introspector;
import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;

public class ApplicationContext {

    private String classPath;
    private Class config;
    private ClassLoader classLoader;
    private ConcurrentHashMap<String, BeanDefinition> beanDefinitionMap = new ConcurrentHashMap<>();
    private ConcurrentHashMap<String, Object> singletonBeanMap = new ConcurrentHashMap<>();
    private ArrayList<BeanPostProcessor> beanPostProcessorList = new ArrayList<>();

    public ApplicationContext(Class configClass) throws Exception {

        this.config = configClass;
        this.classLoader = ApplicationContext.class.getClassLoader();
        this.classPath = new File(classLoader.getResource("").getFile()).getAbsolutePath();

        // 获取包扫描注解
        if (configClass.isAnnotationPresent(ComponentScan.class)) {
            ComponentScan componentScanAnno = (ComponentScan) configClass.getAnnotation(ComponentScan.class);

            // 获取包路径
            String scanPackage = componentScanAnno.value();
            scanPackage = scanPackage.replace('.', '/');
            File file = new File(classLoader.getResource(scanPackage).getFile());

            // 扫描Component注解，加入BeanDefinitionMap
            loadBeanDefinition(file);

            // 遍历BeanDefinition，单例则加入新建实例加入容器
            for (String beanName : beanDefinitionMap.keySet()) {
                BeanDefinition beanDefinition = beanDefinitionMap.get(beanName);
                if (beanDefinition.getScope().endsWith("singleton")) {
                    createBean(beanName, beanDefinition);
                }
            }

        }
    }

    private void loadBeanDefinition(File file) throws ClassNotFoundException, NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException {
        // 是文件夹递归遍历
        if (file.isDirectory()) {
            File[] childFiles = file.listFiles();
            for (File childFile : childFiles) {
                loadBeanDefinition(childFile);
            }
        }
        // 是.class文件进行反射加载
        else if (file.getName().endsWith(".class")) {

            // 加载类
            String className = file.getAbsolutePath().substring(classPath.length() + 1);
            className = className.replace('\\', '.').replace(".class", "");
            Class clazz = classLoader.loadClass(className);

            // 存在Component注解则注入BeanDefinition
            if (clazz.isAnnotationPresent(Component.class)) {

                // 加载BeanPostProcessor
                if (BeanPostProcessor.class.isAssignableFrom(clazz)) { // 判断类是否继承自接口
                    BeanPostProcessor beanPostProcessor = (BeanPostProcessor) clazz.getConstructor().newInstance();
                    this.beanPostProcessorList.add(beanPostProcessor);
                }

                // 通过Conponent注解获取beanName
                Component component = (Component) clazz.getAnnotation(Component.class);
                String beanName = component.value();
                if (beanName.equals("")) {
                    beanName = Introspector.decapitalize(clazz.getSimpleName());
                }

                BeanDefinition beanDefinition = new BeanDefinition();
                beanDefinition.setType(clazz);

                // 存在scope注解获取值，不存在默认singleton
                if (clazz.isAnnotationPresent(Scope.class)) {
                    Scope scope = (Scope) clazz.getAnnotation(Scope.class);
                    beanDefinition.setScope(scope.value());
                } else {
                    beanDefinition.setScope("singleton");
                }

                this.beanDefinitionMap.put(beanName, beanDefinition);
            }
        }

    }

    public Object getBean(String beanName) throws Exception {

        BeanDefinition beanDefinition = beanDefinitionMap.get(beanName);
        if (beanDefinition == null) {
            throw new Exception("beanDefinition不存在");
        }

        Object object = null;
        // 单例模式先从容器中拿
        if (beanDefinition.getScope().equals("singleton")) {
            object = singletonBeanMap.get(beanName);
            if (null == object) {
                object = createBean(beanName, beanDefinition);
            }
            singletonBeanMap.put(beanName, object);
        }
        // 多例模式直接创建
        else {
            object = createBean(beanName, beanDefinition);
        }
        return object;
    }

    private Object createBean(String beanName, BeanDefinition beanDefinition) throws Exception {

        Class clazz = beanDefinition.getType();
        Object bean = clazz.getConstructor().newInstance();

        // 依赖注入
        Field[] fields = clazz.getDeclaredFields();
        for (Field field : fields) {
            if (field.isAnnotationPresent(Autowired.class)) {
                field.setAccessible(true);                      // 获取权限
                field.set(bean, getBean(field.getName()));    // 可能循环依赖
            }
        }

        // aware回调
        if (bean instanceof BeanNameAware) {
            ((BeanNameAware) bean).setBeanName(beanName);
        }

        this.beanPostProcessorList.forEach(
                beanPostProcessor -> {
                    beanPostProcessor.postProcessBeforeInitialization(beanName, bean);
                }
        );

        // 初始化
        if (bean instanceof InitializingBean) {
            ((InitializingBean) bean).afterPropertiesSet();
        }

        this.beanPostProcessorList.forEach(
                beanPostProcessor -> {
                    beanPostProcessor.postProcessAfterInitialization(beanName, bean);
                }
        );

        return bean;
    }

}

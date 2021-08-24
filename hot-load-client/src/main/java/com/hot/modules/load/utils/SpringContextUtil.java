package com.hot.modules.load.utils;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * spring上下文工具类
 *
 * @author liujun
 * @date 2021/8/17
 */
@Component
public class SpringContextUtil implements ApplicationContextAware{
    /**
     * 上下文对象实例
     */
    private static ApplicationContext applicationContext;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        SpringContextUtil.applicationContext = applicationContext;
    }

    /**
     * 获取applicationContext
     * @return 上下文对象实例
     */
    public static ApplicationContext getApplicationContext() {
        return applicationContext;
    }
    /**
     * 获取bean工厂，用来实现动态注入bean
     * 不能使用其他类加载器加载bean
     * 否则会出现异常:类未找到，类未定义
     * @return bean工厂
     */
    public static DefaultListableBeanFactory getBeanFactory(){
        return (DefaultListableBeanFactory) getApplicationContext().getAutowireCapableBeanFactory();
    }

    /**
     * 获取所有的bean
     * @return 查询结果
     */
    public static List<Map<String, Object>> getAllBean() {

        List<Map<String, Object>> list = new ArrayList<>();
        String[] beans = getApplicationContext().getBeanDefinitionNames();
        for (String beanName : beans) {
            Class<?> beanType = getApplicationContext().getType(beanName);
            Map<String, Object> map = new HashMap<>();
            map.put("beanName", beanName);
            map.put("beanType", beanType);
            map.put("package", Objects.isNull(beanType)?null:beanType.getPackage());
            list.add(map);
        }
        return list;
    }

    /**
     * 判断是否存在bean
     * @param beanName bean的名称
     * @return true-存在,false-不存在
     */
    public static boolean containsBean(String beanName){
        return getApplicationContext().containsBean(beanName);
    }


    /**
     * 通过beanName 获取bean
     * @param beanName bean的名称
     * @return bean
     */
    public static Object getBean(String beanName) {
        return getApplicationContext().getBean(beanName);
    }

    /**
     * 通过类实体获取bean
     * @param clazz 类实体
     * @param <T> 类的类型
     * @return bean
     */
    public static <T> T getBean(Class<T> clazz) {
        return getApplicationContext().getBean(clazz);
    }

    /**
     * 通过beanName和类实体获取bean
     * @param beanName bean的名称
     * @param clazz 类实体
     * @param <T> 类的类型
     * @return bean
     */
    public static <T> T getBean(String beanName, Class<T> clazz) {
        return getApplicationContext().getBean(beanName, clazz);
    }

}

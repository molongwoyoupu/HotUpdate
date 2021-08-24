package com.hot.modules.load.utils;

import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 自定义类加载器的存储库
 *
 * @author liujun
 * @date 2021/8/17
 */
@Slf4j
public class ClassLoaderRepository {
    /**
     * 存储自定义类加载器的map集合
     */
    private final Map<String, CustomClassLoader> repositoryMap = new ConcurrentHashMap<>();

    private ClassLoaderRepository(){}

    /**
     * 存储库添加自定义类加载器
     * @param moduleName        模块名
     * @param customClassLoader 自定义类加载器
     */
    public void addClassLoader(String moduleName, CustomClassLoader customClassLoader){
        repositoryMap.put(moduleName,customClassLoader);
    }

    /**
     * 判断存储库是否存在该模块的类加载器
     * @param moduleName    模块名
     * @return true-存在,false-不存在
     */
    public boolean containsClassLoader(String moduleName){
        return repositoryMap.containsKey(moduleName);
    }

    /**
     * 通过模块名获取 存储库的自定义类加载器
     * @param moduleName    模块名
     * @return 自定义类加载器
     */
    public CustomClassLoader getClassLoaderByModuleName(String moduleName){
        return repositoryMap.get(moduleName);
    }

    /**
     * 通过模块名移除 存储库的自定义类加载器
     * @param moduleName    模块名
     */
    public void removeClassLoader(String moduleName){
        CustomClassLoader customClassLoader = repositoryMap.get(moduleName);
        try {
            Map<String,Class<?>> registeredBeanMap = customClassLoader.getRegisteredBeanMap();
            for (String beanName : registeredBeanMap.keySet()) {
                Class<?> cla=registeredBeanMap.get(beanName);
                if(CustomClassLoader.isSpringBeanClass(cla)){
                    customClassLoader.unregisterBean(beanName);
                }else if(CustomClassLoader.isSpringControllerClass(cla)){
                    log.debug("删除controller:"+beanName);
                    customClassLoader.unregisterController(beanName);
                }
            }

            customClassLoader.getJarFile().close();
            customClassLoader.close();
            repositoryMap.remove(moduleName);
            
        } catch (IOException e) {
            log.error("删除"+moduleName+"模块发生错误");
        }
    }





    private static class ClassLoaderRepositoryHolder{
        private static final ClassLoaderRepository INSTANCE = new ClassLoaderRepository();
    }

    public static ClassLoaderRepository getInstance(){
        return ClassLoaderRepositoryHolder.INSTANCE;
    }

}

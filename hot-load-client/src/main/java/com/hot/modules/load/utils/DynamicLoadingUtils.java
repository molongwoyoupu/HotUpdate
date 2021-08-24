package com.hot.modules.load.utils;

import cn.hutool.core.io.FileUtil;

import java.io.File;
import java.net.URI;
import java.net.URL;

/**
 * 动态加载工具类
 *
 * @author liujun
 * @date 2021/8/17
 */
public class DynamicLoadingUtils {

    public DynamicLoadingUtils() {

    }

    /**
     * 动态添加jar到classpath
     *
     * @param name      jar的名称
     * @param jarPath   jar的路径
     */
    public static void loadJar(String name, String jarPath) {
        try {
            // 获取jar路径
            File jarFile = FileUtil.file(jarPath);
            URI uri = jarFile.toURI();

            String moduleName = jarPath.substring(jarPath.lastIndexOf("/")+1,jarPath.lastIndexOf("."));
            //判断唯一
            if(ClassLoaderRepository.getInstance().containsClassLoader(moduleName)){
                ClassLoaderRepository.getInstance().removeClassLoader(moduleName);
            }

            CustomClassLoader  classLoader = new CustomClassLoader(new URL[]{uri.toURL()}, CustomClassLoader.class.getClassLoader());
            SpringContextUtil.getBeanFactory().setBeanClassLoader(classLoader);
            Thread.currentThread().setContextClassLoader(classLoader);
            classLoader.initBean();
            ClassLoaderRepository.getInstance().addClassLoader(moduleName,classLoader);

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

}

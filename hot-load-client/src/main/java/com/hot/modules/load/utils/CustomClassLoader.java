package com.hot.modules.load.utils;


import com.hot.modules.load.constants.LoaderConstants;
import com.hot.modules.load.utils.SpringContextUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Controller;
import org.springframework.stereotype.Repository;
import org.springframework.stereotype.Service;
import org.springframework.util.ClassUtils;
import org.springframework.util.ReflectionUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 * 动态加载外部jar包的自定义类加载器
 *
 * @author liujun
 * @date 2021/8/17
 */
@Slf4j
public class CustomClassLoader extends URLClassLoader {

    /**
     * 属于本类加载器加载的jar包
     */
    private JarFile jarFile;

    /**
     * 保存已经加载过的Class对象
     */
    private final Map<String, Class<?>> cacheClassMap = new HashMap<>();

    /**
     * 保存本类加载器加载的class字节码
     */
    private final Map<String,byte[]> classBytesMap = new HashMap<>();

    /**
     * 需要注册的spring bean的name集合
     */
    private final Map<String, Class<?>> registeredBeanMap = new HashMap<>();

    /**
     * 构造方法
     * @param urls      jar包的url
     * @param parent    父亲类加载器
     */
    public CustomClassLoader(URL[] urls, ClassLoader parent) {
        super(urls, parent);
        URL url = urls[0];
        String path = url.getPath();
        try {
            jarFile = new JarFile(path);
        } catch (IOException e) {
            e.printStackTrace();
        }
        //初始化类加载器,执行类加载
        init();
    }

    /**
     * 重写loadClass方法,改写loadClass方式
     * @param name 全类名
     * @return 类实体
     * @throws ClassNotFoundException 异常
     */
    @Override
    public Class<?> loadClass(String name) throws ClassNotFoundException {
        if(classBytesMap.containsKey(name)){
            return super.findClass(name);
        }else {
            return super.loadClass(name);
        }
    }


    /**
     * 初始化类加载器，保存字节码
     */
    private void init() {
        //解析jar包每一项
        Enumeration<JarEntry> en = jarFile.entries();
        InputStream input = null;
        try{
            while (en.hasMoreElements()) {
                JarEntry je = en.nextElement();
                String name = je.getName();
                //这里添加了路径扫描限制
                if (name.endsWith(LoaderConstants.CLASS_SUFFIX)) {
                    String className = name.replace(LoaderConstants.CLASS_SUFFIX, LoaderConstants.EMPTY_STR)
                            .replaceAll(LoaderConstants.SLASH, LoaderConstants.POINT);
                    input = jarFile.getInputStream(je);
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    int bufferSize = 4096;
                    byte[] buffer = new byte[bufferSize];
                    int bytesNumRead;
                    while ((bytesNumRead = input.read(buffer)) != -1) {
                        baos.write(buffer, 0, bytesNumRead);
                    }
                    byte[] classBytes = baos.toByteArray();
                    classBytesMap.put(className,classBytes);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if(input!=null){
                try {
                    input.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        //将jar中的每一个class字节码进行Class载入
        for (Map.Entry<String, byte[]> entry : classBytesMap.entrySet()) {
            String key = entry.getKey();
            Class<?> aClass = null;
            try {
                aClass = loadClass(key);
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
            cacheClassMap.put(key,aClass);
        }

    }

    /**
     * 初始化spring bean
     */
    public void initBean(){
        //初始化bean
        for (Map.Entry<String, Class<?>> entry : cacheClassMap.entrySet()) {
            String className = entry.getKey();
            Class<?> cla = entry.getValue();
            if(isSpringBeanClass(cla)){
                registerBean(className,cla);
            }else if(isSpringControllerClass(cla)){
                registerController(className,cla);
            }
        }
    }

    /**
     * 注册bean
     * @param className 全类名
     * @param cla       类实体
     * @return bean名称
     */
    public String registerBean(String className, Class<?> cla){

        BeanDefinitionBuilder beanDefinitionBuilder = BeanDefinitionBuilder.genericBeanDefinition(cla);
        BeanDefinition beanDefinition = beanDefinitionBuilder.getRawBeanDefinition();
        //设置当前bean定义对象是单例的
        beanDefinition.setScope("singleton");

        //获取beanName
        String beanName = getBeanNameByClassName(className);

        SpringContextUtil.getBeanFactory().registerBeanDefinition(beanName,beanDefinition);
        registeredBeanMap.put(beanName,cla);
        log.debug("注册bean:"+beanName);

        //注册后立马加载bean,解决重新加载不成功的问题(非常重要)
        SpringContextUtil.getBean(beanName);
        return beanName;
    }

    /**
     * 注册controller
     * @param className 全类名
     * @param cla       类实体
     */
    public void registerController(String className, Class<?> cla){
        //先注册bean
        String beanName=registerBean(className,cla);

        //再注册controller
        final RequestMappingHandlerMapping requestMappingHandlerMapping =SpringContextUtil.getBean(RequestMappingHandlerMapping.class);
        try{
            //注册Controller
            Method method = requestMappingHandlerMapping.getClass().getSuperclass().getSuperclass().
                    getDeclaredMethod("detectHandlerMethods", Object.class);
            //将private改为可使用
            method.setAccessible(true);
            method.invoke(requestMappingHandlerMapping, beanName);

            log.debug("注册controller:"+beanName);
        }catch (Exception e){
            e.printStackTrace();
        }

    }

    /**
     * 卸载bean
     * @param beanName bean的名称
     */
    public void unregisterBean(String beanName) {
        SpringContextUtil.getBeanFactory().removeBeanDefinition(beanName);
        log.debug("删除bean:"+beanName);
    }


    /**
     * 卸载controller
     * @param className 全类名
     */
    public void unregisterController(String className){
        final RequestMappingHandlerMapping requestMappingHandlerMapping = SpringContextUtil.getBean(RequestMappingHandlerMapping.class);
        String handler = getBeanNameByClassName(className);
        Object controller = SpringContextUtil.getBean(handler);
        final Class<?> targetClass = controller.getClass();
        ReflectionUtils.doWithMethods(targetClass, method -> {
            Method specificMethod = ClassUtils.getMostSpecificMethod(method, targetClass);
            try {
                Method createMappingMethod = RequestMappingHandlerMapping.class.
                        getDeclaredMethod("getMappingForMethod", Method.class, Class.class);
                createMappingMethod.setAccessible(true);
                RequestMappingInfo requestMappingInfo = (RequestMappingInfo)
                        createMappingMethod.invoke(requestMappingHandlerMapping, specificMethod, targetClass);
                if (requestMappingInfo != null) {
                    requestMappingHandlerMapping.unregisterMapping(requestMappingInfo);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }, ReflectionUtils.USER_DECLARED_METHODS);

        //卸载bean
        unregisterBean(handler);
    }


    /**
     * 获取当前类加载器注册的bean
     * 在移除当前类加载器的时候需要手动删除这些注册的bean
     * @return 获取结果
     */
    public Map<String, Class<?>> getRegisteredBeanMap() {
        return this.registeredBeanMap;
    }

    /**
     * 获取当前类加载器加载的jar文件
     * 在移除当前类加载器的时候需要手动关闭
     */
    public JarFile getJarFile(){
        return this.jarFile;
    }

    /**
     * 方法描述 判断class对象是否带有spring的注解
     * @param cla jar中的每一个class
     * @return true 是spring bean   false 不是spring bean
     */
    public static boolean isSpringBeanClass(Class<?> cla){
        if(cla==null){
            return false;
        }else if(cla.isInterface()){
            //是接口
            return false;
        }else if( Modifier.isAbstract(cla.getModifiers())){
            //是抽象类
            return false;
        }else if(Objects.nonNull(cla.getAnnotation(Component.class))){
            return true;
        }else if(Objects.nonNull(cla.getAnnotation(Repository.class))){
            return true;
        }else {
            return Objects.nonNull(cla.getAnnotation(Service.class));
        }
    }

    /**
     * 方法描述 判断class对象是否带有spring的注解 controller
     * @param cla jar中的每一个class
     * @return true-spring的bean,false-不是spring的bean
     */
    public static boolean isSpringControllerClass(Class<?> cla){
        if(cla==null){
            return false;
        }else if(cla.isInterface()){
            //是接口
            return false;
        }else if( Modifier.isAbstract(cla.getModifiers())){
            //是抽象类
            return false;
        }else if(Objects.nonNull(cla.getAnnotation(RestController.class))){
            return true;
        }else {
            return Objects.nonNull(cla.getAnnotation(Controller.class));
        }
    }

    /**
     * 根据类名获取beanName
     * @param   className   全类名
     * @return  beanName
     */
    private String getBeanNameByClassName(String className){
        //将变量首字母置小写
        String beanName = StringUtils.uncapitalize(className);

        beanName =  beanName.substring(beanName.lastIndexOf(".")+1);
        return StringUtils.uncapitalize(beanName);
    }

}

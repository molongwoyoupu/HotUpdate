package com.hot.model;

import com.hot.modules.load.utils.ClassLoaderRepository;
import com.hot.modules.load.utils.SpringContextUtil;
import com.hot.modules.load.utils.CustomClassLoader;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.List;

/**
 * 测试
 * @author liujun
 * @date 2021/8/18
 */
@RestController
@RequestMapping(value = "/loader")
@Slf4j
public class ClassLoaderController {

    @GetMapping(value = "/loadJar")
    public List<?> loadJar(String jarPath){
        jarPath="F:\\test-server\\samc-bpm-client-service-1.0-SNAPSHOT.jar";
        File jar = new File(jarPath);
        URI uri = jar.toURI();
        try {
            String moduleName = jarPath.substring(jarPath.lastIndexOf("/")+1,jarPath.lastIndexOf("."));
            //判断唯一
            if(ClassLoaderRepository.getInstance().containsClassLoader(moduleName)){
                ClassLoaderRepository.getInstance().removeClassLoader(moduleName);
            }

            CustomClassLoader classLoader = new CustomClassLoader(new URL[]{uri.toURL()}, CustomClassLoader.class.getClassLoader());
            Thread.currentThread().setContextClassLoader(classLoader);
            SpringContextUtil.getBeanFactory().setBeanClassLoader(classLoader);
            classLoader.initBean();
            ClassLoaderRepository.getInstance().addClassLoader(moduleName,classLoader);

        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return SpringContextUtil.getAllBean();
    }

    @GetMapping(value = "/deleteJar")
    public List<?> deleteJar(String jarPath){
        jarPath="F:\\test-server\\samc-bpm-client-service-1.0-SNAPSHOT.jar";
        String moduleName = jarPath.substring(jarPath.lastIndexOf("/")+1,jarPath.lastIndexOf("."));
        //判断唯一
        if(ClassLoaderRepository.getInstance().containsClassLoader(moduleName)){
            ClassLoaderRepository.getInstance().removeClassLoader(moduleName);
        }
        return SpringContextUtil.getAllBean();
    }

    @GetMapping(value = "/all-bean")
    public List<?> deleteJar(){

        return SpringContextUtil.getAllBean();
    }


    @Scheduled(cron = "*/5 * * * * ?")
    public void test() {
        String str="测试定时任务：";
        log.debug(str);
    }

}

package com.hot;

import com.hot.modules.load.annotation.EnableHotLoad;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;


/**
 * @author liujun
 * @date 2021/8/17
 */
@SpringBootApplication
@EnableHotLoad
@EnableScheduling
public class SamcBpmClientApplication {

    public static void main(String[] args) {
        SpringApplication.run(SamcBpmClientApplication.class, args);
    }
}

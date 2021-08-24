package com.hot.modules.test.controller;

import com.hot.modules.test.service.TestService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * 热加载测试的jar里的类
 * @author liujun
 * @date 2021/8/17
 */
@RestController
@Slf4j
@RequestMapping("/test")
public class TestController {

    @Resource
    private TestService testService;

    @Scheduled(cron = "*/5 * * * * ?")
    public void test() {
        String str="测试加载初始化版本222：";
        log.debug(str);
    }


    @GetMapping("/test")
    public void hhh() {
        testService.test();
    }

}

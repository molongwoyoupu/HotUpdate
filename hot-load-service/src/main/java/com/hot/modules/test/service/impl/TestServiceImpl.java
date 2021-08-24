package com.hot.modules.test.service.impl;

import com.hot.modules.test.service.TestService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * @author liujun
 * @date 2021/8/23
 */
@Service
@Slf4j
public class TestServiceImpl implements TestService {


    @Override
    public void test() {
        String str="service：";
        log.debug(str);
    }
}

package com.hot.modules.load.annotation;

import com.hot.modules.load.event.HotLoadApplicationStartedEvent;
import org.springframework.context.annotation.Import;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 热加载监听注解
 * 使用方式：加在启动类Application上
 *
 * @author liujun
 * @date 2021/8/18
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Import({HotLoadApplicationStartedEvent.class})
public @interface EnableHotLoad {
}

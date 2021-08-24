package com.hot.modules.load.event;

import com.hot.listener.HotUpdateListener;
import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.context.ApplicationListener;

/**
 * 热加载开始事件
 *
 * @author liujun
 * @date 2021/8/18
 */
public class HotLoadApplicationStartedEvent implements ApplicationListener<ApplicationStartedEvent> {

    @Override
    public void onApplicationEvent(ApplicationStartedEvent applicationStartedEvent) {
        HotUpdateListener hotClassLoader = new HotUpdateListener ();
        hotClassLoader.startListening();
    }

}

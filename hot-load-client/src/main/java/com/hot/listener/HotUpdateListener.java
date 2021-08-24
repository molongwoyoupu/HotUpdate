package com.hot.listener;

import com.hot.modules.load.utils.DynamicLoadingUtils;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.util.Objects;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * 监听热更新
 *
 * @author liujun
 * @date 2021/8/18
 */
@Slf4j
public class HotUpdateListener {

    /**
     * 加载间隔时间
     */
    private static final long LOADER_INTERVAL = 3;
    /**
     * jar包名
     */
    private static final String HOT_UPDATE_JAR_NAME = "hot-load-service-1.0-SNAPSHOT.jar";
    /**
     * jar所在路径
     */
    private static final String HOT_UPDATE_JAR_DIR_PATH = "F:\\test-server";
    /**
     * 指向动态加载module的jar文件
     */
    private static final String HOT_UPDATE_JAR_PATH = HOT_UPDATE_JAR_DIR_PATH+"\\"+HOT_UPDATE_JAR_NAME;
    /**
     * jar文件最后更新时间
     */
    private static long lastModifiedTime = 0;

    /**
     * 开始监听jar文件是否有更新
     */
    public void startListening() {
        // 休眠一会,等加载完
        sleep(3000);
        ScheduledExecutorService scheduledExecutorService = Executors.newScheduledThreadPool(1);
        scheduledExecutorService.scheduleAtFixedRate(() -> {
            if (isHotUpdate()) {
                reload();
            }
        }, 0, LOADER_INTERVAL, TimeUnit.SECONDS);
    }

    /**
     * 休眠一会
     * @param timeMillis 休眠时间
     */
    private static void sleep(long timeMillis) {
        try {
            Thread.sleep(timeMillis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
    /**
     * 判断是否有更新
     *
     * @return 更新标识
     */
    private boolean isHotUpdate() {
        File hotLoaderFile = new File(HOT_UPDATE_JAR_PATH);
        boolean isHotUpdate = false;
        if (hotLoaderFile.exists()) {
            long newModifiedTime = hotLoaderFile.lastModified();
            isHotUpdate = !Objects.equals(lastModifiedTime,newModifiedTime);
            lastModifiedTime = newModifiedTime;
        } else {
            log.debug(hotLoaderFile.getAbsolutePath() + " is not found.");
        }

        log.debug("isHotUpdate：" + isHotUpdate);
        return isHotUpdate;
    }

    /**
     * 重新加载jar文件
     */
    private void reload() {
        File jarPath = new File(HOT_UPDATE_JAR_PATH);
        log.debug("jar lastModified xxxxxxxxxxxxxxxxxx: " + jarPath.lastModified());

        if (jarPath.exists()) {
            try {
                DynamicLoadingUtils.loadJar(HOT_UPDATE_JAR_NAME,HOT_UPDATE_JAR_PATH);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            log.debug("Hot update jar is not found.");
        }
    }


}

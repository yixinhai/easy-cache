package com.xh.easy.easycache.utils;

/**
 * @author yixinhai
 */
public class ThreadUtil {

    public static void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}

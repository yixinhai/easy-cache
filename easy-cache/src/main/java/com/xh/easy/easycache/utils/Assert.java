package com.xh.easy.easycache.utils;

/**
 * 断言
 *
 * @author yixinhai
 */
public class Assert {

    /**
     * 断言字符串不为null、空串、空格
     *
     * @param str 待校验的字符串
     * @param message 错误信息
     */
    public static void notBlank(String str, String message) {
        if (str == null || str.trim().length() == 0) {
            throw new IllegalArgumentException(message);
        }
    }
}

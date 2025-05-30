package com.xh.easy.easycache.utils;

import lombok.extern.slf4j.Slf4j;

import java.util.Random;

/**
 * 随机数工具
 *
 * @author yixinhai
 */
@Slf4j
public class RandomUtils {

    /**
     * 生成随机数
     *
     * @param seed    随机数种子
     * @param nextInt 随机数边界
     * @return 随机数
     */
    public static int generateRandom(long seed, int nextInt) {
        Random random = new Random(seed);
        int randomInt = random.nextInt(nextInt);
        log.info("seed={} nextInt={} randomInt={}", seed, nextInt, randomInt);
        return randomInt;
    }
}
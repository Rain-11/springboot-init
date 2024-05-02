package com.crazy.rain.utils;

import java.util.Random;

/**
 * @ClassName: RandomUtils
 * @Description: 获取随机数
 * @author: CrazyRain
 * @date: 2024/4/19 下午7:02
 */

public class RandomUtils {
    private static final Random random = new Random();

    /**
     * 生成五位随机数
     * @return 无畏随机数
     */
    public static int randomInt() {
        return random.nextInt(90000) + 10000;
    }
}

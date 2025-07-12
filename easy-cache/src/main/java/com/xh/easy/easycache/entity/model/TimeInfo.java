package com.xh.easy.easycache.entity.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.concurrent.TimeUnit;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TimeInfo {

    /**
     * 缓存过期时间
     */
    private long expireTime;

    /**
     * 时间单位
     */
    private TimeUnit timeUnit;

    public long toSeconds() {
        return timeUnit.toSeconds(expireTime);
    }
}

package com.xh.easy.easycache;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@Slf4j
@SpringBootTest
class EasyCacheApplicationTests {

    @Autowired
    private CacheService cacheService;

    @Test
    void contextLoads() {

        String info = cacheService.getInfo(1L);
        log.info("contextLoads.info: {}", info);
    }

}

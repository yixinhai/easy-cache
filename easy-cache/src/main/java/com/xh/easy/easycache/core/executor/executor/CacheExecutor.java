package com.xh.easy.easycache.core.executor.executor;

import com.xh.easy.easycache.core.executor.CacheHealthyChecker;
import com.xh.easy.easycache.core.executor.CacheReader;
import com.xh.easy.easycache.core.executor.CacheWriter;

/**
 * 缓存执行器
 *
 * @author yixinhai
 */
public interface CacheExecutor extends CacheReader, CacheWriter, CacheHealthyChecker {
}

package com.xh.easy.easycache.entity.constant;

/**
 * lua脚本结果
 *
 * @author yixinhai
 */
public class LuaExecResult {

    /**
     * 成功
     */
    public static final String SUCCESS = "success";

    /**
     * 需要查询
     */
    public static final String NEED_QUERY = "need_query";

    /**
     * 成功需要查询
     */
    public static final String SUCCESS_NEED_QUERY = "success_need_query";

    /**
     * 需要等待
     */
    public static final String NEED_WAIT = "need_wait";

    /**
     * 锁持有者不匹配
     */
    public static final String OWNER_MISMATCH = "owner_mismatch";

    /**
     * 空值成功
     */
    public static final String EMPTY_VALUE_SUCCESS = "empty_value_success";
}

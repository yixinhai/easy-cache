package com.xh.easy.easycache.core.lua;

import com.xh.easy.easycache.core.lua.event.NoScriptEvent;

/**
 * 加载lua脚本事件观察者
 *
 * @author yixinhai
 */
public interface LoadScriptVisitor {

    void visit(NoScriptEvent event);
}

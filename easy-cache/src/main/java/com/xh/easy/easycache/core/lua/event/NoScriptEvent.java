package com.xh.easy.easycache.core.lua.event;

import com.xh.easy.easycache.base.ApplicationContextAdapter;
import com.xh.easy.easycache.base.Element;
import com.xh.easy.easycache.core.lua.LoadScriptVisitor;
import com.xh.easy.easycache.core.lua.LuaShPublisher;
import com.xh.easy.easycache.core.lua.RedisConnection;

/**
 * @author yixinhai
 */
public class NoScriptEvent implements Element {

    private final RedisConnection connection;
    private final LoadScriptVisitor visitor;

    public NoScriptEvent(RedisConnection connection) {
        this.connection = connection;
        this.visitor = ApplicationContextAdapter.getBeanByType(LuaShPublisher.class);
    }

    public RedisConnection getConnection() {
        return connection;
    }

    @Override
    public void accept() {
        visitor.visit(this);
    }
}

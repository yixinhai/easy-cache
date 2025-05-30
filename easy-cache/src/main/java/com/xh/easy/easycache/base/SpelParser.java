package com.xh.easy.easycache.base;

import org.springframework.context.expression.MethodBasedEvaluationContext;
import org.springframework.core.DefaultParameterNameDiscoverer;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.util.ObjectUtils;

import java.lang.reflect.Method;

/**
 * SPEL表达式解析类
 *
 * @author yixinhai
 */
public class SpelParser extends SpelExpressionParser {

    private final DefaultParameterNameDiscoverer discoverer = new DefaultParameterNameDiscoverer();

    private final String[] definitionKeys;

    private final Method method;

    private final Object[] parameterValues;

    public SpelParser(String[] definitionKeys, Method method, Object[] parameterValues) {
        this.definitionKeys = definitionKeys;
        this.method = method;
        this.parameterValues = parameterValues;
    }

    public String[] parse() {
        return parseSpELDefinitionKey(definitionKeys, method, parameterValues);
    };


    private String[] parseSpELDefinitionKey(String[] definitionKeys, Method method, Object[] parameterValues) {
        String[] resultArray = new String[definitionKeys.length];
        EvaluationContext context = new MethodBasedEvaluationContext(new Object(), method, parameterValues, discoverer);

        for(int i = 0; i < definitionKeys.length; ++i) {
            if (!ObjectUtils.isEmpty(definitionKeys[i])) {
                Object objKey = parseExpression(definitionKeys[i]).getValue(context);
                resultArray[i] = ObjectUtils.nullSafeToString(objKey);
            }
        }

        return resultArray;
    }
}

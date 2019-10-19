package sun.reflect;

import java.lang.reflect.InvocationTargetException;

/**
 * 这个接口提供给java.lang.reflect.Method.invoke ()
 * 每个方法对象是配置了一个(可能是动态生成的)类实现这个接口。
 */

public interface MethodAccessor {
    /**
     * Matches specification in {@link java.lang.reflect.Method}
     */
    public Object invoke(Object obj, Object[] args)
            throws IllegalArgumentException, InvocationTargetException;
}

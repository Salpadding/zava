package com.github.zava.core.reflect;

import com.google.common.util.concurrent.RateLimiter;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

public class ReflectUtil {
    // jdk 动态限流包装器
    public static class RateLimitInvocationHandler implements InvocationHandler {

        private final RateLimiter rateLimiter;
        private final Object proxyTarget;


        public RateLimitInvocationHandler(Object proxyTarget, int ops) {
            this.proxyTarget = proxyTarget;
            this.rateLimiter = RateLimiter.create(ops);
        }

        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            rateLimiter.acquire();
            return method.invoke(proxyTarget, args);
        }
    }

    public static <T> T limitOps(
        Class<T> methodInterface,  // 需要限流的方法声明在这里
        T obj,  // 需要限流的对象
        int ops // 每秒钟能操作多少次
    ) {
        if (ops <= 0) return obj;
        return (T) Proxy.newProxyInstance(
            obj.getClass().getClassLoader(),
            new Class[]{methodInterface},
            new RateLimitInvocationHandler(obj, ops)
        );
    }
}

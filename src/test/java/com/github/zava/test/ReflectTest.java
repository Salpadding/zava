package com.github.zava.test;

import com.github.zava.core.reflect.ReflectUtils;
import org.junit.jupiter.api.Test;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

public class ReflectTest {
    // 测试动态代理
    public static interface ToProxy {
        String needProxy();
    }

    public static interface NoProxy {
        String noProxy();
    }

    public static class Example implements ToProxy, NoProxy {

        @Override
        public String needProxy() {
            return "before proxy";
        }

        @Override
        public String noProxy() {
            return "before proxy";
        }
    }

    public static class ExampleInvokeHandler implements InvocationHandler {

        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            return "after proxy";
        }
    }

    @Test
    public void test1() {
        ToProxy toProxy = (ToProxy) Proxy.newProxyInstance(
            ReflectTest.class.getClassLoader(),
            new Class[]{ToProxy.class},
            new ExampleInvokeHandler()
        );
        System.out.println(toProxy.getClass().getName());
        System.out.println(toProxy.needProxy());
    }

    @Test
    public void test2() {
        ToProxy toProxy = ReflectUtils.limitOps(ToProxy.class, new Example(), 100);
    }
}

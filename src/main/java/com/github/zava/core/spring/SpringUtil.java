package com.github.zava.core.spring;


import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import net.bytebuddy.ByteBuddy;
import net.bytebuddy.implementation.MethodDelegation;
import net.bytebuddy.implementation.bind.annotation.AllArguments;
import net.bytebuddy.implementation.bind.annotation.Origin;
import net.bytebuddy.implementation.bind.annotation.RuntimeType;
import net.bytebuddy.implementation.bind.annotation.This;
import net.bytebuddy.matcher.ElementMatchers;
import org.springframework.context.ApplicationContext;
import org.springframework.core.env.Environment;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;
import org.springframework.util.ResourceUtils;

import java.io.FileInputStream;
import java.io.InputStream;
import java.lang.reflect.Method;

@Component
public class SpringUtil {
    private static ApplicationContext ctx;
    private static Environment env;

    public SpringUtil(ApplicationContext ctx, Environment env) {
        SpringUtil.ctx = ctx;
        SpringUtil.env = env;
    }

    @SneakyThrows
    public static InputStream getResource(String path) {
        return path.startsWith("classpath:") ?
            new ClassPathResource(path.replaceAll("^classpath:", ""))
                .getInputStream() :
            new FileInputStream(ResourceUtils.getFile(path));
    }

    public static <T> T getBean(Class<T> clazz) {
        return ctx.getBean(clazz);
    }

    @RequiredArgsConstructor
    public static class GetterInterceptor {
        private final Class clazz;

        @RuntimeType
        public Object interceptor(
            @This Object instance,
            @Origin Method method,
            @AllArguments Object[] args) throws Exception {
            try {
                clazz.getDeclaredMethod(method.getName(), method.getParameterTypes());
            } catch (NoSuchMethodException ex) {
                return null;
            }
            return SpringUtil.getBean(method.getReturnType());
        }
    }

    // 生成动态代理对象 用于运行时依赖注入
    @SneakyThrows
    public static <T> T createBeanGetter(Class<T> clazz) {
        Class<?> newClass = new ByteBuddy().subclass(clazz)
            .method(ElementMatchers.any())
            .intercept(MethodDelegation.to(new GetterInterceptor(clazz))).make()
            .load(SpringUtil.class.getClassLoader())
            .getLoaded();
        return (T) newClass.getDeclaredConstructor().newInstance();
    }
}

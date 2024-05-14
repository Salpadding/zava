package com.github.zava.core.spring;


import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;
import org.springframework.core.env.Environment;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;
import org.springframework.util.ReflectionUtils;
import org.springframework.util.ResourceUtils;

import java.io.FileInputStream;
import java.io.InputStream;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

@Component
@Slf4j(topic = "bean")
public class SpringUtil {
    private static ApplicationContext ctx;
    private static Environment env;

    public SpringUtil(ApplicationContext ctx, Environment env) {
        log.info("create {}", this.getClass().getName());
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
    public static class GetterInterceptor implements InvocationHandler {
        // 代理的接口
        private final Class clazz;

        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            if (ReflectionUtils.isObjectMethod(method))
                return method.invoke(this, args);

            clazz.getDeclaredMethod(method.getName(), method.getParameterTypes());
            return SpringUtil.getBean(method.getReturnType());
        }
    }

    // 生成动态代理对象 用于运行时依赖注入
    @SneakyThrows
    public static <T> T createBeanGetter(Class<T> clazz) {
        return (T) Proxy.newProxyInstance(
            SpringUtil.class.getClassLoader(),
            new Class[]{clazz}, new GetterInterceptor(clazz)
        );
    }
}

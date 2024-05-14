package com.github.zava.core.orm;

import com.google.common.reflect.Reflection;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.reflect.MethodUtils;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.cglib.proxy.MethodInterceptor;
import org.springframework.cglib.proxy.MethodProxy;
import org.springframework.util.ReflectionUtils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;

// 自动发现 xml 无需配置完全相同的路径
// 自动修改 xml 的 namespace
@RequiredArgsConstructor
public class ClassLoaderProxyHandler implements MethodInterceptor {
    private final String locationPrefix;
    private final ClassLoader delegate;

    public static class ClassLoaderImpl extends ClassLoader {
    }

    // 还原 exception
    public Object intercept(Object obj, Method method, Object[] args, MethodProxy proxy) throws Throwable {
        try {
            return interceptInternal(obj, method, args, proxy);
        } catch (InvocationTargetException ex) {
            throw ex.getTargetException();
        }
    }

    @SneakyThrows
    private Object interceptInternal(Object obj, Method method, Object[] args, MethodProxy proxy) throws InvocationTargetException {
        // object 的方法 不需要代理
        if(ReflectionUtils.isObjectMethod(method))
            return method.invoke(this, args);

        // 不需要代理的方法
        if (!method.getName().equals("getResourceAsStream") || method.getParameterTypes().length != 1 ||
            method.getParameterTypes()[0] != String.class
        ) {
            return method.invoke(delegate, args);
        }

        if (args[0] == null) throw new RuntimeException("invalid argument");
        String resource = (String) args[0];

        // 查看是否是 mapper
        String mapperInterfaceName = resource.replaceAll("/", ".")
            .replaceAll("\\.xml$", "");

        Class mapperInterface = null;
        if (StringUtils.isBlank(mapperInterfaceName)) return method.invoke(delegate, args);
        try {
            mapperInterface = delegate.loadClass(mapperInterfaceName);
        } catch (ClassNotFoundException ex) {
            // 不是 java class 不需要代理
            return method.invoke(delegate, args);
        }

        // 查看是否是 mybatis mapper
        if (!mapperInterface.isAnnotationPresent(Mapper.class))
            return method.invoke(delegate, args);

        String baseFileName = Paths.get(resource).getFileName().toString();

        // 确定是 mapper 了 进行魔改操作
        // 先读取文件
        InputStream inputStream = null;
        String filePath = Paths.get(locationPrefix, baseFileName).toString();
        if (filePath.startsWith("classpath:")) {
            inputStream = delegate.getResourceAsStream(
                filePath.replaceAll("^classpath:", "")
            );
        } else {
            inputStream = new FileInputStream(filePath);
        }

        if (inputStream == null)
            throw new RuntimeException(filePath + " not found");

        String xmlContent = IOUtils.toString(inputStream, StandardCharsets.UTF_8);

        // 补充 mapper.namespace
        String result = xmlContent.replaceAll(
            "<mapper([\\s\\t]+namespace=\"[\\s\\t]*\")?[\\s\\t]*>",
            "<mapper namespace=\"" + mapperInterfaceName + "\">"
        );
        return IOUtils.toInputStream(result, StandardCharsets.UTF_8);
    }
}

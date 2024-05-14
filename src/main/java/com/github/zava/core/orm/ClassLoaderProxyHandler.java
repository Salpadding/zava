package com.github.zava.core.orm;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.apache.commons.lang3.StringUtils;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.cglib.proxy.MethodInterceptor;
import org.springframework.cglib.proxy.MethodProxy;

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
        if (method.getName().equals("hashCode") && args.length == 0)
            return System.identityHashCode(this);
        if (method.getName().equals("equals") &&
            method.getParameterTypes().length == 1 &&
            method.getParameterTypes()[0] == Object.class)
            return this.equals(args[0]);

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
            return method.invoke(delegate, args);
        }

        if (!mapperInterface.isAnnotationPresent(Mapper.class))
            return method.invoke(delegate, args);

        String baseFileName = Paths.get(resource).getFileName().toString();

        // 确定是 mapper 了 进行魔改操作
        // 先读取文件
        InputStream inputStream = null;
        if (locationPrefix.startsWith("classpath:")) {
            try {
                inputStream = delegate.getResourceAsStream(
                    Paths.get(
                        locationPrefix.replaceAll("^classpath:", ""),
                        baseFileName
                    ).toString()
                );
            } catch (Exception ex) {
                throw new RuntimeException(ex);
            }
        } else {
            try {
                inputStream = new FileInputStream(Paths.get(
                    locationPrefix,
                    baseFileName
                ).getFileName().toFile());
            } catch (Exception ex) {
                throw new RuntimeException(ex);
            }
        }
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        byte[] buf = new byte[4096];

        int n = 0;
        while (true) {
            n = inputStream.read(buf);
            if (n <= 0)
                break;
            os.write(buf, 0, n);
        }
        inputStream.close();
        os.close();

        String xmlContent = os.toString(StandardCharsets.UTF_8);

        // 补充 mapper.namespace
        String result = xmlContent.replaceAll("<mapper([\\s\\t]+namespace=\"[\\s\\t]*\")?[\\s\\t]*>", "<mapper namespace=\"" + mapperInterfaceName + "\">");
        return new ByteArrayInputStream(result.getBytes(StandardCharsets.UTF_8));
    }
}

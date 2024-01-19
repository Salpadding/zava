package zava.common;

import lombok.SneakyThrows;

import java.lang.reflect.Method;
import java.util.Objects;

public class ObjectMapperWrapper {
    static Class<?> CLASS;

    @SneakyThrows
    static Class<?> loadClass() {
        if(CLASS != null) return CLASS;
        CLASS = Class.forName("com.fasterxml.jackson.databind.ObjectMapper");
        return CLASS;
    }

    private Object objectMapper;

    public ObjectMapperWrapper(Object objectMapper) {
        ReflectionUtils.mustAssignable(loadClass(),
                Objects.requireNonNull(objectMapper).getClass());
        this.objectMapper = objectMapper;
    }

    static Method CONVERT_VALUE;

    @SneakyThrows
    public Object convertValue(Object src, Class<?> dst) {
        if(CONVERT_VALUE == null) {
            CONVERT_VALUE = loadClass().getMethod(
                    "convertValue", Object.class, Class.class);
        }
        return CONVERT_VALUE.invoke(this.objectMapper, src, dst);
    }
}

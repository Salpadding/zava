package zava.common;

import lombok.SneakyThrows;

import java.lang.reflect.Method;
import java.util.Objects;

public class JsonNodeWrapper {
    static Class<?> CLASS;

    @SneakyThrows
    static Class<?> loadClass() {
        if(CLASS != null) return CLASS;
        CLASS = Class.forName("com.fasterxml.jackson.databind.JsonNode");
        return CLASS;
    }

    private Object json;

    public JsonNodeWrapper(Object json) {
        ReflectionUtils.mustAssignable(loadClass(),
                Objects.requireNonNull(json).getClass());
        this.json = json;
    }

    static Method GET;

    @SneakyThrows
    public Object get(String name) {
        if(GET == null)
            GET = loadClass().getMethod("get", String.class);

        return GET.invoke(json, name);
    }
}

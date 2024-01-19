package zava.common;

import lombok.SneakyThrows;

import java.lang.reflect.Method;
import java.util.Objects;

class ApplicationContextWrapper {
    public static final String CLASS_NAME = "org.springframework.beans.factory.BeanFactory";
    private static Class<?> CLAZZ;

    static Method GET_BEAN;

    @SneakyThrows
    static Class<?> loadClass() {
        if (CLAZZ != null) return CLAZZ;
        CLAZZ = Class.forName(CLASS_NAME);
        return CLAZZ;
    }

    private Object ctx;

    public ApplicationContextWrapper(Object ctx) {
        ReflectionUtils.mustAssignable(loadClass(),
                Objects.requireNonNull(ctx).getClass());
        this.ctx = ctx;
    }


    @SneakyThrows
    public Object getBean(Class<?> clazz) {
        if (GET_BEAN == null)
            GET_BEAN = loadClass().getMethod("getBean", Class.class);
        return GET_BEAN.invoke(this.ctx, clazz);
    }
}

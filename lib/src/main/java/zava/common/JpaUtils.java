package zava.common;

import lombok.SneakyThrows;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

public class JpaUtils {
    static Class<? extends Annotation> TRANSIENT;

    @SneakyThrows
    static Class<? extends Annotation> getTransient() {
        if(TRANSIENT != null) return TRANSIENT;
        TRANSIENT = (Class<? extends Annotation>) Class.forName("jakarta.persistence.Transient");
        return TRANSIENT;
    }

    @SneakyThrows
    public static int patch(Object objectMapper, Object dst, Object src) {
        int n = 0;
        if(dst == null || src == null) return n;
        ObjectMapperWrapper w = new ObjectMapperWrapper(objectMapper);
        if(!JsonNodeWrapper.loadClass().isAssignableFrom(src.getClass())) {
            // 转成 json node
            src = w.convertValue(src, JsonNodeWrapper.loadClass());
        }
        JsonNodeWrapper wrapper = new JsonNodeWrapper(src);
        Field[] fields = dst.getClass().getDeclaredFields();

        for(int i = 0; i < fields.length; i++) {
            if(Modifier.isTransient(fields[i].getModifiers())) continue;
            if(fields[i].isAnnotationPresent(getTransient())) continue;
            Object value = wrapper.get(fields[i].getName());
            if(value == null) continue;
            fields[i].setAccessible(true);
            fields[i].set(dst, w.convertValue(value, fields[i].getType()));
            n++;
        }
        return n;
    }
}

package zava.common;

import java.lang.annotation.Annotation;

public class ReflectionUtils {
    public static void mustAssignable(Class<?> dst, Class<?> src) {
        if (!dst.isAssignableFrom(
                src
        )) throw ExceptionUtils.message("require %s but %s found", dst.getName(), src.getName());
    }

    public static boolean matchAnnotation(Annotation[] annotations, String pattern) {
        if (annotations == null) return false;
        for(int i = 0; i < annotations.length; i++) {
            if(annotations[i].annotationType().getName().matches(pattern)) return true;
        }
        return false;
    }
}

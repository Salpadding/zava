package zava.common;

public class ReflectionUtils {
    public static void mustAssignable(Class<?> dst, Class<?> src) {
        if (!dst.isAssignableFrom(
                src
        )) throw ExceptionUtils.message("require %s but %s found", dst.getName(), src.getName());
    }
}

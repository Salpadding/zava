package zava.common;

public class ExceptionUtils {
    public static RuntimeException message(String fmt, Object... args) {
        return new RuntimeException(String.format(fmt, args));
    }
}

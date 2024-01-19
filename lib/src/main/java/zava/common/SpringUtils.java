package zava.common;

import lombok.SneakyThrows;

import java.lang.reflect.Field;

public class SpringUtils {

    // inject 更灵活的依赖注入
    public static void inject(Object applicationContext, Object o) {
        inject(applicationContext, o, o.getClass());
    }

    // inject 更灵活的依赖注入
    @SneakyThrows
    public static void inject(Object applicationContext, Object o, Class<?> clazz) {
        ApplicationContextWrapper ctx = new ApplicationContextWrapper(applicationContext);
        Field[] field = clazz.getDeclaredFields();

        for (int i = 0; i < field.length; i++) {
            if (!ReflectionUtils.matchAnnotation(field[i].getAnnotations(), ".*(Inject|Autowired)$")) continue;
            field[i].setAccessible(true);
            if (field[i].getType().isAssignableFrom(applicationContext.getClass())) {
                field[i].set(o, applicationContext);
            } else {
                Object bean = ctx.getBean(field[i].getType());
                field[i].set(o, bean);
            }
        }
    }

}

package zava.common;


import org.junit.Assert;
import org.junit.Test;


public class ReflectionUtilsTest {

    @Test
    public void test() {
        Assert.assertTrue(ReflectionUtils.matchAnnotation(Sample.class.getAnnotations(), ".*(Autowired|Entity)$"));
    }
}

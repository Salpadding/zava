package zava.common;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest
public class SpringUtilsTest {
    @Autowired
    private ApplicationContext context;

    public static class Example {
        @Inject
        public Config.ExampleBean bean;
        @Inject
        public ApplicationContext ctx;
    }

    @Test
    public void injectTest() {
        Example e = new Example();
        SpringUtils.inject(context, e);
        Assert.assertNotNull(e.bean);
        Assert.assertNotNull(e.ctx);
    }
}

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
public class ApplicationContextWrapperTest {
    @Autowired
    private ApplicationContext context;

    @Test
    public void testGetBean() {
        ApplicationContextWrapper w = new ApplicationContextWrapper(context);
        Assert.assertNotNull(w.getBean(Config.ExampleBean.class));
    }
}

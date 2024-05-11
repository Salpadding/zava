package com.github.zava.test;

import com.github.zava.core.spring.SpringUtil;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;

@SpringBootTest(classes = SpringTest.class)
@ComponentScan(basePackageClasses = SpringUtil.class)
public class SpringTest {
    public static class TestBean {
        public String getName() {
            return TestBean.class.getName();
        }
    }

    public interface BeanGetter {
        TestBean getTestBean();
    }

    @Bean
    public TestBean testBean() {
        return new TestBean();
    }

    @Bean
    public BeanGetter beanGetter() {
        return SpringUtil.createBeanGetter(BeanGetter.class);
    }

    @Test
    public void test1() {
        Assertions.assertNotNull(
            SpringUtil.getBean(BeanGetter.class).getTestBean()
        );
        Assertions.assertEquals(
            SpringUtil.getBean(BeanGetter.class).getTestBean().getName(),
            TestBean.class.getName()
        );
    }
}

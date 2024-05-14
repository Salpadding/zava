package com.github.zava.test;

import com.github.zava.core.spring.SpringUtil;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;

@SpringBootTest(classes = SpringTest.Config.class)
@Slf4j(topic = "bean")
public class SpringTest {
    @ComponentScan(basePackageClasses = SpringUtil.class)
    public static class Config {
        @Bean
        public TestBean testBean() {
            return new TestBean();
        }

        @Bean
        public BeanGetter beanGetter() {
            log.info("create {}", BeanGetter.class);
            return SpringUtil.createBeanGetter(BeanGetter.class);
        }
    }

    public static class TestBean {
        public TestBean() {
            log.info("create {}", this.getClass());
        }

        public String getName() {
            return TestBean.class.getName();
        }
    }

    public interface BeanGetter {
        TestBean getTestBean();
    }


    public SpringTest() {
        log.info("create {}", this.getClass().getName());
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

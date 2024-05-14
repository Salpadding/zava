package com.github.zava.test;

import com.github.zava.core.orm.MybatisUtil;
import com.github.zava.core.spring.SpringUtil;
import com.github.zava.test.mybatis.City;
import com.github.zava.test.mybatis.Country;
import com.github.zava.test.mybatis.Index;
import com.github.zava.test.mybatis.mappers.CityMapper;
import com.github.zava.test.mybatis.mappers.CountryMapper;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

import javax.sql.DataSource;
import java.util.Collections;
import java.util.Map;


@SpringBootTest(
    classes = MybatisTest.Config.class,
    properties = {
        "spring.datasource.url=jdbc:h2:mem:testdb",
        "spring.jpa.hibernate.ddl-auto=update",
        "spring.jpa.show-sql=true",
        "spring.datasource.username=sa",
        "spring.datasource.driverClassName=org.h2.Driver",
        "logging.level.com.github.zava.test.mybatis.mappers=DEBUG"
    }
)
@Slf4j(topic = "mb")
public class MybatisTest {
    public MybatisTest() {
        log.info("create {}", this.getClass());
    }

    @EntityScan(basePackageClasses = Index.class)
    @EnableJpaRepositories(basePackageClasses = Index.class)
    @SpringBootApplication(scanBasePackageClasses = SpringUtil.class)
    public static class Config {
        private boolean init = false;

        public Config() {
            log.info("create {}", this.getClass());
        }

        @Bean
        public SqlSessionFactory sqlSessionFactory(DataSource dataSource) {
            return MybatisUtil.createFactory(CityMapper.class, "classpath:mappers", dataSource);
        }

        @Bean
        public SqlSession sqlSession(SqlSessionFactory factory) {
            return factory.openSession();
        }

        @Bean
        public CityMapper cityMapper(SqlSession session) {
            return session.getMapper(CityMapper.class);
        }

        @Bean
        public CountryMapper countryMapper(SqlSession session) {
            return session.getMapper(CountryMapper.class);
        }
    }


    @Autowired
    private CityMapper cityMapper;

    @Autowired
    private CountryMapper countryMapper;

    @Autowired
    private Config config;

    public static final Map<String, String[]> countries = Map.of(
        "china", new String[]{"beijing", "shanghai"},
        "usa", new String[]{"new york", "chicago"}
    );

    // TestClass 的生命周期比 Spring 容器短
    @PostConstruct
    public synchronized void init() {
        if (config.init) return;
        config.init = true;
        log.info("init bean {}", this.getClass());
        for (Map.Entry<String, String[]> entry : countries.entrySet()) {
            Country c = Country.builder().name(entry.getKey()).build();
            countryMapper.create(c);
            for (String cityName : entry.getValue()) {
                cityMapper.create(City.builder().name(cityName).countryId(c.getId()).build());
            }
        }
    }

    @Test
    public void test1() {
        Assertions.assertFalse(
            cityMapper.findAll().isEmpty()
        );
    }

    @Test
    public void test2() {
        log.info("{}", countryMapper.findAll(Map.of("name", "usa")));
    }

    @Test
    public void test3() {
        Country newCountry = new Country(0, "france", null, null);
        countryMapper.create(newCountry);
        log.info("new country = {}", newCountry);
    }

    @Test
    public void test4() {
        log.info("{}", cityMapper.findAll(Collections.singletonMap("name", "shanghai")));
        log.info("{}", cityMapper.findAll());
    }
}

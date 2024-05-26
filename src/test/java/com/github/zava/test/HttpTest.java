package com.github.zava.test;

import com.github.zava.core.net.HttpUtil;
import lombok.SneakyThrows;
import org.apache.commons.io.IOUtils;
import org.apache.hc.client5.http.async.methods.SimpleHttpRequest;
import org.apache.hc.core5.http.ClassicHttpResponse;
import org.apache.hc.core5.http.HttpException;
import org.apache.hc.core5.http.io.HttpClientResponseHandler;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

@SpringBootTest(classes = HttpTest.Config.class)
public class HttpTest {
    @Configuration
    public static class Config {
        @Bean
        public HttpUtil httpUtil() {
            return HttpUtil.DEFAULT;
        }
    }

    @Autowired
    private HttpUtil httpUtil;

    @Test
    public void test() {
        String content = httpUtil
            .getForString("https://www.baidu.com");

        System.out.println(content);
    }
}

package com.github.zava.test;

import com.github.zava.core.date.Constants;
import com.github.zava.core.date.DateUtil;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.time.ZoneId;

public class DateTest {
    @Test
    public void test() {
        System.out.println(ZoneId.systemDefault());
        System.out.println(
            DateUtil.withLocale(LocalDateTime.now(), "Asia/Shanghai")
                .format(Constants.DATE_FORMAT)
        );
    }
}

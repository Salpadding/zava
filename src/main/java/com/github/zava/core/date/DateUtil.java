package com.github.zava.core.date;

import java.time.LocalDateTime;
import java.time.ZoneId;

public class DateUtil {
    // 用指定的时区显示当前时间
    public static LocalDateTime withLocale(LocalDateTime system, String locale) {
        ZoneId dst = ZoneId.of(locale);
        ZoneId from = ZoneId.systemDefault();
        return system.atZone(from).toOffsetDateTime()
            .atZoneSameInstant(dst)
            .toLocalDateTime();
    }
}

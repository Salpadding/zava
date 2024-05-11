package com.github.zava.core.script;

import lombok.SneakyThrows;
import org.apache.commons.jexl3.*;
import org.apache.commons.jexl3.introspection.JexlPermissions;

import java.util.Map;
import java.util.concurrent.ConcurrentSkipListMap;

// 执行 jexl 表达式
public class JexlUtil {
    public static final Map<String, JexlScript> CACHE = new ConcurrentSkipListMap<>();
    public static final JexlEngine ENGINE = new JexlBuilder()
        .permissions(JexlPermissions.UNRESTRICTED)
        .features(new JexlFeatures())
        .create();

    @SneakyThrows
    public static Object eval(Map<String, Object> payload, String expr) {
        JexlScript script = CACHE.get(expr);
        boolean put = script == null;
        if (script == null)
            script = ENGINE.createScript(expr);
        if (put)
            CACHE.put(expr, script);
        return script.execute(createContext(payload));
    }


    public static JexlContext createContext(Map<String, Object> msg) {
        MapContext ret = new MapContext();
        for (Map.Entry<String, Object> entry : msg.entrySet()) {
            ret.set(entry.getKey(), entry.getValue());
        }
        return ret;
    }
}

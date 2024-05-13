package com.github.zava.core.script;

import lombok.SneakyThrows;
import org.apache.commons.jexl3.*;
import org.apache.commons.jexl3.introspection.JexlPermissions;

import java.util.*;
import java.util.concurrent.ConcurrentSkipListMap;

public class JexlUtil {
    public static final Map<String, JexlScript> CACHE = new ConcurrentSkipListMap<>();
    public static final JexlEngine ENGINE = new JexlBuilder()
        .permissions(JexlPermissions.UNRESTRICTED)
        .features(new JexlFeatures())
        .create();


    public static List<Object> evalAll(Map<String, Object> payload, String[] expr) {
        return evalAll(payload, expr, false);
    }

    public static List<Object> evalAll(Map<String, Object> payload, Collection<? extends String> exprs) {
        return evalAll(payload, exprs, false);
    }

    public static List<Object> evalAll(Map<String, Object> payload, Collection<? extends String> exprs, boolean cache) {
        if (exprs == null) return Collections.emptyList();
        List<Object> ret = new ArrayList<>();
        for (String expr : exprs) {
            ret.add(eval(payload, expr, cache));
        }
        return ret;
    }

    public static List<Object> evalAll(Map<String, Object> payload, String[] expr, boolean cache) {
        if (expr == null) return Collections.emptyList();
        return evalAll(payload, Arrays.asList(expr), cache);
    }

    public static Object eval(Map<String, Object> payload, String expr) {
        return eval(payload, expr, false);
    }

    public static List<Object> evalAll(Collection<? extends Map<String, Object>> payloads, String expr, boolean cache) {
        if (payloads == null || payloads.isEmpty() || expr == null) return Collections.emptyList();
        List<Object> ret = new ArrayList<>();
        for (Map<String, Object> payload : payloads) {
            ret.add(eval(payload, expr, cache));
        }
        return ret;
    }

    @SneakyThrows
    public static Object eval(Map<String, Object> payload, String expr, boolean cache) {
        if (expr == null) return null;
        if (!cache)
            return ENGINE.createScript(expr).execute(createContext(payload));

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
        if (msg == null) return ret;
        for (Map.Entry<String, Object> entry : msg.entrySet()) {
            ret.set(entry.getKey(), entry.getValue());
        }
        return ret;
    }
}

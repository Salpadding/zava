package zava.common;

import lombok.Getter;

import java.util.HashMap;
import java.util.Map;

@Getter
public class MapBuilder<K, V> {
    private Map<K, V> data = new HashMap<>();

    public static MapBuilder builder() {
        return new MapBuilder();
    }

    public MapBuilder<K, V> put(K k, V v) {
        data.put(k, v);
        return this;
    }
}

package de.netbeacon.tools.jda.internal.command.container;

import java.util.*;
import java.util.stream.Collectors;

public class DataMap {

    private final Map<String, Object> dataMap;

    public DataMap() {
        this.dataMap = new HashMap<>();
    }

    public DataMap(Map<String, Object> dataMap) {
        this.dataMap = dataMap;
    }

    public static DataMap combine(DataMap a, DataMap b) {
        var map = new HashMap<>(a.getDataMap());
        map.putAll(b.getDataMap());
        return new DataMap(map);
    }

    public synchronized DataMap add(Object object) {
        return add(null, object);
    }

    public synchronized DataMap add(String name, Object object) {
        if (name == null || name.isBlank()) {
            if (!dataMap.containsKey("")) {
                dataMap.put("", new ArrayList<>());
            }
            ((List<Object>) dataMap.get("")).add(object);
        } else {
            dataMap.put(name, object);
        }
        return this;
    }

    public Object get(String name) {
        return dataMap.get(name);
    }

    public boolean containsKey(String name) {
        return dataMap.containsKey(name);
    }

    public List<Object> get(Class<?> clazz) {
        return dataMap.values().stream().filter(Objects::nonNull).filter(e -> clazz.isAssignableFrom(e.getClass())).collect(Collectors.toList());
    }

    public Map<String, Object> getDataMap() {
        return dataMap;
    }

}

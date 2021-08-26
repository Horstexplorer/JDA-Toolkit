package de.netbeacon.tools.jda.internal.command.utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MapUtil {

    private MapUtil(){}

    public static <T> Map<T, List<Object>> merge(Map<T, List<Object>> a, Map<T, List<Object>> b){
        var combined = new HashMap<>(a);
        for(var key : b.keySet()){
            if(!combined.containsKey(key)){
                combined.put(key, new ArrayList<>());
            }
            combined.get(key).addAll(b.get(key));
        }
        return combined;
    }

}

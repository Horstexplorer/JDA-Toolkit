package de.netbeacon.tools.jda.internal.language.packag;

import de.netbeacon.tools.jda.api.language.packag.LanguagePackage;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class LanguagePackageImp implements LanguagePackage {

    private final String id;
    private final String name;
    private final String description;
    private final boolean isDefault;
    private final ConcurrentHashMap<String, String> translations = new ConcurrentHashMap<>();

    public LanguagePackageImp(JSONObject jsonObject){
        id = jsonObject.getString("languageId").toLowerCase();
        name = jsonObject.getString("languageName");
        description = jsonObject.getString("languageDescription");
        isDefault = jsonObject.getBoolean("isDefault");
        // build keys
        var a = toSimpleAccessors(null, jsonObject.getJSONObject("translations"));
        translations.putAll(a);
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public String getFullName() {
        return name;
    }

    @Override
    public String getDescription() {
        return description;
    }

    @Override
    public boolean isDefault() {
        return isDefault;
    }

    @Override
    public String getTranslation(String key, String... placeholderInserts) {
        String translation = translations.get(key);
        if(translation == null){
            return "Missing translation for: " + key;
        }
        int i = 0;
        for(Object o : placeholderInserts){
            String placeholder = "%" + i + "%";
            translation = translation.replace(placeholder, o.toString());
            i++;
        }
        return translation;
    }

    private Map<String, String> toSimpleAccessors(String prefix, Object o){
        Map<String, String> map = new HashMap<>();
        if(o instanceof JSONObject){
            for(String key : ((JSONObject) o).keySet()){
                map.putAll(toSimpleAccessors((prefix == null ? "" : prefix + ".") + key, ((JSONObject) o).get(key)));
            }
        }
        else{
            map.put(prefix, o.toString());
        }
        return map;
    }
}

package de.netbeacon.tools.jda.api.language.packag;

import de.netbeacon.tools.jda.internal.language.packag.LanguagePackageImp;
import org.json.JSONObject;

public interface LanguagePackage {

    String getId();

    String getFullName();

    String getDescription();

    boolean isDefault();

    default String getTranslation(Class<?> clazz, String key, String... placeholderInserts){
        return getTranslation(clazz.getName() + "." + key);
    }

    String getTranslation(String key, String... placeholderInserts);

    static LanguagePackage from(JSONObject jsonObject){
        return new LanguagePackageImp(jsonObject);
    }

}

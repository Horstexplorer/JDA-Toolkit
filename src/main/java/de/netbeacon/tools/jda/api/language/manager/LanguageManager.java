package de.netbeacon.tools.jda.api.language.manager;

import de.netbeacon.tools.jda.api.language.packag.LanguagePackage;
import de.netbeacon.tools.jda.internal.language.packag.LanguagePackageImp;
import org.json.JSONObject;

import java.util.List;

public interface LanguageManager {

    boolean containsLanguage(String id);

    LanguagePackage getDefaultPackage();

    LanguagePackage getPackage(String id);

    LanguageManager addPackage(LanguagePackage languagePackage);

    List<LanguagePackage> getPackages();

    List<String> getPackageIds();

}

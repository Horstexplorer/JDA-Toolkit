package de.netbeacon.tools.jda.internal.language.manager;

import de.netbeacon.tools.jda.api.language.manager.LanguageManager;
import de.netbeacon.tools.jda.api.language.packag.LanguagePackage;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class LanguageManagerImp implements LanguageManager {

    private final ConcurrentHashMap<String, LanguagePackage> packages = new ConcurrentHashMap<>();

    private LanguageManagerImp() {
    }

    public static LanguageManager create() {
        return new LanguageManagerImp();
    }

    public static LanguageManager create(JSONObject... languagePackages) {
        return create(Arrays.stream(languagePackages).map(LanguagePackage::from).toArray(LanguagePackage[]::new));
    }

    public static LanguageManager create(LanguagePackage... languagePackages) {
        var lmi = new LanguageManagerImp();
        for (var lp : languagePackages)
            lmi.addPackage(lp);
        return lmi;
    }

    @Override
    public boolean containsLanguage(String id) {
        return packages.containsKey(id);
    }

    @Override
    public LanguagePackage getDefaultPackage() {
        return packages.values().stream().filter(LanguagePackage::isDefault).findFirst().orElse(null);
    }

    @Override
    public LanguagePackage getPackage(String id) {
        return packages.get(id);
    }

    @Override
    public LanguageManager addPackage(LanguagePackage languagePackage) {
        packages.put(languagePackage.getId(), languagePackage);
        return this;
    }

    @Override
    public List<LanguagePackage> getPackages() {
        return new ArrayList<>(packages.values());
    }

    @Override
    public List<String> getPackageIds() {
        return packages.values().stream().map(LanguagePackage::getId).collect(Collectors.toList());
    }
}

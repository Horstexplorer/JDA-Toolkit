package de.netbeacon.tools.jda.api.language.manager;

import de.netbeacon.tools.jda.api.language.packag.LanguagePackage;
import de.netbeacon.tools.jda.internal.language.packag.LanguagePackageImp;
import org.json.JSONObject;

import java.util.List;

/**
 * Represents a container managing language
 */
public interface LanguageManager {

    /**
     * Checks if the manager contains a language with the specified id
     *
     * @param id of the language
     * @return true if found
     */
    boolean containsLanguage(String id);

    /**
     * Returns the language package set up as default
     *
     * @return LanguagePackage or null if none has been set
     */
    LanguagePackage getDefaultPackage();

    /**
     * Returns the language package with the given id
     *
     * @param id of the desired package
     * @return LanguagePackage or null if none has been found
     */
    LanguagePackage getPackage(String id);

    /**
     * Adds a language package to the language manager
     *
     * @param languagePackage to add
     * @return current instance of the manager, useful for chaining
     */
    LanguageManager addPackage(LanguagePackage languagePackage);

    /**
     * Returns a list of all language packages contained within this manager
     *
     * @return List
     */
    List<LanguagePackage> getPackages();

    /**
     * Returns a list of all language ids
     *
     * @return List
     */
    List<String> getPackageIds();

}

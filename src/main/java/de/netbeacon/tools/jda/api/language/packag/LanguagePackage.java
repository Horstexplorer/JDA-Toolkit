package de.netbeacon.tools.jda.api.language.packag;

import de.netbeacon.tools.jda.internal.language.packag.LanguagePackageImp;
import org.json.JSONObject;

/**
 * Represents a container to be used to translate descriptive content provided to the user
 */
public interface LanguagePackage {

    /**
     * Creates a LanguagePackage from a JSONObject
     * <p>
     * The JSONObject needs to follow the following format
     * <pre>
     *  {
     *      "id": "id",
     *      "name": "full name",
     *      "description": "short description",
     *      isDefault: false,
     *      translations: {
     *          ...
     *
     *          foo: {
     *              bar: {
     *                  package: {
     *                      class: {
     *                          "key": "value"
     *                      }
     *                  }
     *              }
     *          }
     *
     *          ...
     *      }
     *  }
     * </pre>
     *
     * @param jsonObject jsonobject
     * @return LanguagePackage
     */
    static LanguagePackage from(JSONObject jsonObject) {
        return new LanguagePackageImp(jsonObject);
    }

    /**
     * Returns the language id
     * <p>
     * Could be something like: en_us, ...
     *
     * @return String
     */
    String getId();

    /**
     * Returns the full name of the language
     * <p>
     * Could be something like: English_US
     *
     * @return String
     */
    String getFullName();

    /**
     * Returns a brief description of the language
     * <p>
     * Could be something like: Contains translation assets in us English provided by @translator
     *
     * @return String
     */
    String getDescription();

    /**
     * Whether this language package should act as default
     *
     * @return boolean
     */
    boolean isDefault();

    /**
     * Overload of {@link LanguagePackage#getTranslation(String, String...)}
     *
     * @param clazz              which will be prepended as full.package.Classname to the key string
     * @param key                to identify which translation data is wanted
     * @param placeholderInserts to replace ordered placeholders of the format %0% %1% ... with
     * @return String
     */
    default String getTranslation(Class<?> clazz, String key, String... placeholderInserts) {
        return getTranslation(clazz.getName() + "." + key);
    }

    /**
     * Returns a translation for the given key and replaces placeholders with the given inserts
     *
     * @param key                to identify which translation data is wanted
     * @param placeholderInserts to replace ordered placeholders of the format %0% %1% ... with
     * @return String
     */
    String getTranslation(String key, String... placeholderInserts);

}

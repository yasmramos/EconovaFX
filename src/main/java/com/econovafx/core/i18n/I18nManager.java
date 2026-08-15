package com.econovafx.core.i18n;

import java.util.Locale;
import java.util.ResourceBundle;

/**
 * Internationalization manager for the application.
 * Loads and provides access to resource bundles based on the configured locale.
 */
public class I18nManager {
    
    private static final String BUNDLE_NAME = "i18n.messages";
    private static ResourceBundle bundle;
    private static Locale currentLocale;
    
    /**
     * Initializes the I18n manager with the specified locale.
     * Should be called during application startup.
     * 
     * @param locale the locale to use for translations
     */
    public static void init(Locale locale) {
        currentLocale = locale;
        bundle = ResourceBundle.getBundle(BUNDLE_NAME, locale);
    }
    
    /**
     * Initializes the I18n manager with the default locale (Spanish - Cuba).
     */
    public static void init() {
        init(new Locale("es", "CU"));
    }
    
    /**
     * Gets the string value for the given key.
     * 
     * @param key the translation key
     * @return the translated string, or the key itself if not found
     */
    public static String get(String key) {
        if (bundle == null) {
            init();
        }
        try {
            return bundle.getString(key);
        } catch (Exception e) {
            // Return key if not found (helps identify missing translations)
            return "%" + key;
        }
    }
    
    /**
     * Gets the string value for the given key with format arguments.
     * 
     * @param key the translation key
     * @param args the format arguments
     * @return the translated and formatted string
     */
    public static String get(String key, Object... args) {
        String pattern = get(key);
        return String.format(pattern, args);
    }
    
    /**
     * Gets the current locale.
     * 
     * @return the current locale
     */
    public static Locale getCurrentLocale() {
        if (currentLocale == null) {
            currentLocale = new Locale("es", "CU");
        }
        return currentLocale;
    }
    
    /**
     * Sets a new locale and reloads the bundle.
     * 
     * @param locale the new locale
     */
    public static void setLocale(Locale locale) {
        currentLocale = locale;
        bundle = ResourceBundle.getBundle(BUNDLE_NAME, locale);
    }
    
    /**
     * Gets the resource bundle for use with FXMLLoader.
     * 
     * @return the current resource bundle
     */
    public static ResourceBundle getBundle() {
        if (bundle == null) {
            init();
        }
        return bundle;
    }
}

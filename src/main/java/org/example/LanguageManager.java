package org.example;

import java.util.Locale;
import java.util.ResourceBundle;

public class LanguageManager {

    // Keeps track of which language is currently selected app-wide
    private static Locale currentLocale = new Locale("en");
    private static ResourceBundle bundle = ResourceBundle.getBundle("messages", currentLocale);

    // Call this when user picks a language from the dropdown
    public static void setLanguage(String languageCode) {
        currentLocale = new Locale(languageCode); // "en", "hi", or "bho"
        bundle = ResourceBundle.getBundle("messages", currentLocale);
    }

    // Call this anywhere you need translated text, passing the key
    public static String get(String key) {
        return bundle.getString(key);
    }

    public static Locale getCurrentLocale() {
        return currentLocale;
    }
}
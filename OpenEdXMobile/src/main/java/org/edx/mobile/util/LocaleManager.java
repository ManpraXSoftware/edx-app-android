package org.edx.mobile.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Build;
import android.preference.PreferenceManager;

import androidx.annotation.StringDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class LocaleManager {
    @Retention(RetentionPolicy.SOURCE)
    @StringDef({ ENGLISH, HINDI,KANNADA,TAMIL,MALAYALAM,ODIA})
    public @interface LocaleDef {
        String[] SUPPORTED_LOCALES = { ENGLISH, HINDI,KANNADA,TAMIL,MALAYALAM,ODIA};
    }

    public static final String ENGLISH = "en";
    public static final String HINDI = "hi";
    public static final String SPANISH = "es";
    public static final String MARATHI = "mr";
    public static final String KANNADA = "kn";
    public static final String TELUGU = "te";

    public static final String MALAYALAM = "ml";
    public static final String ODIA = "or";
    public static final String TAMIL = "ta";
    public static final String Empty = "";

    private static final Map<String, String> LANGUAGE_MAP = new HashMap<>();

    static {
        LANGUAGE_MAP.put(ENGLISH, "English");
        LANGUAGE_MAP.put(HINDI, "Hindi");
        LANGUAGE_MAP.put(SPANISH, "Spanish");
        LANGUAGE_MAP.put(MARATHI, "Marathi");
        LANGUAGE_MAP.put(KANNADA, "Kannada");
        LANGUAGE_MAP.put(TELUGU, "Telugu");
        LANGUAGE_MAP.put(MALAYALAM, "Malayalam");
        LANGUAGE_MAP.put(ODIA, "Odia");
        LANGUAGE_MAP.put(TAMIL, "Tamil");
    }

    public static String getFullLanguageName(String languageCode) {
        return LANGUAGE_MAP.getOrDefault(languageCode, "Unknown Language");
    }
    /**
     * SharedPreferences Key
     */
    private static final String LANGUAGE_KEY = "language_key";
    /**
     * set current pref locale
     */
    public static Context setLocale(Context mContext) {
        return updateResources(mContext, getLanguagePref(mContext));
    }
    /**
     * Set new Locale with context
     */
    public static Context setNewLocale(Context mContext, @LocaleDef String language) {
        setLanguagePref(mContext, language);
        return updateResources(mContext, language);
    }
    /**
     * Get saved Locale from SharedPreferences
     *
     * @param mContext current context
     * @return current locale key by default return english locale
     */
    public static String getLanguagePref(Context mContext) {
        SharedPreferences mPreferences = PreferenceManager.getDefaultSharedPreferences(mContext);
        String selected_language=mPreferences.getString(LANGUAGE_KEY, Empty);
        if(selected_language.equals("ml")){
            selected_language="ml-IN";
        }
        return selected_language;
    }
    /**
     * set pref key
     */
    private static void setLanguagePref(Context mContext, String localeKey) {
        SharedPreferences mPreferences = PreferenceManager.getDefaultSharedPreferences(mContext);
        mPreferences.edit().putString(LANGUAGE_KEY, localeKey).apply();
    }
    /**
     * update resource
     */
    private static Context updateResources(Context context, String language) {
        if(language.equals("ml-IN")){
            language="ml";
        }
        Locale locale = new Locale(language);
        Locale.setDefault(locale);
        Resources res = context.getResources();
        Configuration config = new Configuration(res.getConfiguration());
        if (Build.VERSION.SDK_INT >= 17) {
            config.setLocale(locale);
            context = context.createConfigurationContext(config);
        } else {
            config.locale = locale;
            res.updateConfiguration(config, res.getDisplayMetrics());
        }
        return context;
    }
    /**
     * get current locale
     */
    public static Locale getLocale(Resources res) {
        Configuration config = res.getConfiguration();
        return Build.VERSION.SDK_INT >= 24 ? config.getLocales().get(0) : config.locale;
    }
}

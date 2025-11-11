package com.company.calculatar;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.Map;

public class PreferenceManager {
    private final SharedPreferences sharedPreferences;

    public PreferenceManager(Context context) {
        sharedPreferences = context.getSharedPreferences("chatAppPreferences", Context.MODE_PRIVATE);
    }

    public void putBoolean(String key, boolean value) {
        sharedPreferences.edit().putBoolean(key, value).apply();
    }

    public boolean getBoolean(String key, boolean defaultValue) {
        return sharedPreferences.getBoolean(key, defaultValue);
    }

    public void putString(String key, String value) {
        sharedPreferences.edit().putString(key, value).apply();
    }

    public String getString(String key) {
        return sharedPreferences.getString(key, null);
    }

    public void clear() {
        sharedPreferences.edit().clear().apply();
    }

    public void remove(String key) {
        sharedPreferences.edit().remove(key).apply();
    }

    public Map<String, ?> getAll() {
        return sharedPreferences.getAll();
    }
}

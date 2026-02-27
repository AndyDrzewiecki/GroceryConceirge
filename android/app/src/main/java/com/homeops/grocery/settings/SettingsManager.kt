package com.homeops.grocery.settings

import android.content.Context
import com.homeops.grocery.GroceryApplication
import com.homeops.grocery.config.AppConfig

/**
 * Persists and retrieves the user-configured backend BASE_URL using
 * SharedPreferences.
 *
 * All callers use the static helpers so ViewModels never need a Context.
 */
object SettingsManager {

    private const val PREFS_NAME = "homeops_prefs"
    private const val KEY_BASE_URL = "base_url"

    private val prefs by lazy {
        GroceryApplication.instance.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    /** Returns the currently saved BASE_URL, defaulting to AppConfig.DEFAULT_BASE_URL. */
    fun getBaseUrl(): String {
        return prefs.getString(KEY_BASE_URL, AppConfig.DEFAULT_BASE_URL)
            ?: AppConfig.DEFAULT_BASE_URL
    }

    /**
     * Persists [url] as the new BASE_URL.
     * Ensures the value ends with "/" before saving.
     */
    fun saveBaseUrl(url: String) {
        val normalised = if (url.endsWith("/")) url else "$url/"
        prefs.edit().putString(KEY_BASE_URL, normalised).apply()
    }
}

package com.example.myapp.util

import android.content.Context
import android.content.res.Configuration
import android.content.res.Resources
import android.os.Build
import android.preference.PreferenceManager
import java.util.Locale

/**
 * Утилитный класс для управления языками в приложении
 */
object LanguageUtil {
    private const val SELECTED_LANGUAGE = "selected_language"
    
    /**
     * Список поддерживаемых языков
     */
    enum class Language(val code: String) {
        KAZAKH("kk"),
        RUSSIAN("ru"),
        ENGLISH("en")
    }
    
    /**
     * Применение выбранного языка к контексту
     */
    fun applyLanguage(context: Context, languageCode: String): Context {
        saveLanguage(context, languageCode)
        
        val locale = Locale(languageCode)
        Locale.setDefault(locale)
        
        val resources = context.resources
        val configuration = Configuration(resources.configuration)
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            configuration.setLocale(locale)
            return context.createConfigurationContext(configuration)
        } else {
            configuration.locale = locale
            resources.updateConfiguration(configuration, resources.displayMetrics)
            return context
        }
    }
    
    /**
     * Сохранение выбранного языка в SharedPreferences
     */
    private fun saveLanguage(context: Context, languageCode: String) {
        val preferences = PreferenceManager.getDefaultSharedPreferences(context)
        preferences.edit().putString(SELECTED_LANGUAGE, languageCode).apply()
    }
    
    /**
     * Получение текущего выбранного языка
     */
    fun getLanguage(context: Context): String {
        val preferences = PreferenceManager.getDefaultSharedPreferences(context)
        return preferences.getString(SELECTED_LANGUAGE, Language.KAZAKH.code) ?: Language.KAZAKH.code
    }
    
    /**
     * Применение сохраненного языка при запуске приложения
     */
    fun applyInitialLanguage(context: Context): Context {
        return applyLanguage(context, getLanguage(context))
    }
} 
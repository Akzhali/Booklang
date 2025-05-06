package com.example.myapp

import android.app.Application
import android.content.Context
import android.util.Log
import com.example.myapp.util.BookManager
import com.example.myapp.util.LanguageUtil
import com.google.firebase.FirebaseApp
import com.google.firebase.appcheck.FirebaseAppCheck
import com.google.firebase.appcheck.debug.DebugAppCheckProviderFactory
import com.google.firebase.appcheck.playintegrity.PlayIntegrityAppCheckProviderFactory

class MyApplication : Application() {
    
    companion object {
        private const val TAG = "MyApplication"
        
        // Set this to true for development, false for production
        private const val USE_DEBUG_PROVIDER = true
    }
    
    override fun onCreate() {
        super.onCreate()
        
        try {
            // Initialize Firebase
            FirebaseApp.initializeApp(this)
            
            // Initialize Firebase App Check
            val firebaseAppCheck = FirebaseAppCheck.getInstance()
            
            if (USE_DEBUG_PROVIDER) {
                // Use debug provider for development
                Log.d(TAG, "Using debug App Check provider")
                firebaseAppCheck.installAppCheckProviderFactory(
                    DebugAppCheckProviderFactory.getInstance()
                )
            } else {
                // Use Play Integrity provider for production
                Log.d(TAG, "Using Play Integrity App Check provider")
                firebaseAppCheck.installAppCheckProviderFactory(
                    PlayIntegrityAppCheckProviderFactory.getInstance()
                )
            }
            
            Log.d(TAG, "Firebase App Check initialized successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Error initializing Firebase App Check: ${e.message}", e)
        }
        
        // Инициализация менеджера книг
        BookManager.initialize(this)
    }
    
    override fun attachBaseContext(base: Context) {
        // Применяем сохраненный язык при запуске приложения
        super.attachBaseContext(LanguageUtil.applyInitialLanguage(base))
    }
} 
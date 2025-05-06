package com.example.myapp.util

import android.content.Context
import android.util.Log
import android.widget.Toast
import com.google.firebase.appcheck.FirebaseAppCheck
import com.google.firebase.appcheck.debug.DebugAppCheckProviderFactory

/**
 * Utility class for Firebase App Check debugging
 * Only use this in development builds!
 */
object AppCheckDebugUtils {
    
    private const val TAG = "AppCheckDebug"
    
    /**
     * Generates and displays a debug token for Firebase App Check
     * Call this method from a debug menu or developer settings screen
     */
    fun generateAndShowDebugToken(context: Context) {
        try {
            // Get the debug token
            val firebaseAppCheck = FirebaseAppCheck.getInstance()
            
            firebaseAppCheck.getAppCheckToken(false)
                .addOnSuccessListener { appCheckToken ->
                    val token = appCheckToken.token
                    Log.d(TAG, "Debug token: $token")
                    
                    // Show the token to the developer
                    Toast.makeText(context, "Debug token: $token", Toast.LENGTH_LONG).show()
                    
                    // Copy to clipboard
                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
                    val clip = android.content.ClipData.newPlainText("Firebase App Check Debug Token", token)
                    clipboard.setPrimaryClip(clip)
                    
                    Toast.makeText(context, "Token copied to clipboard", Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener { e ->
                    Log.e(TAG, "Error getting debug token: ${e.message}", e)
                    Toast.makeText(context, "Error getting debug token: ${e.message}", Toast.LENGTH_LONG).show()
                }
        } catch (e: Exception) {
            Log.e(TAG, "Error generating debug token: ${e.message}", e)
            Toast.makeText(context, "Error generating debug token: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }
} 
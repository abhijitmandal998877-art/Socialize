package com.example.data

import android.content.Context
import android.util.Log
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

object FirebaseService {
    private const val TAG = "FirebaseService"
    
    val auth: FirebaseAuth by lazy {
        FirebaseAuth.getInstance()
    }
    
    val firestore: FirebaseFirestore by lazy {
        FirebaseFirestore.getInstance()
    }

    fun initialize(context: Context) {
        try {
            if (FirebaseApp.getApps(context).isEmpty()) {
                val options = FirebaseOptions.Builder()
                    .setApiKey("AIzaSyDRzUQo60-SpOG5re-kbluni6yXS4LTz8g")
                    .setApplicationId("1:616662871489:android:90bf76be4eb596a5129340")
                    .setProjectId("socialize-790")
                    .build()
                FirebaseApp.initializeApp(context, options)
                Log.d(TAG, "Firebase initialized programmatically successfully")
            } else {
                Log.d(TAG, "Firebase already initialized")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to initialize Firebase programmatically", e)
        }
    }
}

package com.example

import android.app.Application
import com.revenuecat.purchases.Purchases
import com.revenuecat.purchases.PurchasesConfiguration

import com.revenuecat.purchases.LogLevel
import com.revenuecat.purchases.interfaces.UpdatedCustomerInfoListener
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions

class PerennialApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        
        FirebaseApp.initializeApp(this)
        
        val apiKey = BuildConfig.REVENUECAT_API_KEY
        try {
            if (apiKey.isNotEmpty() && apiKey != "YOUR_REVENUECAT_ANDROID_API_KEY") {
                Purchases.logLevel = LogLevel.DEBUG
                Purchases.configure(
                    PurchasesConfiguration.Builder(this, apiKey).build()
                )

                Purchases.sharedInstance.updatedCustomerInfoListener = UpdatedCustomerInfoListener { customerInfo ->
                    val hasPlus = customerInfo.entitlements["Perennial Pro"]?.isActive == true
                    val tier = if (hasPlus) "plus" else "free"
                    
                    val auth = FirebaseAuth.getInstance()
                    val userId = auth.currentUser?.uid
                    if (userId != null) {
                        val db = FirebaseFirestore.getInstance()
                        db.collection("users").document(userId).update("subscriptionTier", tier)
                            .addOnFailureListener {
                                db.collection("users").document(userId).set(mapOf("subscriptionTier" to tier), SetOptions.merge())
                            }
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}

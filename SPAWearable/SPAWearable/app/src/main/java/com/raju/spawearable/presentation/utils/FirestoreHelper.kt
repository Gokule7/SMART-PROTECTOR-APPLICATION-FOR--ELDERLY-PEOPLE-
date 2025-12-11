package com.raju.spawearable.presentation.utils

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

object FirestoreHelper {
    private val db: FirebaseFirestore by lazy { FirebaseFirestore.getInstance() }

    suspend fun getUserData(phone: String): Elder? {
        return try {
            val snapshot = db.collection(Constants.KEY_ELDER_COLLECTION)
                .whereEqualTo(Constants.KEY_ELDER_PHONE, phone)
                .get()
                .await()

            if (!snapshot.isEmpty) {
                val elder = snapshot.documents.firstOrNull()?.toObject(Elder::class.java)
                if (elder != null) {
                    Log.d("Firestore", "User Data: $elder")
                    elder
                } else {
                    Log.d("Firestore", "Document found, but data is null!")
                    null
                }
            } else {
                Log.d("Firestore", "No matching documents found!")
                null
            }
        } catch (e: Exception) {
            Log.e("FirestoreHelper", "Error fetching user data: ${e.message}", e)
            null
        }
    }

    suspend fun getCaretakerFcmToken(elderPhone: String): String? {
        return try {
            // First query: Get connected caretaker phone
            val connectQuery = db
                .collection(Constants.KEY_CONNECT_COLLECTION)
                .whereEqualTo(Constants.KEY_ELDER_PHONE, elderPhone)
                .get()
                .await()

            if (connectQuery.isEmpty) {
                return null  // No connected caretakers
            }

            val caretakerPhone = connectQuery.documents[0]
                .getString(Constants.KEY_CARETAKER_PHONE)
                ?: return null

            // Second query: Get caretaker's FCM token
            val caretakerQuery =db
                .collection(Constants.KEY_CARETAKER_COLLECTION)
                .whereEqualTo(Constants.KEY_CARETAKER_PHONE, caretakerPhone)
                .get()
                .await()

            if (caretakerQuery.isEmpty) {
                return null  // Caretaker record not found
            }

            caretakerQuery.documents[0].getString(Constants.KEY_FCM_TOKEN)
        } catch (e: Exception) {
            Log.e("Firestore", "Error getting caretaker FCM", e)
            null
        }
    }
}

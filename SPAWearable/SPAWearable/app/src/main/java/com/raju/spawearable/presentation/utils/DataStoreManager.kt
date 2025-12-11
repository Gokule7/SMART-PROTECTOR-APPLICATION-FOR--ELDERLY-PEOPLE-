package com.raju.spawearable.presentation.utils

import android.content.Context
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking

private val Context.dataStore by preferencesDataStore(name = Constants.KEY_PREFERENCE_NAME)

object DataStoreManager {

    // Generic function to save any key-value pair
    suspend fun saveData(context: Context, key: String, value: String) {
        val dataKey = stringPreferencesKey(key)
        context.dataStore.edit { prefs ->
            prefs[dataKey] = value
        }
    }

    suspend fun saveData(context: Context, key: String, value: Boolean) {
        val dataKey = booleanPreferencesKey(key)
        context.dataStore.edit { prefs ->
            prefs[dataKey] = value
        }
    }

    // Generic function to get a value by key
    fun getData(context: Context, key: String): String? {
        val dataKey = stringPreferencesKey(key)
        return runBlocking {
            val prefs = context.dataStore.data.first()
            prefs[dataKey]
        }
    }

    fun getBooleanData(context: Context, key: String): Boolean? {
        val dataKey = booleanPreferencesKey(key)
        return runBlocking {
            val prefs = context.dataStore.data.first()
            prefs[dataKey]
        }
    }

    // Function to remove a specific key
    suspend fun removeData(context: Context, key: String) {
        val dataKey = stringPreferencesKey(key)
        context.dataStore.edit { prefs ->
            prefs.remove(dataKey)
        }
    }

    // Function to clear all stored preferences
    suspend fun clearAllData(context: Context) {
        context.dataStore.edit { prefs ->
            prefs.clear()
        }
    }
}

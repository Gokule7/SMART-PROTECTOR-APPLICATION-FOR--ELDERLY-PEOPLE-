package com.raju.spawearable.presentation.screens

import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.OutlinedTextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.wear.compose.material.Button
import androidx.wear.compose.material.CircularProgressIndicator
import androidx.wear.compose.material.Text
import com.raju.spawearable.presentation.utils.Constants
import com.raju.spawearable.presentation.utils.DataStoreManager
import com.raju.spawearable.presentation.utils.Elder
import com.raju.spawearable.presentation.utils.FirestoreHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun LoginScreen(
    navController: NavController  // Now passes Elder data on success
) {
    var phoneNumber by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()


    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Error message
        errorMessage?.let {
            Text(
                text = it,
                color = Color.Red,
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }

        // Phone input
        OutlinedTextField(
            value = phoneNumber,
            onValueChange = {
                phoneNumber = it
                errorMessage = null // Clear error when typing
            },
            label = { Text("Phone Number") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Login button
        Button(
            onClick = {
                if (phoneNumber.length < 10) {
                    errorMessage = "Please enter a valid phone number"
                    return@Button
                }

                coroutineScope.launch {
                    isLoading = true
                    errorMessage = null

                    try {
                        // 1. Check Firestore for user
                        val elder = FirestoreHelper.getUserData(phoneNumber)

                        if (elder != null) {
                            // 2. Save phone if user exists
                            DataStoreManager.saveData(context, Constants.KEY_IS_ELDER_SIGNED_IN, true)
                            DataStoreManager.saveData(context,Constants.KEY_ELDER_PHONE,phoneNumber)
                            DataStoreManager.saveData(context,Constants.KEY_ELDER_NAME,elder.elderName)

                            val fcm = FirestoreHelper.getCaretakerFcmToken(phoneNumber)
                            if (fcm!=null){
                                DataStoreManager.saveData(context,Constants.KEY_CARETAKER_FCM,fcm)
                            }else{
                                DataStoreManager.saveData(context,Constants.KEY_CARETAKER_FCM,"")
                            }
                            isLoading =false

                            withContext(Dispatchers.Main) {
                                navController.navigate("home") {
                                    popUpTo("login") { inclusive = true }
                                }
                            }
                        // Pass the Elder data
                        } else {
                            errorMessage = "User not found. Please register first."
                        }
                    } catch (e: Exception) {
                        errorMessage = "Login failed: ${e.message}"
                        Log.e("LoginScreen", "Login error", e)
                    } finally {
                        isLoading = false
                    }
                }
            },
            enabled = !isLoading && phoneNumber.length >= 10,
            modifier = Modifier.fillMaxWidth()
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    indicatorColor = Color.Blue,
                    strokeWidth = 2.dp
                )
            } else {
                Text("Login")
            }
        }
    }
}
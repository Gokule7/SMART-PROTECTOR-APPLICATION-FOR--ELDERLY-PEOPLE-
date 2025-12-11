package com.raju.spawearable.presentation.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavController
import androidx.wear.compose.material.CircularProgressIndicator
import com.raju.spawearable.presentation.utils.Constants
import com.raju.spawearable.presentation.utils.DataStoreManager

@Composable
fun AuthStateHandler(navController: NavController) {
    val context = LocalContext.current
    var isCheckingAuth by remember { mutableStateOf(true) }
    var isLoggedIn by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        isLoggedIn = DataStoreManager.getBooleanData(context, Constants.KEY_IS_ELDER_SIGNED_IN) ?: false
        isCheckingAuth = false
    }

    if (!isCheckingAuth) {
        if (isLoggedIn) {
            LaunchedEffect(Unit) {
                navController.navigate("home") {
                    popUpTo("auth") { inclusive = true }
                }
            }
        } else {
            LaunchedEffect(Unit) {
                navController.navigate("login") {
                    popUpTo("auth") { inclusive = true }
                }
            }
        }
    } else {
        // Show splash screen or loading indicator
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
    }
}
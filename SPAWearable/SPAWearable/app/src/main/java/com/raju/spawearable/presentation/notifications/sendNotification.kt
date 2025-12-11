package com.raju.spawearable.presentation.notifications

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.Location
import android.util.Log
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import com.raju.spawearable.presentation.utils.Constants
import com.raju.spawearable.presentation.utils.DataStoreManager
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.Locale

fun sendsNotification(context: Context, message: String) {
    val TAG = "Notification"
    Log.d(TAG, "func")
    val receiverToken = DataStoreManager.getData(context, Constants.KEY_CARETAKER_FCM)
    if (receiverToken != null && receiverToken.isNotEmpty()) {
        Log.d(TAG, receiverToken)
        val fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(context)
        if (
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            val cancellationTokenSource = CancellationTokenSource()
            fusedLocationProviderClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, cancellationTokenSource.token)
                .addOnSuccessListener { location: Location? ->
                if (location != null) {
                    Log.d(TAG, "getting address")
                    val geocoder = Geocoder(context, Locale.getDefault())
                    try {
                        val addresses =
                            geocoder.getFromLocation(location.latitude, location.longitude, 1)
                        if (addresses != null) {
                            val addressStr = String.format(
                                "Address : %s\nCity :%s\nCountry :%s",
                                addresses[0].getAddressLine(0),
                                addresses[0].locality,
                                addresses[0].countryName
                            )

                            try {
                                Log.d(TAG, "Inside try")
                                val notification = NotificationData(
                                    token = receiverToken,
                                    data = hashMapOf(
                                        Constants.KEY_NAME to DataStoreManager.getData(
                                            context,
                                            Constants.KEY_ELDER_NAME
                                        )!!,
                                        Constants.KEY_LAST_ADDRESS to addressStr,
                                        Constants.KEY_ALERT_MESSAGE to message,
                                        Constants.KEY_ELDER_PHONE to DataStoreManager.getData(
                                            context,
                                            Constants.KEY_ELDER_PHONE
                                        )!!
                                    )
                                )
                                Log.d("Notification Body JSON", notification.toString())
                                sendingNotification(notification, context)
                            } catch (e: Exception) {
                                Toast.makeText(context, "Error", Toast.LENGTH_SHORT).show();
                                Log.e("Notification", e.localizedMessage!!)
                            }
                        } else {
                            try {
                                Log.d(TAG, "Inside else try")
                                val notification = NotificationData(
                                    token = receiverToken,
                                    data = hashMapOf(
                                        Constants.KEY_NAME to DataStoreManager.getData(
                                            context,
                                            Constants.KEY_ELDER_NAME
                                        )!!,
                                        Constants.KEY_LAST_ADDRESS to "Not-Known",
                                        Constants.KEY_ALERT_MESSAGE to message,
                                        Constants.KEY_ELDER_PHONE to DataStoreManager.getData(
                                            context,
                                            Constants.KEY_ELDER_PHONE
                                        )!!
                                    )
                                )
                                Log.d("Notification Body JSON", notification.toString())
                                sendingNotification(notification, context)
                            } catch (e: Exception) {
                                Toast.makeText(context, "Error", Toast.LENGTH_SHORT).show();
                                Log.e("Notification", e.localizedMessage!!)
                            }
                        }
                    } catch (e: Exception) {
                        Toast.makeText(context, "Error", Toast.LENGTH_SHORT).show();
                        Log.e("Notification", e.localizedMessage!!)
                    }
                } else {
                    Log.d(TAG, "Location null")
                }
            }
                .addOnFailureListener { e ->
                    Toast.makeText(context, "Error", Toast.LENGTH_SHORT).show();
                    Log.e("Notification", e.localizedMessage!!)
                }
        } else {
            Log.d(TAG, "Not allowed")
        }
    } else {
        Log.d(TAG, "null")
    }
}


fun sendingNotification(notification: NotificationData, context: Context) {
    val TAG = "Notification"
    val token = "Bearer ${AccessToken.getAccessToken()}"
    Log.d(TAG, "Token from server :$token")
    NotificationApi.sendNotification()
        .notification(accessToken = token, message = Notification(message = notification)).enqueue(
        object : Callback<Notification> {
            override fun onResponse(call: Call<Notification>, response: Response<Notification>) {
                if (response.isSuccessful) {
                    Log.d(TAG, "Notification sent successfully")

                    response.errorBody()?.string()?.let {
                        val responseJson = JSONObject(it)
                        val results = responseJson.optJSONArray("results")

                        if (responseJson.optInt("failure", 0) == 1) {
                            val error = results?.optJSONObject(0)
                            Log.e(TAG, "Notification send failure: $error")
                            return
                        }
                    }

                    Toast.makeText(context, "Notification Sent", Toast.LENGTH_SHORT).show()
                } else {
                    Log.e(TAG, "Notification failed: ${response.errorBody()?.string()}")
                    Toast.makeText(context, "Notification Not Sent", Toast.LENGTH_SHORT).show()
                }
                Toast.makeText(context, "Done", Toast.LENGTH_LONG).show()
            }

            override fun onFailure(call: Call<Notification>, t: Throwable) {
                Toast.makeText(context, "Failed", Toast.LENGTH_SHORT).show()
                Log.d("Notification", "Send Notification Failed :${t.localizedMessage}")
            }

        }
    )
}

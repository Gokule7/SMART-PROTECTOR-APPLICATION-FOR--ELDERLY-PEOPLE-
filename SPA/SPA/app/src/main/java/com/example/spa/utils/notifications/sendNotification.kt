package com.example.spa.utils.notifications

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Geocoder
import android.util.Log
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.example.spa.utils.helpers.Constants
import com.example.spa.utils.helpers.PreferenceManager
import com.google.android.gms.location.LocationServices
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.Locale

fun sendNotification(context:Context,message:String){
    val TAG = "Notification"
    val preferenceManager = PreferenceManager(context)
    val receiverToken = preferenceManager.getString(Constants.KEY_CARETAKER_FCM)
    Log.d(TAG,receiverToken!!)
    if (receiverToken!=null){
        Log.d(TAG,"receiver not null")
        val fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(context)
        if (ContextCompat.checkSelfPermission(context,Manifest.permission.POST_NOTIFICATIONS)==PackageManager.PERMISSION_GRANTED){
            fusedLocationProviderClient.lastLocation
                .addOnSuccessListener {location->
                    if (location != null) {
                        Log.d("Address", "getting address")
                        val geocoder = Geocoder(context, Locale.getDefault())
                        try {
                            val addresses = geocoder.getFromLocation(location.latitude,location.longitude,1)
                            if (addresses!=null){
                                val addressStr = String.format(
                                    "Address : %s\nCity :%s\nCountry :%s",
                                    addresses[0].getAddressLine(0),addresses[0].locality,addresses[0].countryName
                                )

                                try {
                                    Log.d(TAG,"Inside try")
                                    val notification = NotificationData(
                                        token = receiverToken,
                                        data = hashMapOf(
                                            Constants.KEY_NAME to preferenceManager.getString(Constants.KEY_ELDER_NAME)!!,
                                            Constants.KEY_LAST_ADDRESS to addressStr,
                                            Constants.KEY_ALERT_MESSAGE to message,
                                            Constants.KEY_ELDER_PHONE to preferenceManager.getString(Constants.KEY_ELDER_PHONE)!!
                                        )
                                    )
                                    Log.d("Notification Body JSON",notification.toString())
                                    sendingNotification(notification,context)
                                }catch (e:Exception){
                                    Toast.makeText(context, "Error", Toast.LENGTH_SHORT).show();
                                    Log.e("Notification",e.localizedMessage!!)
                                }
                            }else{
                                try {
                                    Log.d(TAG,"Inside else try")
                                    val notification = NotificationData(
                                        token = receiverToken,
                                        data = hashMapOf(
                                            Constants.KEY_USER_ID to preferenceManager.getString(Constants.KEY_ELDER_ID)!!,
                                            Constants.KEY_NAME to preferenceManager.getString(Constants.KEY_ELDER_NAME)!!,
                                            Constants.KEY_FCM_TOKEN to preferenceManager.getString(Constants.KEY_FCM_TOKEN)!!,
                                            Constants.KEY_LAST_ADDRESS to "Not-Known",
                                            Constants.KEY_ALERT_MESSAGE to message,
                                            Constants.KEY_ELDER_PHONE to preferenceManager.getString(Constants.KEY_ELDER_PHONE)!!
                                        )
                                    )
                                    Log.d("Notification Body JSON",notification.toString())
                                    sendingNotification(notification,context)
                                }catch (e:Exception){
                                    Toast.makeText(context, "Error", Toast.LENGTH_SHORT).show();
                                    Log.e("Notification",e.localizedMessage!!)
                                }
                            }
                        }catch (e:Exception){
                            Toast.makeText(context, "Error", Toast.LENGTH_SHORT).show();
                            Log.e("Notification",e.localizedMessage!!)
                        }
                    }
                }
                .addOnFailureListener { e->
                    Toast.makeText(context, "Error", Toast.LENGTH_SHORT).show();
                    Log.e("Notification",e.localizedMessage!!)
                }
        }
    }
}



fun sendingNotification(notification: NotificationData,context: Context){
    val TAG = "Notification"
    val token = "Bearer ${AccessToken.getAccessToken()}"
    Log.d(TAG,"Token :$token")
    NotificationApi.sendNotification().notification(accessToken = token, message = Notification(message = notification)).enqueue(
        object :Callback<Notification>{
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
                Toast.makeText(context,"Done",Toast.LENGTH_LONG).show()
            }

            override fun onFailure(call: Call<Notification>, t: Throwable) {
                Toast.makeText(context, "Failed", Toast.LENGTH_SHORT).show()
                Log.d("Notification","Send Notification Failed :${t.localizedMessage}")
            }

        }
    )
}

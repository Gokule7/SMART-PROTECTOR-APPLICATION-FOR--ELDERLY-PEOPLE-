package com.raju.spawearable.presentation.notifications

import com.raju.spawearable.presentation.notifications.Notification
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.Headers
import retrofit2.http.POST

interface NotificationInterface {
    @POST("/v1/projects/smart-protector-app/messages:send")
    @Headers(
        "Content-Type: application/json",
        "Accept: application/json"
    )
    fun notification(
        @Body message : Notification,
        @Header("Authorization")accessToken : String
    ):Call<Notification>
}
package com.example.spa.utils.notifications

import android.content.Context
import android.util.Log
import com.example.spa.R
import com.google.auth.oauth2.GoogleCredentials
import java.io.ByteArrayInputStream
import java.io.IOException
import java.io.InputStream
import java.nio.charset.StandardCharsets

object AccessToken {

    private val firebaseMessagingScope = "https://www.googleapis.com/auth/firebase.messaging"
    fun getAccessToken():String?{
        try{
            val jsonString = "{\n" +
                    "  \"type\": \"service_account\",\n" +
                    "  \"project_id\": \"smart-protector-app\",\n" +
                    "  \"private_key_id\": \"458732ba46383901e965a9ebc89f0272df77dde7\",\n" +
                    "  \"private_key\": \"-----BEGIN PRIVATE KEY-----\\nMIIEvQIBADANBgkqhkiG9w0BAQEFAASCBKcwggSjAgEAAoIBAQDKLsu1dy3vxWmr\\nHB3pKYeNFN4RmwykEYFy97ZAwX6RzQayk9ekjzwZctxFKbUnax80bSACtFjWKFAi\\nHnMcBfG2qWFAl88T48prnu0Hh8CugJOfECePm6QLXA4NRHAccw2Y7Yj4EZQGletS\\nh3m6DFBOVAjiAi2KzG6JjIDpUxTF9DN1PoIw2YvVkQKp0uyFinT4qRd6danX/bXd\\n3RZyYumi1JmwwMYHno272YzN6xeLXBqOqKQzm28lacaWG8wD0vUUgXo8T8G6V5L4\\nqgEuJBGjzhuIe4bMefly5MM6qBA0eJXqf9OLAbYEDND8BYuGW69gquddNm6KYahz\\nHXyduYUJAgMBAAECggEAC/rzonI4UDzBBwz8lz45HGKkCYRH3pl0c5bqz2G/0Z0l\\n5FNHHAbKQ4JHO9httfMep10/gDWGTZNYMztdbAfMo7ub6gdM05OEATZKuo9gWx6+\\nkJZ1F6j78pne8cmQQJlPMRSDMRrei/jad7qUG5KC+C850wWYxACa3K7tDRZZlOSp\\nPRrB1/oQrhCYYKn70nwsPrY2SN5Pzz6v+N5m9MWqO2f7nh3zdYRFXRjaDDTJdGSy\\njJF40mLZIKHvdxrN5nVP9juxyaPdnAqUMPqyWS0e9mTIEbOOl4zUkNIzVhUvSPH9\\nCJ/R6UDzC85YidJdmCaEZ5oaykH0w0x3q3XKcS95AQKBgQD+tH+LN5/Vqz7oC84u\\nguNHauphH5MUjuOpqfNkWnuK5u/FtA/gy7aRe/5xBnOvfpNXmU9ew+FH7mHLQip/\\nAvDzxqNKeephAxCUV47uUf56iCXV8p9c6lpTVtgZFFX64Tv82astbY/eoBETooSG\\nhGaiScrv2sreNdH/4nYjdAD7AQKBgQDLNfBq/ThAPzdidqOl8VDc0sKLVejstf82\\nGcyvXu9e/jJ/yBSnuxqlZcm+t9h3TqewYCY2r/+JBRMZgmIkfDvSJoKpIfspMwAl\\nZVi43kZcYPdsAsV5rt/WQXFTUebnHQwY33b2jTEWDq3a0nxJdViuL80QBE2metSI\\nV5nMrSqyCQKBgH6aucs5mvlaZapyM8pqxQ561oXwa+uMGx36nEEFuOMSZ5yeJEZY\\n/CbUu2TzEA3rnNGuIwW32AMpOvMNsJHxpFOIKSKb9yTBoTwbSbCskRHXGvd94jkN\\nRFiokkHlGdfanTbR+4RZMWo3pkVcQwOaTGnexED1QGGz3u6SYqa8wsABAoGAJjKS\\naZoFm9YGNCT25/dvasLXPNe+O9iOC35YAlMHDcUcevaCI3FBNfvnK0llkC+hx76b\\nEEaO8xOYHoBqsE8F85+3idFQY9MpYN4nCkEF2tGDULNIqXHcs2qDwdSwYZsNLhO7\\nSecKZgoGVg6gvDIppDxqAGu+qN+H2cUVrOqUDvkCgYEAneZ+ScnwQq7tzScPGxGX\\nFICx8FkuV1RoJwFvA95kkGlUsbezx5me1FoSASbaP0LIdw5sOj+sv2ad2UQn5+ND\\nogZx1yd2Qhc29sGlBFNlyL/oKMVT2dT4euwD5xZcqBmpX5K4txwmGeisd3Q4dsSD\\n2UifPULzeAwcAoT0rDeCkpg=\\n-----END PRIVATE KEY-----\\n\",\n" +
                    "  \"client_email\": \"firebase-adminsdk-7yhy4@smart-protector-app.iam.gserviceaccount.com\",\n" +
                    "  \"client_id\": \"101837664923498918342\",\n" +
                    "  \"auth_uri\": \"https://accounts.google.com/o/oauth2/auth\",\n" +
                    "  \"token_uri\": \"https://oauth2.googleapis.com/token\",\n" +
                    "  \"auth_provider_x509_cert_url\": \"https://www.googleapis.com/oauth2/v1/certs\",\n" +
                    "  \"client_x509_cert_url\": \"https://www.googleapis.com/robot/v1/metadata/x509/firebase-adminsdk-7yhy4%40smart-protector-app.iam.gserviceaccount.com\",\n" +
                    "  \"universe_domain\": \"googleapis.com\"\n" +
                    "}\n"

            val stream = ByteArrayInputStream(jsonString.toByteArray(StandardCharsets.UTF_8))
            val googleCredential = GoogleCredentials.fromStream(stream)
                .createScoped(arrayListOf(firebaseMessagingScope))

            googleCredential.refresh()
            return googleCredential.accessToken.tokenValue
        }catch (e:Exception){
            return null
        }
    }
}
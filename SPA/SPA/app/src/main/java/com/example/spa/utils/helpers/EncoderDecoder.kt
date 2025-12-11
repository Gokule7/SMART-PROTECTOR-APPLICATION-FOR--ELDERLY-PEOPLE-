package com.example.spa.utils.helpers

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Base64
import android.util.Log
import android.widget.Toast
import com.example.spa.utils.user_models.Medication
import com.google.android.material.timepicker.MaterialTimePicker
import com.google.android.material.timepicker.TimeFormat
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.InputStream
import java.io.ObjectInputStream
import java.io.ObjectOutputStream
import java.util.Locale

object EncoderDecoder {
    fun getBitmapBytes(bitmap: Bitmap, quality: Int): ByteArray {
        val stream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, quality, stream)
        return stream.toByteArray()
    }

    fun getBitmap(uri: Uri,context: Context):Bitmap{
        var stream:InputStream? = null
        try{
            stream = context.contentResolver.openInputStream(uri)
        }catch (e:Exception){
            Toast.makeText(context,e.message.toString(),Toast.LENGTH_SHORT).show()
            Log.d("Error_Utils",e.message.toString())
        }
        return BitmapFactory.decodeStream(stream)
    }

    fun encodeImage(uri: Uri, context: Context): String {
        val bitmap = getBitmap(uri,context)
        val bArray = getBitmapBytes(bitmap,100)
        return Base64.encodeToString(bArray,Base64.DEFAULT)
    }

    fun decodeImage(img: String): Bitmap {
        val bArray = Base64.decode(img, Base64.DEFAULT)
        return BitmapFactory.decodeByteArray(bArray, 0, bArray.size)
    }

    // Time Picker to String
    fun timePickerToString(timePicker: MaterialTimePicker): String {
        // Get the selected hour and minute from the TimePicker
        val hour = timePicker.hour
        val minute = timePicker.minute

        // Format the hour and minute into a String
        return String.format(Locale.getDefault(), "%02d:%02d", hour, minute)
    }

    // String to Time Picker Object
    fun stringToTimePicker(time: String): MaterialTimePicker {
        // Split the time string into hour and minute components
        val timeComponents = time.split(":")
        val hour = timeComponents[0].toInt()
        val minute = timeComponents[1].toInt()

        // Create a MaterialTimePicker instance with the specified hour and minute
        return MaterialTimePicker.Builder()
            .setTimeFormat(TimeFormat.CLOCK_24H)
            .setHour(hour)
            .setMinute(minute)
            .build()
    }

    fun encodeMedicineList(medList : List<Medication>) : String{
        val byteArrayOutputStream = ByteArrayOutputStream()
        val oos = ObjectOutputStream(byteArrayOutputStream)
        oos.writeObject(medList)
        val res = String(Base64.encode(byteArrayOutputStream.toByteArray(), Base64.DEFAULT))
        oos.close()
        return res
    }

    fun decodeMedicineList(encode : String):List<Medication>{
        val bytes:ByteArray = Base64.decode(encode,Base64.DEFAULT)
        val bars = ByteArrayInputStream(bytes)
        val ois = ObjectInputStream(bars)
        val list:List<Medication> = ois.readObject() as List<Medication>
        ois.close()
        return list
    }
}
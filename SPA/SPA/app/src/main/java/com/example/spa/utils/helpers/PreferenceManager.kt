package com.example.spa.utils.helpers

import android.content.Context
import android.content.SharedPreferences

class PreferenceManager public constructor(context: Context) {
    private final var sharedPreferences: SharedPreferences =
        context.getSharedPreferences(Constants.KEY_PREFERENCE_NAME,Context.MODE_PRIVATE)

    fun putBoolean(key:String,value:Boolean){
        val editor = sharedPreferences.edit()
        editor.putBoolean(key, value)
        editor.apply()
    }

    fun getBoolean(key:String):Boolean{
        return sharedPreferences.getBoolean(key,false);
    }

    fun putString(key:String,value:String){
        val editor = sharedPreferences.edit()
        editor.putString(key, value)
        editor.apply()
    }

    fun putInt(key:String,value:Int){
        val editor = sharedPreferences.edit()
        editor.putInt(key, value)
        editor.apply()
    }


    fun getString(key:String):String?{
        return sharedPreferences.getString(key,null)
    }

    fun clear(){
        val editor = sharedPreferences.edit()
        editor.clear()
        editor.apply()
    }

    fun getInt(key:String):Int{
        return sharedPreferences.getInt(key,0)
    }

}
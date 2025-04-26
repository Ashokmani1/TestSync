package com.teksxt.closedtesting.data.local.converter

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class NotificationPreferencesConverter {
    private val gson = Gson()

    @TypeConverter
    fun fromNotificationPreferences(preferences: Map<String, Boolean>?): String? {
        return preferences?.let {
            gson.toJson(it)
        }
    }

    @TypeConverter
    fun toNotificationPreferences(preferencesString: String?): Map<String, Boolean>? {
        return preferencesString?.let {
            val type = object : TypeToken<Map<String, Boolean>>() {}.type
            gson.fromJson(it, type)
        }
    }
}
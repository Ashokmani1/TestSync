package com.teksxt.closedtesting.explore.data.local.converter

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.teksxt.closedtesting.explore.domain.model.Screenshot

class ScreenshotListConverter {
    private val gson = Gson()

    @TypeConverter
    fun fromScreenshotList(screenshots: List<Screenshot>?): String? {
        return screenshots?.let {
            gson.toJson(it)
        }
    }

    @TypeConverter
    fun toScreenshotList(screenshotsString: String?): List<Screenshot>? {
        return screenshotsString?.let {
            val type = object : TypeToken<List<Screenshot>>() {}.type
            gson.fromJson(it, type)
        }
    }
}
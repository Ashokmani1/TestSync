package com.teksxt.closedtesting.data.local.converter

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.teksxt.closedtesting.settings.domain.model.DeviceInfo


class DeviceListConverter {
    private val gson = Gson()

    @TypeConverter
    fun fromDeviceList(devices: List<DeviceInfo>?): String? {
        return devices?.let {
            gson.toJson(it)
        }
    }

    @TypeConverter
    fun toDeviceList(devicesString: String?): List<DeviceInfo>? {
        return devicesString?.let {
            val type = object : TypeToken<List<DeviceInfo>>() {}.type
            gson.fromJson(it, type)
        }
    }
}
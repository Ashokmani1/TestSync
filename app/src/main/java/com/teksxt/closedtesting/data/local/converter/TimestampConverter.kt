package com.teksxt.closedtesting.data.local.converter

import androidx.room.TypeConverter
import com.google.firebase.Timestamp
import java.util.Date

class TimestampConverter {
    @TypeConverter
    fun fromTimestamp(timestamp: Timestamp?): Long? {
        return timestamp?.seconds?.times(1000)?.plus(timestamp.nanoseconds / 1000000)
    }

    @TypeConverter
    fun toTimestamp(milliseconds: Long?): Timestamp? {
        return milliseconds?.let {
            val seconds = it / 1000
            val nanoseconds = ((it % 1000) * 1000000).toInt()
            Timestamp(seconds, nanoseconds)
        }
    }
}
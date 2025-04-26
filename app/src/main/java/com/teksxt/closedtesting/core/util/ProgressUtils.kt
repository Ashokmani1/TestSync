package com.teksxt.closedtesting.core.util

import com.teksxt.closedtesting.myrequest.domain.model.Request
import java.util.Calendar
import java.util.Date
import kotlin.math.max
import kotlin.math.min

/**
 * Utility class for calculating progress of testing requests
 */
object ProgressUtils {

    private const val TAG = "ProgressUtils"

    /**
     * Calculates the progress of a testing request with more precise day calculation
     */
    fun calculateProgress(request: Request?): Float
    {
        if (request == null) return 0f

        if (request.status.lowercase() == "completed") return 1f

        val createdAt = request.createdAt ?: return 0f

        val testingDays = request.testingDays.takeIf { it > 0 } ?: return 0f

        // Get current time in millis
        val currentTime = System.currentTimeMillis()

        // Create Date objects for more accurate day calculation
        val startDate = Date(createdAt)
        val currentDate = Date(currentTime)

        // Get timezone-adjusted day values (zeroing out hours/minutes)
        val calendar = Calendar.getInstance()

        // Set to start date and zero out time portion
        calendar.time = startDate
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        val startDay = calendar.timeInMillis

        // Set to current date and zero out time portion
        calendar.time = currentDate
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        val currentDay = calendar.timeInMillis

        // Calculate elapsed days (24-hour periods)
        val elapsedDays = (currentDay - startDay) / (24 * 60 * 60 * 1000)

        return min(1f, max(0f, elapsedDays.toFloat() / testingDays))
    }

    /**
     * Calculates the current testing day based on request creation time
     *
     * @param request The request to calculate current day for
     * @return Int representing the current day of testing (1-based)
     */
    fun calculateCurrentDay(request: Request?): Int
    {
        if (request == null) return 1
        if (request.status.lowercase() == "completed") return request.testingDays

        val createdAt = request.createdAt ?: return 1
        val testingDays = request.testingDays.takeIf { it > 0 } ?: return 1

        // Create calendar instances for proper timezone handling
        val createdCalendar = Calendar.getInstance()
        val currentCalendar = Calendar.getInstance()

        // Set to creation timestamp
        createdCalendar.timeInMillis = createdAt

        // Zero out time portions for accurate day calculation
        createdCalendar.set(Calendar.HOUR_OF_DAY, 0)
        createdCalendar.set(Calendar.MINUTE, 0)
        createdCalendar.set(Calendar.SECOND, 0)
        createdCalendar.set(Calendar.MILLISECOND, 0)

        currentCalendar.set(Calendar.HOUR_OF_DAY, 0)
        currentCalendar.set(Calendar.MINUTE, 0)
        currentCalendar.set(Calendar.SECOND, 0)
        currentCalendar.set(Calendar.MILLISECOND, 0)

        // Calculate elapsed days
        val startDay = createdCalendar.timeInMillis
        val endDay = currentCalendar.timeInMillis
        val elapsedDays = ((endDay - startDay) / (24 * 60 * 60 * 1000)).toInt()

        // Current day is elapsed days + 1 (since day 1 is the start day)
        val currentDay = elapsedDays + 1

        // Limit current day to the maximum testing days
        return min(currentDay, testingDays)
    }
}
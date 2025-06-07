package com.teksxt.closedtesting.util

import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import com.teksxt.closedtesting.TestSyncApp
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DeviceInfoProvider @Inject constructor(
    @ApplicationContext private val context: Context
) {

    fun getAppVersion(): String {
        return try {
            val packageInfo = context.packageManager.getPackageInfo(context.packageName, 0)
            "${packageInfo.versionName} (${packageInfo.versionCode})"
        } catch (e: PackageManager.NameNotFoundException) {
            "Unknown"
        }
    }

    fun getDeviceInfo(): Map<String, String> {
        return try {
            mapOf(
                "deviceModel" to "${Build.MANUFACTURER} ${Build.MODEL}",
                "osVersion" to "Android ${Build.VERSION.RELEASE}",
                "appVersion" to try {
                    DeviceInfoProvider(context).getAppVersion()
                } catch (e: Exception) {
                    "Unknown"
                }
            )
        } catch (e: Exception) {
            mapOf("error" to "Could not collect device info")
        }
    }
}
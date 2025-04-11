package com.teksxt.closedtesting.util

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import org.jsoup.Jsoup
import java.net.URL

object TestSyncUtil {
    /**
     * Fetches app icon URL from Play Store package name
     * @param packageName Package name of the app (e.g., "com.example.app")
     * @return URL of the app icon or null if not found
     */
    suspend fun fetchAppIconUrl(packageName: String): String? = withContext(Dispatchers.IO) {
        try {
            val url = "https://play.google.com/store/apps/details?id=$packageName"
            val doc = Jsoup.connect(url)
                .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64)")
                .timeout(10000)
                .get()

            // Get meta property og:image
            val metaImage = doc.select("meta[property=og:image]")
            val iconUrl = metaImage.firstOrNull()?.attr("content")
            iconUrl
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /**
     * Generates a unique app ID from package name or Play Store URL
     */
    fun generateAppId(playStoreLink: String): String {
        // Extract package name from Play Store link
        val packageNameRegex = "id=([^&]+)".toRegex()
        val match = packageNameRegex.find(playStoreLink)
        return match?.groupValues?.getOrNull(1) ?: playStoreLink.hashCode().toString()
    }
    
    /**
     * Validates a Play Store URL
     */
    fun isValidPlayStoreUrl(url: String): Boolean {
        return url.startsWith("https://play.google.com/store/apps/") && 
               url.contains("id=")
    }
}
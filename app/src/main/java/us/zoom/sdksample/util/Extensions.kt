package us.zoom.sdksample.util

import android.annotation.SuppressLint

/**
 * Extension for extracting [String] value from the [android.os.SystemProperties].
 */
@SuppressLint("PrivateApi")
fun String.Companion.getSystemProperty(key: String, default: String? = null): String? {
    try {
        val clazz = Class.forName("android.os.SystemProperties")
        val method = clazz.getDeclaredMethod("get", String::class.java)
        val value = method.invoke(null, key) as String
        return value.ifBlank { default }

    } catch (e: Exception) {
        e.printStackTrace()
    }

    return default
}

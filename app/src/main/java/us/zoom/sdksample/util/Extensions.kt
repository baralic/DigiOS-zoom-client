package us.zoom.sdksample.util

import android.annotation.SuppressLint
import android.app.Activity
import androidx.activity.ComponentActivity
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat

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

/**
 * Extension for extracting [Boolean] value from the [android.os.SystemProperties].
 */
@SuppressLint("PrivateApi")
fun Boolean.Companion.getSystemProperty(key: String, default: Boolean = false): Boolean {
    try {
        val clazz = Class.forName("android.os.SystemProperties")
        val method = clazz.getDeclaredMethod("getBoolean", String::class.java, Boolean::class.java)
        return method.invoke(null, key, default) as Boolean

    } catch (e: Exception) {
        e.printStackTrace()
    }

    return default
}

/**
 * Extension for extracting [Int] value from the [android.os.SystemProperties].
 */
@SuppressLint("PrivateApi")
fun Int.Companion.getSystemProperty(key: String, default: Int = 0): Int {
    try {
        val clazz = Class.forName("android.os.SystemProperties")
        val method = clazz.getDeclaredMethod("getBoolean", String::class.java, Boolean::class.java)
        return method.invoke(null, key, default) as Int

    } catch (e: Exception) {
        e.printStackTrace()
    }

    return default
}

fun Activity.hideSystemBars() {
    WindowCompat.getInsetsController(window, window.decorView).apply {
        systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        hide(WindowInsetsCompat.Type.systemBars())
    }
}
package us.zoom.sdksample.util

import android.app.Activity
import android.content.Context
import android.content.SharedPreferences
import android.content.SharedPreferences.Editor
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat


object Constants {

    interface Auth {
        companion object {
            const val WEB_DOMAIN: String = "zoom.us"
        }
    }

    interface Endpoint {
        companion object {
            const val JWT_ENDPOINT = "https://digios.azurewebsites.net"
        }
    }

    @JvmDefaultWithCompatibility
    interface SysProperty {
        companion object {
            const val PROPERTY_JWT_ENDPOINT = "debug.property.digilens.jwt.endpoint"
            const val PROPERTY_SHOW_SETTINGS = "debug.property.digilens.ui.settings"
        }

        fun getString(key: String, default: String): String? = String.getSystemProperty(key, default)
        fun getBoolean(key: String): Boolean = Boolean.getSystemProperty(key)
    }

    @JvmDefaultWithCompatibility
    interface Preferences {
        companion object {
            const val PREFS_NAME = "DigiOS_Zoom_Prefs"
            const val PREFS_IS_REMEMBERED = "Remembered"
            const val PREFS_MEETING_ID = "MeetingID"
            const val PREFS_USERNAME = "Username"
        }

        fun getPreferences(context: Context): SharedPreferences =
            context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

        fun getPreferenceEditor(preferences: SharedPreferences): Editor = preferences.edit()

        fun getMeetingId(preferences: SharedPreferences): String =
            preferences.getString(PREFS_MEETING_ID, "") ?: ""
        fun editMeetingId(editor: Editor, id: String): Preferences =
            editor.putString(PREFS_MEETING_ID, id).run { this@Preferences }

        fun getUsername(preferences: SharedPreferences): String =
            preferences.getString(PREFS_USERNAME, "") ?: ""
        fun editUsername(editor: Editor, username: String): Preferences =
            editor.putString(PREFS_USERNAME, username).run { this@Preferences }

        fun getRememberMe(preferences: SharedPreferences): Boolean =
            preferences.getBoolean(PREFS_IS_REMEMBERED, false)
        fun editRememberMe(editor: Editor, enabled: Boolean): Preferences =
            editor.putBoolean(PREFS_IS_REMEMBERED, enabled).run { this@Preferences }
    }

    fun hideSystemBars(activity: Activity) {
        WindowCompat.getInsetsController(activity.window, activity.window.decorView).apply {
            systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            hide(WindowInsetsCompat.Type.systemBars())
        }
    }
}

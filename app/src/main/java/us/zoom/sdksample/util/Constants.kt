package us.zoom.sdksample.util

object Constants {

    const val JWT_ENDPOINT = "https://3be5-94-189-232-137.ngrok-free.app"

    const val PROPERTY_JWT_ENDPOINT = "debug.property.digilens.jwt.endpoint"
    const val PROPERTY_SHOW_SETTINGS = "debug.property.digilens.ui.settings"

    fun getString(key: String, default: String): String? = String.getSystemProperty(key, default)
    fun getBoolean(key: String): Boolean = Boolean.getSystemProperty(key)
}
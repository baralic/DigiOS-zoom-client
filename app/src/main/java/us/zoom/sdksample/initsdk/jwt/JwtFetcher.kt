package us.zoom.sdksample.initsdk.jwt

import android.app.ProgressDialog
import android.content.Context
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONException
import org.json.JSONObject
import us.zoom.sdksample.util.Constants.JWT_ENDPOINT
import us.zoom.sdksample.util.Constants.PROPERTY_JWT_ENDPOINT
import us.zoom.sdksample.util.getSystemProperty
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL


class JwtFetcher(
    private val endpoint: String,
    private val callback: Callback,
    private val progress: Boolean = true,
) {

    constructor(callback: Callback) : this(
        endpoint = getJwtEndpoint(JWT_ENDPOINT),
        callback = callback,
    )

    interface Callback {
        fun onPreExecution()
        fun onPostExecution(signature: String)
        fun context(): Context
    }

    companion object {

        const val WEB_DOMAIN = "zoom.us"

        private fun getJwtEndpoint(default: String): String =
            String.getSystemProperty(PROPERTY_JWT_ENDPOINT, default)!!

        fun newInstance(callback: Callback) = JwtFetcher(callback = callback)
    }

    private var dialog: ProgressDialog? = null

    fun execute() {
        CoroutineScope(Dispatchers.Main).launch {
            onPreExecute()
            val result = doInBackground()
            onPostExecute(result)
        }
    }

    private fun getPayload(): String = "{}"

    private suspend fun onPreExecute() = withContext(Dispatchers.Main) {
        if (progress) {
            dialog = ProgressDialog.show(callback.context(), "Loading...", "")
        }
        callback.onPreExecution()
    }

    private suspend fun doInBackground(): String = withContext(Dispatchers.IO) {
        try {
            val url = URL(endpoint)
            val connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "POST"
            connection.doOutput = true
            connection.setRequestProperty("Content-Type", "application/json")
            connection.outputStream.use { os ->
                val input = getPayload().toByteArray(charset("utf-8"))
                os.write(input, 0, input.size)
            }
            connection.outputStream.close()

            val responseCode = connection.responseCode
            if (responseCode == HttpURLConnection.HTTP_OK) {
                BufferedReader(InputStreamReader(connection.inputStream)).use { isr ->
                    var inputLine: String?
                    val response = StringBuilder()
                    while (isr.readLine().also { inputLine = it } != null) {
                        response.append(inputLine)
                    }
                    isr.close()
                    response.toString()
                }
            } else {
                "Error: $responseCode"
            }
        } catch (e: Exception) {
            "Error: " + e.message
        }
    }

    private suspend fun onPostExecute(result: String) = withContext(Dispatchers.Main) {
        val jsonObject: JSONObject = try {
            JSONObject(result)
        } catch (e: JSONException) {
            Log.e("JWT", e.toString())
            JSONObject()
        }

        if (progress) {
            dialog?.dismiss()
            dialog = null
        }

        val signature = jsonObject.optString("signature")
        callback.onPostExecution(signature)
    }
}

package us.zoom.sdksample.ui

import android.os.Bundle
import android.util.Log
import com.unity3d.player.UnityPlayer
import com.unity3d.player.UnityPlayerActivity
import us.zoom.sdksample.inmeetingfunction.customizedmeetingui.MyMeetingActivity

class UnityWrapperActivity : UnityPlayerActivity() {

    private var message: String? = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        intent.extras?.let { message = it.getString("messageForUnity", MyMeetingActivity.START_TRAINING_COMMAND) }
        Log.d("UnityWrapperActivity", message!!)
        UnityPlayer.UnitySendMessage("DataExchanger", "ShowMessage", message)
    }

    override fun onResume() {
        super.onResume()
        intent.extras?.let { message = it.getString("messageForUnity", MyMeetingActivity.START_TRAINING_COMMAND) }
        Log.d("UnityWrapperActivity", message!!)
        UnityPlayer.UnitySendMessage("DataExchanger", "ShowMessage", message)
    }

    companion object {

        @JvmStatic
        fun setDataFromUnity(message: String) {
            Log.d("Unity", message)
        }

        @JvmStatic
        fun sendDataFromAndroid(startTrainingCommand: String) {
            UnityPlayer.UnitySendMessage("DataExchanger", "ShowMessage", startTrainingCommand)
            Log.d("UnityWrapperActivity", "sendDataFromAndroid called")
        }
    }
}
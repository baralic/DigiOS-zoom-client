package us.zoom.sdksample.otherfeatures

import android.app.Activity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import us.zoom.sdk.DialOutStatus
import us.zoom.sdk.DialOutStatusListener
import us.zoom.sdk.ZoomSDK
import us.zoom.sdksample.R

class MyCallMeByPhoneActivity : Activity(), View.OnClickListener, DialOutStatusListener {

    private lateinit var mBtnCall: Button
    private lateinit var mBtnHangup: Button
    private lateinit var mEdtPhoneNumber: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.call_me_activity)

        mEdtPhoneNumber = findViewById<View>(R.id.edtPhoneNumber) as EditText
        mBtnCall = findViewById<View>(R.id.btnCall) as Button
        mBtnCall.setOnClickListener(this)
        mBtnHangup = findViewById<View>(R.id.btnHangUp) as Button
        mBtnHangup.setOnClickListener(this)

        initButtons()

        val zoomSDK = ZoomSDK.getInstance()
        val meetingService = zoomSDK.meetingService
        meetingService?.addDialOutListener(this)
    }

    override fun onDialOutStatusChanged(status: Int) {
        Log.d(TAG, "onDialOutStatusChanged status = $status")
        if (status == DialOutStatus.DIALOUT_STATUS_JOIN_SUC) {
            finish()
        }
        updateButtons(status)
    }

    override fun onClick(arg0: View) {
        val zoomSDK = ZoomSDK.getInstance()
        val meetingService = zoomSDK.meetingService
        if (arg0.id == R.id.btnCall) {
            if (meetingService != null) {
                val number = mEdtPhoneNumber.getText().toString().trim { it <= ' ' }
                meetingService.dialOutUser(number, null, true)
            }
        } else if (arg0.id == R.id.btnHangUp) {
            meetingService.cancelDialOut(true)
        }
    }

    private fun initButtons() {
        val zoomSDK = ZoomSDK.getInstance()
        val meetingService = zoomSDK.meetingService
        if (meetingService != null && meetingService.isDialOutInProgress) {
            mBtnCall.setEnabled(false)
            mBtnHangup.setEnabled(true)
        } else {
            mBtnCall.setEnabled(true)
            mBtnHangup.setEnabled(false)
        }
    }

    private fun updateButtons(status: Int) {
        when (status) {
            DialOutStatus.DIALOUT_STATUS_UNKNOWN,
            DialOutStatus.DIALOUT_STATUS_ZOOM_CANCEL_CALL_FAIL,
            DialOutStatus.DIALOUT_STATUS_ZOOM_CALL_CANCELED,
            DialOutStatus.DIALOUT_STATUS_BUSY,
            DialOutStatus.DIALOUT_STATUS_NOT_AVAILABLE,
            DialOutStatus.DIALOUT_STATUS_USER_HANGUP,
            DialOutStatus.DIALOUT_STATUS_OTHER_FAIL,
            DialOutStatus.DIALOUT_STATUS_TIMEOUT -> {
                mBtnCall.setEnabled(true)
                mBtnHangup.setEnabled(false)
            }

            DialOutStatus.DIALOUT_STATUS_CALLING,
            DialOutStatus.DIALOUT_STATUS_RINGING,
            DialOutStatus.DIALOUT_STATUS_ACCEPTED,
            DialOutStatus.DIALOUT_STATUS_JOIN_SUC -> {
                mBtnCall.setEnabled(false)
                mBtnHangup.setEnabled(true)
            }

            DialOutStatus.DIALOUT_STATUS_ZOOM_START_CANCEL_CALL -> {
                mBtnCall.setEnabled(false)
                mBtnHangup.setEnabled(false)
            }
        }
    }

    override fun onDestroy() {
        val zoomSDK = ZoomSDK.getInstance()
        if (zoomSDK.isInitialized) {
            val meetingService = zoomSDK.meetingService
            meetingService.removeDialOutListener(this)
        }
        super.onDestroy()
    }

    companion object {
        private val TAG = MyCallMeByPhoneActivity::class.java.getSimpleName()
    }
}

package us.zoom.sdksample.ui

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import us.zoom.sdk.MeetingError
import us.zoom.sdk.MeetingParameter
import us.zoom.sdk.MeetingServiceListener
import us.zoom.sdk.MeetingStatus
import us.zoom.sdk.SimpleZoomUIDelegate
import us.zoom.sdk.ZoomSDK
import us.zoom.sdksample.R
import us.zoom.sdksample.inmeetingfunction.customizedmeetingui.MyMeetingActivity
import us.zoom.sdksample.inmeetingfunction.customizedmeetingui.view.MeetingWindowHelper
import us.zoom.sdksample.startjoinmeeting.apiuser.ApiUserStartMeetingHelper
import us.zoom.sdksample.util.Constants.Auth

class APIUserStartJoinMeetingActivity :
    Activity(), Auth, MeetingServiceListener, View.OnClickListener {

    private lateinit var mEdtMeetingNo: EditText
    private lateinit var mEdtMeetingPassword: EditText
    private lateinit var mEdtVanityId: EditText
    private lateinit var mProgressPanel: View
    private lateinit var mBtnStartMeeting: Button
    private lateinit var mBtnJoinMeeting: Button
    private lateinit var mBtnSettings: Button
    private lateinit var mEdtZak: EditText

    private lateinit var mReturnMeeting: Button
    private var mbPendingStartMeeting = false
    private var isResumed = false
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.api_user_start_join)
        mEdtMeetingNo = findViewById<View>(R.id.edtMeetingNo) as EditText
        mEdtVanityId = findViewById<View>(R.id.edtVanityUrl) as EditText
        mEdtMeetingPassword = findViewById<View>(R.id.edtMeetingPassword) as EditText
        mProgressPanel = findViewById(R.id.progressPanel)
        mBtnStartMeeting = findViewById<View>(R.id.btnStartMeeting) as Button
        mBtnStartMeeting.setOnClickListener(this)
        mBtnJoinMeeting = findViewById<View>(R.id.btnJoinMeeting) as Button
        mBtnJoinMeeting.setOnClickListener(this)
        mBtnSettings = findViewById(R.id.btn_settings)
        mReturnMeeting = findViewById(R.id.btn_return)
        mEdtZak = findViewById(R.id.editZak)
        registerListener()
    }

    private fun registerListener() {
        val meetingService = ZoomSDK.getInstance().meetingService
        meetingService?.addListener(this)
    }

    override fun onPause() {
        super.onPause()
        isResumed = false
    }

    override fun onResume() {
        super.onResume()
        isResumed = true
        refreshUI()
        ZoomSDK.getInstance().zoomUIService.setZoomUIDelegate(object : SimpleZoomUIDelegate() {
            override fun afterMeetingMinimized(activity: Activity) {
                val intent = Intent(activity, APIUserStartJoinMeetingActivity::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)
                activity.startActivity(intent)
            }
        })
    }

    override fun onDestroy() {
        val zoomSDK = ZoomSDK.getInstance()
        if (zoomSDK.isInitialized) {
            val meetingService = zoomSDK.meetingService
            meetingService.removeListener(this) //unregister meetingServiceListener
        }
        MeetingWindowHelper.getInstance().removeOverlayListener()
        super.onDestroy()
    }

    override fun onClick(v: View) {
        if (v.id == R.id.btnStartMeeting) {
            onClickBtnStartMeeting()
        } else if (v.id == R.id.btnJoinMeeting) {
            onClickBtnJoinMeeting()
        }
    }

    fun onClickReturnMeeting(view: View?) {
        UIUtil.returnToMeeting(this)
    }

    fun onClickSettings(view: View?) {
        val intent = Intent(this, MeetingSettingActivity::class.java)
        startActivity(intent)
    }

    fun onClickBtnJoinMeeting() {
        val meetingNo = mEdtMeetingNo.getText().toString().trim { it <= ' ' }
        val meetingPassword = mEdtMeetingPassword.getText().toString().trim { it <= ' ' }
        val vanityId = mEdtVanityId.getText().toString().trim { it <= ' ' }
        val zak = mEdtZak.getText().toString().trim { it <= ' ' }
        if (TextUtils.isEmpty(zak)) {
            Toast.makeText(this, "You need to enter zoom access token(zak)", Toast.LENGTH_LONG)
                .show()
            return
        }
        if (meetingNo.isEmpty() && vanityId.isEmpty()) {
            Toast.makeText(
                this,
                "You need to enter a meeting number/ vanity id which you want to join.",
                Toast.LENGTH_LONG
            ).show()
            return
        }
        if (meetingNo.isNotEmpty() && vanityId.isNotEmpty()) {
            Toast.makeText(
                this,
                "Both meeting number and vanity id have value,  just set one of them",
                Toast.LENGTH_LONG
            ).show()
            return
        }
        val zoomSDK = ZoomSDK.getInstance()
        if (!zoomSDK.isInitialized) {
            Toast.makeText(this, "ZoomSDK has not been initialized successfully", Toast.LENGTH_LONG)
                .show()
            return
        }
        val meetingService = zoomSDK.meetingService
        val ret = if (vanityId.isNotEmpty()) {
            ApiUserStartMeetingHelper.getInstance()
                .joinMeetingWithVanityId(this, vanityId, meetingPassword, zak)
        } else {
            ApiUserStartMeetingHelper.getInstance()
                .joinMeetingWithNumber(this, meetingNo, meetingPassword, zak)
        }
        Log.i(TAG, "onClickBtnJoinMeeting, ret=$ret")
    }

    fun onClickBtnStartMeeting() {
        val meetingNo = mEdtMeetingNo.getText().toString().trim { it <= ' ' }
        val vanityId = mEdtVanityId.getText().toString().trim { it <= ' ' }
        val zak = mEdtZak.getText().toString().trim { it <= ' ' }
        if (TextUtils.isEmpty(zak)) {
            Toast.makeText(this, "You need to enter zoom access token(zak)", Toast.LENGTH_LONG)
                .show()
            return
        }
        if (meetingNo.isNotEmpty() && vanityId.isNotEmpty()) {
            Toast.makeText(
                this,
                "Both meeting number and vanity  have value,  just set one of them",
                Toast.LENGTH_LONG
            ).show()
            return
        }
        val zoomSDK = ZoomSDK.getInstance()
        if (!zoomSDK.isInitialized) {
            Toast.makeText(this, "ZoomSDK has not been initialized successfully", Toast.LENGTH_LONG)
                .show()
            return
        }
        val meetingService = zoomSDK.meetingService
        if (meetingService.meetingStatus != MeetingStatus.MEETING_STATUS_IDLE) {
            val lMeetingNo: Long
            try {
                lMeetingNo = meetingNo.toLong()
            } catch (e: NumberFormatException) {
                Toast.makeText(this, "Invalid meeting number: $meetingNo", Toast.LENGTH_LONG).show()
                return
            }
            if (meetingService.currentRtcMeetingNumber == lMeetingNo) {
                meetingService.returnToMeeting(this)
                return
            }
            AlertDialog.Builder(this)
                .setMessage("Do you want to leave current meeting and start another?")
                .setPositiveButton("Yes") { _, _ ->
                    mbPendingStartMeeting = true
                    meetingService.leaveCurrentMeeting(false)
                }
                .setNegativeButton("No") { _, _ -> }
                .show()
            return
        }
        val ret = if (vanityId.isNotEmpty()) {
            ApiUserStartMeetingHelper.getInstance()
                .startMeetingWithVanityId(this, vanityId, zak)
        } else {
            ApiUserStartMeetingHelper.getInstance().startMeetingWithNumber(this, meetingNo, zak)
        }
        Log.i(TAG, "onClickBtnStartMeeting, ret=$ret")
    }

    override fun onMeetingParameterNotification(meetingParameter: MeetingParameter) {}
    override fun onMeetingStatusChanged(
        meetingStatus: MeetingStatus, errorCode: Int,
        internalErrorCode: Int
    ) {
        Log.i(
            TAG,
            ("onMeetingStatusChanged, meetingStatus=" + meetingStatus + ", errorCode=" + errorCode
                    + ", internalErrorCode=" + internalErrorCode)
        )
        if (meetingStatus == MeetingStatus.MEETING_STATUS_FAILED && errorCode == MeetingError.MEETING_ERROR_CLIENT_INCOMPATIBLE) {
            Toast.makeText(this, "Version of ZoomSDK is too low!", Toast.LENGTH_LONG).show()
        }
        if (mbPendingStartMeeting && meetingStatus == MeetingStatus.MEETING_STATUS_IDLE) {
            mbPendingStartMeeting = false
            onClickBtnStartMeeting()
        }
        if (meetingStatus == MeetingStatus.MEETING_STATUS_CONNECTING) {
            showMeetingUi()
        }
        refreshUI()
    }

    private fun refreshUI() {
        val meetingStatus = ZoomSDK.getInstance().meetingService.meetingStatus
        if ((meetingStatus == MeetingStatus.MEETING_STATUS_CONNECTING) || (meetingStatus == MeetingStatus.MEETING_STATUS_INMEETING
                    ) || (meetingStatus == MeetingStatus.MEETING_STATUS_RECONNECTING)
        ) {
            mBtnSettings.visibility = View.GONE
            mReturnMeeting.visibility = View.VISIBLE
        } else {
            mBtnSettings.visibility = View.VISIBLE
            mReturnMeeting.visibility = View.GONE
        }
        if (ZoomSDK.getInstance().meetingSettingsHelper.isCustomizedMeetingUIEnabled) {
            if (meetingStatus == MeetingStatus.MEETING_STATUS_INMEETING && isResumed) {
                MeetingWindowHelper.getInstance().showMeetingWindow(this)
            } else {
                MeetingWindowHelper.getInstance().hiddenMeetingWindow(true)
            }
        }
    }

    private fun showMeetingUi() {
        if (ZoomSDK.getInstance().meetingSettingsHelper.isCustomizedMeetingUIEnabled) {
            val intent = Intent(this, MyMeetingActivity::class.java)
            intent.putExtra("from", MyMeetingActivity.JOIN_FROM_APIUSER)
            intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)
            this.startActivity(intent)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent) {
        super.onActivityResult(requestCode, resultCode, data)
        MeetingWindowHelper.getInstance().onActivityResult(requestCode, this)
    }

    companion object {
        private val TAG = "ZoomSDKExample"
    }
}

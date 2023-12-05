package us.zoom.sdksample.ui

import android.annotation.SuppressLint
import android.app.Activity
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Color
import android.nfc.Tag
import android.opengl.Visibility
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.CheckBox
import android.widget.CompoundButton
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.Toast
import androidx.core.view.isVisible
import com.qualcomm.snapdragon.spaces.splashscreen.SplashScreenActivity
import com.unity3d.player.UnityPlayer
import com.unity3d.player.UnityPlayerActivity
import us.zoom.sdk.InMeetingNotificationHandle
import us.zoom.sdk.JoinMeetingOptions
import us.zoom.sdk.JoinMeetingParams
import us.zoom.sdk.MeetingParameter
import us.zoom.sdk.MeetingServiceListener
import us.zoom.sdk.MeetingStatus
import us.zoom.sdk.SimpleZoomUIDelegate
import us.zoom.sdk.ZoomApiError
import us.zoom.sdk.ZoomAuthenticationError
import us.zoom.sdk.ZoomError
import us.zoom.sdk.ZoomSDK
import us.zoom.sdksample.R
import us.zoom.sdksample.initsdk.InitAuthSDKCallback
import us.zoom.sdksample.initsdk.InitAuthSDKHelper
import us.zoom.sdksample.initsdk.jwt.JwtFetcher
import us.zoom.sdksample.inmeetingfunction.customizedmeetingui.MyMeetingActivity
import us.zoom.sdksample.inmeetingfunction.customizedmeetingui.RawDataMeetingActivity
import us.zoom.sdksample.inmeetingfunction.customizedmeetingui.view.MeetingWindowHelper
import us.zoom.sdksample.inmeetingfunction.zoommeetingui.CustomNewZoomUIActivity
import us.zoom.sdksample.inmeetingfunction.zoommeetingui.ZoomMeetingUISettingHelper
import us.zoom.sdksample.startjoinmeeting.UserLoginCallback
import us.zoom.sdksample.startjoinmeeting.UserLoginCallback.ZoomDemoAuthenticationListener
import us.zoom.sdksample.ui.InitAuthSDKActivity.Companion.TAG
import us.zoom.sdksample.ui.InitAuthSDKActivity.Companion.showMeetingTokenUI
import us.zoom.sdksample.util.Constants
import us.zoom.sdksample.util.Constants.SysProperty
import us.zoom.sdksample.util.Constants.hideSystemBars
import us.zoom.sdksample.util.KeyboardFocusChangeListener
import us.zoom.sdksample.util.getSystemProperty

class InitAuthSDKActivity : Activity(), View.OnClickListener, InitAuthSDKCallback,
    JwtFetcher.Callback, Constants.Preferences, SysProperty, MeetingServiceListener,
    CompoundButton.OnCheckedChangeListener, ZoomDemoAuthenticationListener {

    private var mZoomSDK: ZoomSDK = ZoomSDK.getInstance()
    private lateinit var mReturnMeeting: Button
    private lateinit var mRememberMe: CheckBox
    private lateinit var layoutJoin: View
    private lateinit var numberEdit: EditText
    private lateinit var nameEdit: EditText
    private lateinit var meetingTokenEdit: EditText
    private lateinit var mProgressPanel: View
    private var isResumed = false
    private lateinit var prefs: SharedPreferences
    private lateinit var mUnityPlayer: UnityPlayer

    private var isAutoJoin: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        hideSystemBars(this)

        if (mZoomSDK.isLoggedIn) {
            finish()
            showEmailLoginUserStartJoinActivity()
            return
        }

        mUnityPlayer = UnityPlayer(this)
        returnButton = Button(this)
        returnButton?.isActivated = false
        returnButton?.isVisible = false
        returnButton?.setOnClickListener {
            Log.d(TAG, "Button pressed")
            mUnityPlayer.pause()
            setContentView(R.layout.init_auth_sdk)
        }
        setContentView(R.layout.init_auth_sdk)
        prefs = getPreferences(this)
        prepareMeeting(intent, prefs)

        mProgressPanel = findViewById(R.id.progressPanel)
        mReturnMeeting = findViewById(R.id.btn_return)
        layoutJoin = findViewById(R.id.layout_join)
        mRememberMe = findViewById(R.id.check_remember)
        mRememberMe.setChecked(getRememberMe(prefs))
        mRememberMe.setOnCheckedChangeListener(this)
        numberEdit = findViewById(R.id.edit_join_number)
        numberEdit.setText(getMeetingId(prefs))
        numberEdit.onFocusChangeListener = focusChangeListener
        nameEdit = findViewById(R.id.edit_join_name)
        nameEdit.setText(getUsername(prefs))
        nameEdit.onFocusChangeListener = focusChangeListener
        meetingTokenEdit = findViewById(R.id.edit_join_meeting_token)
        meetingTokenEdit.onFocusChangeListener = focusChangeListener

        mProgressPanel.visibility = View.GONE
        if (showMeetingTokenUI) {
            meetingTokenEdit.visibility = View.VISIBLE
        }
        JwtFetcher(this).execute()
        showProgressPanel(true)
    }

    private fun prepareMeeting(intent: Intent, prefs: SharedPreferences) {
        isAutoJoin = intent.extras?.getBoolean(Constants.KEY_AUTO_JOIN) ?: false

        if (isAutoJoin) {
            val editor = prefs.edit()
            val id = String.getSystemProperty(SysProperty.PROPERTY_ZOOM_ID, "")!!
            val username = String.getSystemProperty(SysProperty.PROPERTY_ZOOM_USERNAME, "")!!
            val password = String.getSystemProperty(SysProperty.PROPERTY_ZOOM_PASSWORD, "")!!
            editUsername(editor, username)
            editMeetingId(editor, id)
            editRememberMe(editor, true)
            editor.apply()
            Log.d(TAG, "ID=$id, Username=$username, Password=$password")
        }
    }

    private val focusChangeListener = KeyboardFocusChangeListener(
        arrayOf(R.id.edit_join_number, R.id.edit_join_name, R.id.edit_join_meeting_token)
    )

    private var handle = InMeetingNotificationHandle { context: Context, _: Intent ->
        val intent = Intent(context, MyMeetingActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)
        if (context !is Activity) {
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        intent.setAction(InMeetingNotificationHandle.ACTION_RETURN_TO_CONF)
        context.startActivity(intent)
        true
    }

    override fun onPreExecution() {
        layoutJoin.visibility = View.GONE
        showSettings(false)
    }

    override fun onPostExecution(signature: String) {
        if (signature.isEmpty()) {
            Toast.makeText(context(), "Empty JWT", Toast.LENGTH_SHORT).show()
            return
        }
        InitAuthSDKHelper.SDK_JWT = signature
        InitAuthSDKHelper.getInstance().initSDK(this, this)
        if (mZoomSDK.isInitialized) {
            layoutJoin.visibility = View.VISIBLE
            layoutJoin.requestFocus()
            showSettings(true)
            mZoomSDK.meetingService.addListener(this)
            mZoomSDK.meetingSettingsHelper.enable720p(false)
        } else {
            layoutJoin.visibility = View.GONE
        }
    }

    override fun context(): Context {
        return this
    }

    override fun onBackPressed() {
        if (null == mZoomSDK.meetingService) {
            super.onBackPressed()
            return
        }
        val meetingStatus = mZoomSDK.meetingService.meetingStatus
        if (meetingStatus == MeetingStatus.MEETING_STATUS_INMEETING) {
            moveTaskToBack(true)
        } else {
            super.onBackPressed()
        }
    }

    override fun onZoomSDKInitializeResult(errorCode: Int, internalErrorCode: Int) {
        Log.i(TAG,"onZoomSDKInitializeResult, errorCode=$errorCode, internalErrorCode=$internalErrorCode")
        showProgressPanel(false)
        if (errorCode != ZoomError.ZOOM_ERROR_SUCCESS) {
            Toast.makeText(
                this,
                "Failed to initialize Zoom SDK. Error: $errorCode, internalErrorCode=$internalErrorCode",
                Toast.LENGTH_LONG
            ).show()
        } else {
//            Toast.makeText(this, "Initialize Zoom SDK successfully.", Toast.LENGTH_LONG).show()
//            mZoomSDK.getZoomUIService().enableMinimizeMeeting(true);
//            mZoomSDK.getZoomUIService().setMiniMeetingViewSize(new CustomizedMiniMeetingViewSize(0, 0, 360, 540));
//            setMiniWindows();
            mZoomSDK.meetingService.addListener(this)
            mZoomSDK.zoomUIService.setNewMeetingUI(CustomNewZoomUIActivity::class.java)
            mZoomSDK.zoomUIService.disablePIPMode(false)
            mZoomSDK.meetingSettingsHelper.isCustomizedMeetingUIEnabled = true
            mZoomSDK.meetingSettingsHelper.enable720p(false)
            mZoomSDK.meetingSettingsHelper.enableShowMyMeetingElapseTime(true)
            mZoomSDK.meetingSettingsHelper.setCustomizedNotificationData(null, handle)

            if (mZoomSDK.tryAutoLoginZoom() == ZoomApiError.ZOOM_API_ERROR_SUCCESS) {
                UserLoginCallback.getInstance().addListener(this)
                showProgressPanel(true)
            } else {
                showProgressPanel(false)
            }

            if (isAutoJoin) {
                onClickJoin(findViewById(R.id.btn_join_meeting))
            }
        }
    }

    override fun onClick(v: View) {
        if (!mZoomSDK.isInitialized) {
            Toast.makeText(this, "Init SDK First", Toast.LENGTH_SHORT).show()
            InitAuthSDKHelper.getInstance().initSDK(this, this)
        }
    }

    fun onClickSettings(view: View?) {
        if (!mZoomSDK.isInitialized) {
            Toast.makeText(this, "Init SDK First", Toast.LENGTH_SHORT).show()
            InitAuthSDKHelper.getInstance().initSDK(this, this)
            return
        }
        startActivity(Intent(this, MeetingSettingActivity::class.java))
    }

    override fun onZoomSDKLoginResult(result: Long) {
        if (result.toInt() == ZoomAuthenticationError.ZOOM_AUTH_ERROR_SUCCESS) {
            showEmailLoginUserStartJoinActivity()
            finish()
        } else {
            showProgressPanel(false)
        }
    }

    override fun onZoomSDKLogoutResult(result: Long) {}
    override fun onZoomIdentityExpired() {
        Log.e(TAG, "onZoomIdentityExpired")
        if (mZoomSDK.isLoggedIn) {
            mZoomSDK.logoutZoom()
        }
    }

    override fun onZoomAuthIdentityExpired() {
        Log.d(TAG, "onZoomAuthIdentityExpired")
        JwtFetcher(this).execute()
    }

    fun onClickJoin(view: View?) {
        if (!mZoomSDK.isInitialized) {
            Toast.makeText(this, "Init SDK First", Toast.LENGTH_SHORT).show()
            InitAuthSDKHelper.getInstance().initSDK(this, this)
            return
        }
        mZoomSDK.smsService.enableZoomAuthRealNameMeetingUIShown(
            !mZoomSDK.meetingSettingsHelper.isCustomizedMeetingUIEnabled
        )
        val number = numberEdit.getText().toString()
        val name = nameEdit.getText().toString()
        val zoomMeetingToken = meetingTokenEdit.getText().toString()
        val params = JoinMeetingParams()
        params.meetingNo = number
        params.displayName = name
        params.join_token = zoomMeetingToken
        val options = JoinMeetingOptions()
        mZoomSDK.meetingService.joinMeetingWithParams(
            this, params, ZoomMeetingUISettingHelper.getJoinMeetingOptions()
        )
        saveUserInput()
    }

    fun onClickUnity(view: View?) {
//        val intent = Intent(this, SplashScreenActivity::class.java)
//        intent.action = Intent.ACTION_MAIN
//        intent.addCategory("snapdragon.intent.category.SPACES")
//        startActivity(intent)
        mUnityPlayer.windowFocusChanged(true)
        mUnityPlayer.requestFocus()
        UnityPlayer.UnitySendMessage("DataExchanger", "ShowMessage", "ThisMessageWasPassedFromAndroid")
        mUnityPlayer.resume()
        setContentView(mUnityPlayer)
        mUnityPlayer.removeView(returnButton!!)
        mUnityPlayer.addView(returnButton!!,FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT)
    }

    private fun showProgressPanel(show: Boolean) {
        if (show) {
            showSettings(false)
            mProgressPanel.visibility = View.VISIBLE
            mReturnMeeting.visibility = View.GONE
            layoutJoin.visibility = View.GONE
        } else {
            showSettings(true)
            layoutJoin.visibility = View.VISIBLE
            mProgressPanel.visibility = View.GONE
            mReturnMeeting.visibility = View.GONE
        }
    }

    private fun showSSOLoginActivity() {
        val intent = Intent(this, SSOUserLoginActivity::class.java)
        startActivity(intent)
    }

    private fun showAPIUserActivity() {
        val intent = Intent(this, APIUserStartJoinMeetingActivity::class.java)
        startActivity(intent)
    }

    private fun showEmailLoginUserStartJoinActivity() {
        val intent = Intent(this, LoginUserStartJoinMeetingActivity::class.java)
        startActivity(intent)
    }

    fun onClickReturnMeeting(view: View?) {
        UIUtil.returnToMeeting(this)
    }

    override fun onResume() {
        super.onResume()
        isResumed = true
        refreshUI()
        setMiniWindows()
    }

    private fun setMiniWindows() {
        if (mZoomSDK.isInitialized && !mZoomSDK.meetingSettingsHelper.isCustomizedMeetingUIEnabled) {
            mZoomSDK.zoomUIService.setZoomUIDelegate(object : SimpleZoomUIDelegate() {
                override fun afterMeetingMinimized(activity: Activity) {
                    val intent = Intent(activity, InitAuthSDKActivity::class.java)
                    intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)
                    activity.startActivity(intent)
                }
            })
        }
    }

    override fun onPause() {
        super.onPause()
        isResumed = false
    }

    private fun refreshUI() {
        if (!mZoomSDK.isInitialized) {
            return
        }
        val meetingStatus = mZoomSDK.meetingService.meetingStatus
        if (mZoomSDK.meetingSettingsHelper.isCustomizedMeetingUIEnabled) {
            if (meetingStatus == MeetingStatus.MEETING_STATUS_INMEETING && isResumed) {
                MeetingWindowHelper.getInstance().showMeetingWindow(this)
                showProgressPanel(true)
                mProgressPanel.visibility = View.GONE
                mReturnMeeting.visibility = View.VISIBLE
            } else {
                MeetingWindowHelper.getInstance().hiddenMeetingWindow(true)
                showProgressPanel(false)
            }
        } else {
            if (meetingStatus == MeetingStatus.MEETING_STATUS_INMEETING && isResumed) {
                showProgressPanel(true)
                mProgressPanel.visibility = View.GONE
                mReturnMeeting.visibility = View.VISIBLE
            } else {
                showProgressPanel(false)
            }
        }
    }

    override fun onMeetingParameterNotification(meetingParameter: MeetingParameter) {
        Log.d(TAG, "onMeetingParameterNotification $meetingParameter")
    }

    override fun onMeetingStatusChanged(
        meetingStatus: MeetingStatus,
        errorCode: Int,
        internalErrorCode: Int
    ) {
        Log.d(TAG, "onMeetingStatusChanged $meetingStatus:$errorCode:$internalErrorCode")
        if (!mZoomSDK.isInitialized) {
            showProgressPanel(false)
            return
        }
        if (meetingStatus == MeetingStatus.MEETING_STATUS_CONNECTING) {
            if (ZoomMeetingUISettingHelper.useExternalVideoSource) {
                ZoomMeetingUISettingHelper.changeVideoSource(true, this@InitAuthSDKActivity)
            }
        }
        if (mZoomSDK.meetingSettingsHelper.isCustomizedMeetingUIEnabled) {
            if (meetingStatus == MeetingStatus.MEETING_STATUS_CONNECTING) {
                showMeetingUi()
            }
        }
        refreshUI()
    }

    private fun showMeetingUi() {
        if (mZoomSDK.meetingSettingsHelper.isCustomizedMeetingUIEnabled) {
            val sharedPreferences = getSharedPreferences("UI_Setting", MODE_PRIVATE)
            val enable = sharedPreferences.getBoolean("enable_rawdata", false)
            val intent: Intent
            if (!enable) {
                intent = Intent(this, MyMeetingActivity::class.java)
                intent.putExtra("from", MyMeetingActivity.JOIN_FROM_UNLOGIN)
            } else {
                intent = Intent(this, RawDataMeetingActivity::class.java)
            }
            intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)
            this.startActivity(intent)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent) {
        super.onActivityResult(requestCode, resultCode, data)
        MeetingWindowHelper.getInstance().onActivityResult(requestCode, this)
    }

    public override fun onDestroy() {
        super.onDestroy()
        UserLoginCallback.getInstance().removeListener(this)
        if (null != mZoomSDK.meetingService) {
            mZoomSDK.meetingService.removeListener(this)
        }
        if (null != mZoomSDK.meetingSettingsHelper) {
            mZoomSDK.meetingSettingsHelper.setCustomizedNotificationData(null, null)
        }
        InitAuthSDKHelper.getInstance().reset()
    }

    private fun showSettings(show: Boolean) {
        val visibility =
            if (getBoolean(SysProperty.PROPERTY_SHOW_SETTINGS)) if (show) View.VISIBLE else View.GONE else View.GONE
        val view = findViewById<View>(R.id.btnSettings)
        if (null != view) {
            view.visibility = visibility
        }
    }

    override fun onCheckedChanged(buttonView: CompoundButton, isChecked: Boolean) {
        val editor = getPreferenceEditor(prefs)
        editRememberMe(editor, isChecked)
        if (!isChecked) {
            editMeetingId(editor, "").editUsername(editor, "")
        }
        editor.apply()
    }

    private fun saveUserInput() {
        var meetingId = ""
        var username = ""
        if (mRememberMe.isChecked) {
            meetingId = numberEdit.getText().toString()
            username = nameEdit.getText().toString()
        }
        val editor = getPreferenceEditor(prefs)
        editMeetingId(editor, meetingId).editUsername(editor, username)
        editor.apply()
    }

    companion object {
        const val TAG = "DigiOS_Zoom"
        @SuppressLint("StaticFieldLeak")
        var returnButton: Button? = null

        @JvmStatic
        fun setDataFromUnity(string: String) {
            Log.d("Init", "Passed Data to android : $string")
            returnButton?.let {
                it.isActivated = true
                it.isVisible = true
                it.requestFocus()
            }
        }

        @JvmField
        var showMeetingTokenUI = false
    }
}
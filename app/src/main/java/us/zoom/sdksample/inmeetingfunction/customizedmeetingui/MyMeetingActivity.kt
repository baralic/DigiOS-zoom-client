package us.zoom.sdksample.inmeetingfunction.customizedmeetingui

import android.Manifest
import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.Dialog
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.PersistableBundle
import android.os.Process
import android.provider.Settings
import android.text.TextUtils
import android.util.Log
import android.view.GestureDetector
import android.view.GestureDetector.SimpleOnGestureListener
import android.view.Gravity
import android.view.KeyEvent
import android.view.MotionEvent
import android.view.View
import android.view.View.OnClickListener
import android.view.ViewGroup
import android.view.WindowManager
import android.view.inputmethod.BaseInputConnection
import android.widget.Button
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.PopupWindow
import android.widget.RelativeLayout
import android.widget.TextView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import us.zoom.sdk.BOControllerError
import us.zoom.sdk.IBOAdmin
import us.zoom.sdk.IBOAttendee
import us.zoom.sdk.IBOAttendeeEvent
import us.zoom.sdk.IBOAttendeeEvent.ATTENDEE_REQUEST_FOR_HELP_RESULT
import us.zoom.sdk.IBOData
import us.zoom.sdk.IBODataEvent
import us.zoom.sdk.IInterpretationLanguage
import us.zoom.sdk.ILiveTranscriptionMessageInfo
import us.zoom.sdk.IMeetingInterpretationControllerEvent
import us.zoom.sdk.IZoomRetrieveSMSVerificationCodeHandler
import us.zoom.sdk.IZoomSDKVideoRawDataDelegate
import us.zoom.sdk.IZoomSDKVideoRawDataDelegate.UserRawDataStatus
import us.zoom.sdk.IZoomVerifySMSVerificationCodeHandler
import us.zoom.sdk.InMeetingEventHandler
import us.zoom.sdk.InMeetingLiveTranscriptionController.InMeetingLiveTranscriptionLanguage
import us.zoom.sdk.InMeetingLiveTranscriptionController.InMeetingLiveTranscriptionListener
import us.zoom.sdk.InMeetingLiveTranscriptionController.MobileRTCLiveTranscriptionOperationType
import us.zoom.sdk.InMeetingLiveTranscriptionController.MobileRTCLiveTranscriptionStatus
import us.zoom.sdk.InMeetingService
import us.zoom.sdk.InMeetingUserInfo
import us.zoom.sdk.MeetingService
import us.zoom.sdk.MeetingStatus
import us.zoom.sdk.MobileRTCRenderInfo
import us.zoom.sdk.MobileRTCSDKError
import us.zoom.sdk.MobileRTCSMSVerificationError
import us.zoom.sdk.MobileRTCShareView
import us.zoom.sdk.MobileRTCVideoUnitRenderInfo
import us.zoom.sdk.MobileRTCVideoView
import us.zoom.sdk.MobileRTCVideoViewManager
import us.zoom.sdk.SDKEmojiReactionType
import us.zoom.sdk.ShareSettingType
import us.zoom.sdk.SmsListener
import us.zoom.sdk.SmsService
import us.zoom.sdk.ZoomSDK
import us.zoom.sdk.ZoomSDKCountryCode
import us.zoom.sdk.ZoomSDKRawDataType
import us.zoom.sdk.ZoomSDKRenderer
import us.zoom.sdk.ZoomSDKVideoRawData
import us.zoom.sdksample.R
import us.zoom.sdksample.inmeetingfunction.customizedmeetingui.audio.MeetingAudioCallback
import us.zoom.sdksample.inmeetingfunction.customizedmeetingui.audio.MeetingAudioCallback.AudioEvent
import us.zoom.sdksample.inmeetingfunction.customizedmeetingui.audio.MeetingAudioHelper
import us.zoom.sdksample.inmeetingfunction.customizedmeetingui.audio.MeetingAudioHelper.AudioCallBack
import us.zoom.sdksample.inmeetingfunction.customizedmeetingui.bo.BOEventCallback
import us.zoom.sdksample.inmeetingfunction.customizedmeetingui.bo.BOEventCallback.BOEvent
import us.zoom.sdksample.inmeetingfunction.customizedmeetingui.emoji.EmojiReactionCallback
import us.zoom.sdksample.inmeetingfunction.customizedmeetingui.emoji.EmojiReactionCallback.EmojiReactionEvent
import us.zoom.sdksample.inmeetingfunction.customizedmeetingui.other.MeetingCommonCallback
import us.zoom.sdksample.inmeetingfunction.customizedmeetingui.other.MeetingCommonCallback.CommonEvent
import us.zoom.sdksample.inmeetingfunction.customizedmeetingui.rawdata.RawDataRender
import us.zoom.sdksample.inmeetingfunction.customizedmeetingui.remotecontrol.MeetingRemoteControlHelper
import us.zoom.sdksample.inmeetingfunction.customizedmeetingui.share.MeetingShareCallback
import us.zoom.sdksample.inmeetingfunction.customizedmeetingui.share.MeetingShareCallback.ShareEvent
import us.zoom.sdksample.inmeetingfunction.customizedmeetingui.share.MeetingShareHelper
import us.zoom.sdksample.inmeetingfunction.customizedmeetingui.share.MeetingShareHelper.MeetingShareUICallBack
import us.zoom.sdksample.inmeetingfunction.customizedmeetingui.user.MeetingUserCallback
import us.zoom.sdksample.inmeetingfunction.customizedmeetingui.user.MeetingUserCallback.UserEvent
import us.zoom.sdksample.inmeetingfunction.customizedmeetingui.video.MeetingVideoCallback
import us.zoom.sdksample.inmeetingfunction.customizedmeetingui.video.MeetingVideoCallback.VideoEvent
import us.zoom.sdksample.inmeetingfunction.customizedmeetingui.video.MeetingVideoHelper
import us.zoom.sdksample.inmeetingfunction.customizedmeetingui.video.MeetingVideoHelper.VideoCallBack
import us.zoom.sdksample.inmeetingfunction.customizedmeetingui.view.MeetingOptionBar
import us.zoom.sdksample.inmeetingfunction.customizedmeetingui.view.MeetingOptionBar.MeetingOptionBarCallBack
import us.zoom.sdksample.inmeetingfunction.customizedmeetingui.view.MeetingWindowHelper
import us.zoom.sdksample.inmeetingfunction.customizedmeetingui.view.RealNameAuthDialog
import us.zoom.sdksample.inmeetingfunction.customizedmeetingui.view.VideoListLayout
import us.zoom.sdksample.inmeetingfunction.customizedmeetingui.view.adapter.AttenderVideoAdapter
import us.zoom.sdksample.inmeetingfunction.customizedmeetingui.view.adapter.AttenderVideoAdapter.EmojiParams
import us.zoom.sdksample.inmeetingfunction.customizedmeetingui.view.adapter.AttenderVideoAdapter.ItemClickListener
import us.zoom.sdksample.inmeetingfunction.customizedmeetingui.view.share.AnnotateToolbar
import us.zoom.sdksample.inmeetingfunction.customizedmeetingui.view.share.CustomShareView
import us.zoom.sdksample.inmeetingfunction.livetranscription.LiveTranscriptionRequestHandleDialog
import us.zoom.sdksample.ui.APIUserStartJoinMeetingActivity
import us.zoom.sdksample.ui.BreakoutRoomsAdminActivity
import us.zoom.sdksample.ui.InitAuthSDKActivity
import us.zoom.sdksample.ui.LoginUserStartJoinMeetingActivity
import us.zoom.sdksample.ui.UIUtil.getBoNameUserNameByUserId
import us.zoom.sdksample.util.Constants.Preferences
import us.zoom.sdksample.util.Constants.SysProperty
import us.zoom.sdksample.util.KeyboardFocusChangeListener
import us.zoom.sdksample.util.hideSystemBars

open class MyMeetingActivity : FragmentActivity(),
    View.OnClickListener,
    VideoEvent,
    AudioEvent,
    ShareEvent,
    UserEvent,
    CommonEvent,
    SmsListener,
    BOEvent,
    EmojiReactionEvent,
    SysProperty,
    Preferences {

    private lateinit var prefs: SharedPreferences

    private lateinit var waitingRoomView: ViewGroup
    private lateinit var waitingRoomMessage: TextView
    private lateinit var connectingLabel: TextView
    private lateinit var joinBreakoutButton: Button
    private lateinit var requestHelpButton: Button
    private lateinit var appsView: ImageView
    private lateinit var langLayout: View
    private lateinit var meetingVideoView: FrameLayout
    private lateinit var videoListLayout: VideoListLayout
    private lateinit var defaultVideoView: MobileRTCVideoView
    private lateinit var sharingView: MobileRTCShareView
    private lateinit var drawingView: AnnotateToolbar
    private lateinit var mNormalSenceView: View
    private lateinit var customShareView: CustomShareView
    private lateinit var localShareView: MobileRTCVideoView
    private lateinit var meetingOptionBar: MeetingOptionBar
    private lateinit var localShareRender: RawDataRender

    private lateinit var attenderInputConnection: BaseInputConnection
    private lateinit var attenderVideoAdapter: AttenderVideoAdapter
    private lateinit var gestureDetector: GestureDetector

    private val zoomSDK: ZoomSDK = ZoomSDK.getInstance()
    private var meetingService: MeetingService? = null
    private var defaultVideoViewManager: MobileRTCVideoViewManager? = null
    private lateinit var inMeetingService: InMeetingService // @Nullable
    private lateinit var smsService: SmsService // @Nullable

    private lateinit var meetingAudioHelper: MeetingAudioHelper
    private lateinit var meetingVideoHelper: MeetingVideoHelper
    private lateinit var meetingShareHelper: MeetingShareHelper
    private lateinit var remoteControlHelper: MeetingRemoteControlHelper
    private lateinit var screenInfoData: Intent

    private var from = 0
    private var currentLayoutType = -1
    private var mMeetingFailed = false
    private var builder: Dialog? = null

    private val focusChangeListener = KeyboardFocusChangeListener(
        arrayOf(R.id.edit_pwd, R.id.edit_name)
    )

    private val inMeetingServiceListener = object : SimpleInMeetingListener() {

        override fun onActiveSpeakerVideoUserChanged(userId: Long) {
            CoroutineScope(Dispatchers.Main).launch {
                videoListLayout.onSpeakerChanged(userId)
            }

            super.onActiveSpeakerVideoUserChanged(userId)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        hideSystemBars()
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        meetingService = zoomSDK.meetingService
        inMeetingService = zoomSDK.inMeetingService
        inMeetingService.addListener(inMeetingServiceListener)

        intent.extras?.let { from = it.getInt("from") }
        meetingAudioHelper = MeetingAudioHelper(audioCallBack)
        meetingVideoHelper = MeetingVideoHelper(this, videoCallBack)
        meetingShareHelper = MeetingShareHelper(this, shareCallBack)
        gestureDetector = GestureDetector(GestureDetectorListener())
        registerZoomListener()

        setContentView(R.layout.my_meeting_layout)
        prefs = getPreferences(this)

        waitingRoomView = findViewById(R.id.progressPanel)
        waitingRoomMessage = waitingRoomView.findViewById(R.id.progressMessage)
        connectingLabel = findViewById(R.id.connectingLabel)
        appsView = findViewById(R.id.appsView)
        joinBreakoutButton = findViewById(R.id.joinBreakoutButton)
        requestHelpButton = findViewById(R.id.requestHelpButton)
        meetingOptionBar = findViewById(R.id.meetingOptionBar)
        meetingVideoView = findViewById(R.id.meetingVideoView)
        sharingView = findViewById(R.id.sharingView)
        drawingView = findViewById(R.id.drawingView)
        localShareView = findViewById(R.id.localShareView)
        localShareRender = findViewById(R.id.localShareRender)

        appsView.setOnClickListener(this)
        joinBreakoutButton.setOnClickListener(this)
        requestHelpButton.setOnClickListener(this)
        meetingOptionBar.setCallBack(callBack)

        val inflater = layoutInflater
        mNormalSenceView = inflater.inflate(R.layout.layout_meeting_content_normal, null)
        defaultVideoView = mNormalSenceView.findViewById(R.id.videoView)
        customShareView = mNormalSenceView.findViewById(R.id.custom_share_view)
        remoteControlHelper = MeetingRemoteControlHelper(customShareView)
        meetingVideoView.addView(
            mNormalSenceView,
            FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
        )

        videoListLayout = findViewById(R.id.videoListLayout)
        attenderInputConnection = BaseInputConnection(videoListLayout, true)

        langLayout = findViewById(R.id.langLayout)
        videoListLayout.videoList.bringToFront()

        val layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        videoListLayout.videoList.setLayoutManager(layoutManager)
        attenderVideoAdapter = AttenderVideoAdapter(
            this, windowManager.defaultDisplay.width, pinVideoListener
        )
        videoListLayout.setAdapter(attenderVideoAdapter)

        refreshToolbar()
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent): Boolean {

        var result = meetingOptionBar.mapKeyDown(keyCode, event)
        result?.let {  return super.onKeyDown(it.keyCode, it) }

        result = videoListLayout.mapKeyDown(keyCode, event)
        result?.let {
            attenderInputConnection.sendKeyEvent(it)
            return true
        }

        meetingOptionBar.hideOrShowToolbar(false)

        return super.onKeyDown(keyCode, event)
    }

    override fun onKeyUp(keyCode: Int, event: KeyEvent): Boolean {

        var result = meetingOptionBar.mapKeyUp(keyCode, event)
        result?.let {  return super.onKeyUp(it.keyCode, it) }

        result = videoListLayout.mapKeyUp(keyCode, event)
        result?.let {
            attenderInputConnection.sendKeyEvent(it)
            return true
        }

        return super.onKeyDown(keyCode, event)
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
    }

    private val videoCallBack: VideoCallBack = object : VideoCallBack {
        override fun requestVideoPermission(): Boolean {
            return checkVideoPermission()
        }

        override fun showCameraList(popupWindow: PopupWindow) {
            popupWindow.showAsDropDown(meetingOptionBar.switchCameraView, 0, 20)
        }
    }
    private val audioCallBack: AudioCallBack = object : AudioCallBack {
        override fun requestAudioPermission(): Boolean {
            if (checkSelfPermission(Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(
                    this@MyMeetingActivity,
                    arrayOf(Manifest.permission.RECORD_AUDIO),
                    REQUEST_AUDIO_CODE
                )
                return false
            }
            return true
        }

        override fun updateAudioButton() {
            meetingOptionBar.updateAudioButton()
            meetingOptionBar.updateSpeakerButton()
        }
    }
    private val shareCallBack: MeetingShareUICallBack = object : MeetingShareUICallBack {
        override fun onMySharStart(start: Boolean) {
            defaultVideoView.post {
                if (!start) {
                    showLocalShareContent(false)
                } else {
                    if (meetingShareHelper.isSharingScreen) {
                        showLocalShareContent(true)
                    }
                }
            }
        }

        override fun showShareMenu(popupWindow: PopupWindow) {
            popupWindow.showAtLocation(
                meetingOptionBar.parent as View,
                Gravity.BOTTOM or Gravity.CENTER,
                0,
                150
            )
        }

        override fun getShareView(): MobileRTCShareView {
            return sharingView
        }

        override fun requestStoragePermission(): Boolean {
            val storagePermission =
                if (Build.VERSION.SDK_INT >= 33) Manifest.permission.READ_MEDIA_IMAGES else Manifest.permission.READ_EXTERNAL_STORAGE
            if (checkSelfPermission(storagePermission) != PackageManager.PERMISSION_GRANTED) {
                val storagePermissions = if (Build.VERSION.SDK_INT >= 33) arrayOf(
                    Manifest.permission.READ_MEDIA_IMAGES,
                    Manifest.permission.READ_MEDIA_VIDEO
                ) else arrayOf(
                    Manifest.permission.READ_EXTERNAL_STORAGE
                )
                ActivityCompat.requestPermissions(
                    this@MyMeetingActivity,
                    storagePermissions,
                    REQUEST_STORAGE_CODE
                )
                return false
            }
            return true
        }
    }

    private var currentPinUser: Long = 0
    private var pinVideoListener = ItemClickListener { view, position, userId ->
        if (currentLayoutType == LAYOUT_TYPE_VIEW_SHARE || currentLayoutType == LAYOUT_TYPE_SHARING_VIEW) {
            return@ItemClickListener
        }
        defaultVideoViewManager?.removeAllVideoUnits()
        val renderInfo = MobileRTCVideoUnitRenderInfo(0, 0, 100, 100)
        defaultVideoViewManager?.addAttendeeVideoUnit(userId, renderInfo)
        currentPinUser = userId
    }

    override fun onClick(v: View) {
        val id = v.id
        when (id) {
            R.id.joinBreakoutButton -> {
                val boController = inMeetingService.inMeetingBOController
                val boAttendee = boController.boAttendeeHelper
                boAttendee?.joinBo()
            }

            R.id.requestHelpButton -> attendeeRequestHelp()
            R.id.appsView -> showApps()
        }
    }

    private fun showApps() {
        val aanController = ZoomSDK.getInstance().inMeetingService.inMeetingAANController
        aanController.showAANPanel(this)
    }

    private fun checkVideoPermission(): Boolean {
        if (checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.CAMERA),
                REQUEST_CAMERA_CODE
            )
            return false
        }
        return true
    }

    override fun onEmojiReactionReceived(senderId: Long, type: SDKEmojiReactionType) {
        val emojiParams = EmojiParams(senderId, type)
        attenderVideoAdapter.setEmojiUser(emojiParams)
    }

    override fun onEmojiReactionReceivedInWebinar(type: SDKEmojiReactionType) {}
    internal inner class GestureDetectorListener : SimpleOnGestureListener() {
        override fun onSingleTapUp(e: MotionEvent): Boolean {
            if (drawingView.isAnnotationStarted || remoteControlHelper.isEnableRemoteControl) {
                meetingOptionBar.hideOrShowToolbar(true)
                return true
            }
            val orientation = resources.configuration.orientation
            if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
                if (videoListLayout.visibility == View.VISIBLE && (e.x >= videoListLayout.left || e.y <= meetingOptionBar.topBarHeight) || e.y >= meetingOptionBar.bottomBarTop) {
                    return true
                }
            } else {
                if (videoListLayout.visibility == View.VISIBLE && (e.y >= videoListLayout.top || e.y <= meetingOptionBar.topBarHeight) || e.y >= meetingOptionBar.bottomBarTop) {
                    return true
                }
            }
            if (meetingService?.meetingStatus == MeetingStatus.MEETING_STATUS_INMEETING) {
                meetingOptionBar.hideOrShowToolbar(meetingOptionBar.isShowing)
            }
            return true
        }
    }

    override fun dispatchTouchEvent(ev: MotionEvent): Boolean {
        gestureDetector.onTouchEvent(ev)
        return super.dispatchTouchEvent(ev)
    }

    private fun refreshToolbar() {
        if (meetingService?.meetingStatus == MeetingStatus.MEETING_STATUS_INMEETING) {
            connectingLabel.visibility = View.GONE
            meetingOptionBar.updateMeetingNumber(inMeetingService.currentMeetingNumber.toString() + "")
            meetingOptionBar.updateMeetingPassword(inMeetingService.meetingPassword)
            meetingOptionBar.refreshToolbar()

            // TODO: Not needed for demo
            //  appsView.setVisibility(View.VISIBLE)
        } else {
            if (meetingService?.meetingStatus == MeetingStatus.MEETING_STATUS_CONNECTING) {
                connectingLabel.visibility = View.VISIBLE
            } else {
                connectingLabel.visibility = View.GONE
            }
            meetingOptionBar.hideOrShowToolbar(true)
        }
    }

    private fun updateAnnotationBar() {
        if (mCurShareUserId > 0 && !isMySelfWebinarAttendee) {
            if (meetingShareHelper.isSenderSupportAnnotation(mCurShareUserId)) {
                if (inMeetingService.isMyself(mCurShareUserId) && !meetingShareHelper.isSharingScreen) {
                    if (meetingShareHelper.shareType == MeetingShareHelper.MENU_SHARE_SOURCE || meetingShareHelper.shareType == MeetingShareHelper.MENU_SHARE_SOURCE_WITH_AUDIO) {
                        drawingView.visibility = View.GONE
                    } else {
                        drawingView.visibility = View.VISIBLE
                    }
                } else {
                    if (currentLayoutType == LAYOUT_TYPE_VIEW_SHARE) {
                        drawingView.visibility = View.VISIBLE
                    } else {
                        drawingView.visibility = View.GONE
                    }
                }
            } else {
                drawingView.visibility = View.GONE
            }
        } else {
            drawingView.visibility = View.GONE
        }
    }

    private fun checkShowVideoLayout(forceRefresh: Boolean) {
        if (!checkVideoPermission()) {
            return
        }
        defaultVideoViewManager = defaultVideoView.videoViewManager
        val newLayoutType = newVideoMeetingLayout
        if (currentLayoutType != newLayoutType || forceRefresh) {
            removeOldLayout(currentLayoutType)
            currentLayoutType = newLayoutType
            addNewLayout(newLayoutType)
        }
        updateAnnotationBar()
    }

    private val newVideoMeetingLayout: Int
        get() {
            val newLayoutType: Int
            if (meetingService?.meetingStatus == MeetingStatus.MEETING_STATUS_WAITINGFORHOST) {
                newLayoutType = LAYOUT_TYPE_WAITHOST
                return newLayoutType
            }
            if (meetingService?.meetingStatus == MeetingStatus.MEETING_STATUS_IN_WAITING_ROOM) {
                newLayoutType = LAYOUT_TYPE_IN_WAIT_ROOM
                return newLayoutType
            }
            if (meetingShareHelper.isOtherSharing) {
                newLayoutType = LAYOUT_TYPE_VIEW_SHARE
            } else if (meetingShareHelper.isSharingOut && !meetingShareHelper.isSharingScreen) {
                newLayoutType = LAYOUT_TYPE_SHARING_VIEW
            } else {
                val userlist = inMeetingService.inMeetingUserList
                var userCount = 0
                if (userlist != null) {
                    userCount = userlist.size
                }
                if (userCount > 1) {
                    val preCount = userCount
                    for (i in 0 until preCount) {
                        val userInfo = inMeetingService.getUserInfoById(userlist[i])
                        if (inMeetingService.isWebinarMeeting) {
                            if (userInfo != null && userInfo.inMeetingUserRole == InMeetingUserInfo.InMeetingUserRole.USERROLE_ATTENDEE) {
                                userCount--
                            }
                        }
                    }
                }
                newLayoutType = when (userCount) {
                    0 -> LAYOUT_TYPE_PREVIEW
                    1 -> LAYOUT_TYPE_ONLY_MYSELF
                    else -> LAYOUT_TYPE_LIST_VIDEO
                }
            }
            return newLayoutType
        }

    private fun removeOldLayout(type: Int) {
        when (type) {
            LAYOUT_TYPE_WAITHOST -> {
                waitingRoomView.visibility = View.GONE
                meetingVideoView.visibility = View.VISIBLE
            }

            LAYOUT_TYPE_IN_WAIT_ROOM -> {
                waitingRoomView.visibility = View.GONE
                meetingVideoView.visibility = View.VISIBLE
            }

            LAYOUT_TYPE_PREVIEW, LAYOUT_TYPE_ONLY_MYSELF, LAYOUT_TYPE_ONETOONE -> {
                defaultVideoViewManager?.removeAllVideoUnits()
            }

            LAYOUT_TYPE_LIST_VIDEO, LAYOUT_TYPE_VIEW_SHARE -> {
                defaultVideoViewManager?.removeAllVideoUnits()
                defaultVideoView.setGestureDetectorEnabled(false)
            }

            LAYOUT_TYPE_SHARING_VIEW -> {
                sharingView.visibility = View.GONE
                showLocalShareContent(false)
                meetingVideoView.visibility = View.VISIBLE
            }
        }
        if (type != LAYOUT_TYPE_SHARING_VIEW) {
            customShareView.visibility = View.INVISIBLE
        }
    }

    private fun addNewLayout(type: Int) {
        when (type) {
            LAYOUT_TYPE_WAITHOST -> {
                waitingRoomView.visibility = View.VISIBLE
                waitingRoomMessage.setText(R.string.waiting_room_meeting_not_started)
                refreshToolbar()
                meetingVideoView.visibility = View.GONE
            }

            LAYOUT_TYPE_IN_WAIT_ROOM -> {
                waitingRoomView.visibility = View.VISIBLE
                waitingRoomMessage.setText(R.string.waiting_room_meeting_not_admitted)
                videoListLayout.visibility = View.GONE
                refreshToolbar()
                meetingVideoView.visibility = View.GONE
                drawingView.visibility = View.GONE
            }

            LAYOUT_TYPE_PREVIEW -> {
                showPreviewLayout()
            }

            LAYOUT_TYPE_ONLY_MYSELF -> {
                showOnlyMeLayout()
            }

            LAYOUT_TYPE_ONETOONE -> {
                showOne2OneLayout()
            }

            LAYOUT_TYPE_LIST_VIDEO -> {
                showVideoListLayout()
            }

            LAYOUT_TYPE_VIEW_SHARE -> {
                showViewShareLayout()
            }

            LAYOUT_TYPE_SHARING_VIEW -> {
                showSharingViewOutLayout()
            }
        }
    }

    private fun showPreviewLayout() {
        val renderInfo1 = MobileRTCVideoUnitRenderInfo(0, 0, 100, 100)
        defaultVideoView.visibility = View.VISIBLE
        defaultVideoViewManager?.addPreviewVideoUnit(renderInfo1)
        videoListLayout.visibility = View.GONE
    }

    private fun showOnlyMeLayout() {
        defaultVideoView.visibility = View.VISIBLE
        videoListLayout.visibility = View.GONE
        val renderInfo = MobileRTCVideoUnitRenderInfo(0, 0, 100, 100)
        val myUserInfo = inMeetingService?.myUserInfo
        if (myUserInfo != null) {
            defaultVideoViewManager?.removeAllVideoUnits()
            if (isMySelfWebinarAttendee) {
                if (mCurShareUserId > 0) {
                    defaultVideoViewManager?.addShareVideoUnit(mCurShareUserId, renderInfo)
                } else {
                    defaultVideoViewManager?.addActiveVideoUnit(renderInfo)
                }
            } else {
                defaultVideoViewManager?.addAttendeeVideoUnit(myUserInfo.userId, renderInfo)
            }
        }
    }

    private fun showOne2OneLayout() {
        defaultVideoView.visibility = View.VISIBLE
        videoListLayout.visibility = View.VISIBLE
        val renderInfo = MobileRTCVideoUnitRenderInfo(0, 0, 100, 100)
        //options.aspect_mode = MobileRTCVideoUnitAspectMode.VIDEO_ASPECT_PAN_AND_SCAN;
        defaultVideoViewManager?.addActiveVideoUnit(renderInfo)
        attenderVideoAdapter.setUserList(inMeetingService?.inMeetingUserList)
        attenderVideoAdapter.notifyDataSetChanged()
    }

    private fun showVideoListLayout() {
        val renderInfo = MobileRTCVideoUnitRenderInfo(0, 0, 100, 100)
        //options.aspect_mode = MobileRTCVideoUnitAspectMode.VIDEO_ASPECT_PAN_AND_SCAN;
        defaultVideoViewManager?.addActiveVideoUnit(renderInfo)
        videoListLayout.visibility = View.VISIBLE
        updateAttendeeVideos(inMeetingService.inMeetingUserList, 0)
    }

    private val sdkRenderer = ZoomSDKRenderer(object : IZoomSDKVideoRawDataDelegate {
        override fun onVideoRawDataFrame(rawData: ZoomSDKVideoRawData) {
            Log.d(TAG, "onVideoRawDataFrame:" + rawData.streamWidth + ":" + rawData.streamHeight)
        }

        override fun onUserRawDataStatusChanged(status: UserRawDataStatus) {}
    })

    fun showLocalShareContent(show: Boolean) {
        if (!ENABLE_SHOW_LOCAL_SHARE_CONTENT) {
            return
        }
        if (!show) {
            localShareRender.visibility = View.GONE
            localShareRender.unSubscribe()
            val videoViewManager = localShareView.videoViewManager
            videoViewManager.removeAllVideoUnits()
            localShareView.visibility = View.INVISIBLE
            sdkRenderer.unSubscribe()
        } else {
            if (inMeetingService.isMyself(mCurShareUserId)) {
                sdkRenderer.unSubscribe()
                val error =
                    sdkRenderer.subscribe(mCurShareUserId, ZoomSDKRawDataType.RAW_DATA_TYPE_SHARE)
                Log.d(TAG, "subscribe local share content :$error")
                localShareView.visibility = View.VISIBLE
                val videoViewManager = localShareView.videoViewManager
                videoViewManager.removeAllVideoUnits()
                val renderInfo = MobileRTCRenderInfo(0, 0, 100, 100)
                videoViewManager.addShareVideoUnit(mCurShareUserId, renderInfo)
                localShareRender.visibility = View.VISIBLE
                localShareRender.subscribe(
                    mCurShareUserId,
                    ZoomSDKRawDataType.RAW_DATA_TYPE_SHARE
                )
            }
        }
    }

    private fun showSharingViewOutLayout() {
        if (meetingShareHelper.shareType == MeetingShareHelper.MENU_SHARE_SOURCE || meetingShareHelper.shareType == MeetingShareHelper.MENU_SHARE_SOURCE_WITH_AUDIO) {
            return
        }
        attenderVideoAdapter.setUserList(null)
        attenderVideoAdapter.notifyDataSetChanged()
        videoListLayout.visibility = View.GONE
        meetingVideoView.visibility = View.GONE
        sharingView.visibility = View.VISIBLE
        showLocalShareContent(true)
    }

    private fun updateAttendeeVideos(userList: List<Long>, action: Int) {
        when (action) {
            0 -> {
                attenderVideoAdapter.setUserList(userList)
                attenderVideoAdapter.notifyDataSetChanged()
            }

            1 -> {
                attenderVideoAdapter.addUserList(userList)
            }

            else -> {
                val userId = attenderVideoAdapter.selectedUserId
                if (userList.contains(userId)) {
                    val inMeetingUserList = inMeetingService.inMeetingUserList
                    if (inMeetingUserList.size > 0) {
                        defaultVideoViewManager?.removeAllVideoUnits()
                        val renderInfo = MobileRTCVideoUnitRenderInfo(0, 0, 100, 100)
                        defaultVideoViewManager?.addAttendeeVideoUnit(inMeetingUserList[0], renderInfo)
                    }
                }
                attenderVideoAdapter.removeUserList(userList)
            }
        }
    }

    private fun showViewShareLayout() {
        if (!isMySelfWebinarAttendee) {
            defaultVideoView.visibility = View.VISIBLE
            defaultVideoView.setOnClickListener(null)
            defaultVideoView.setGestureDetectorEnabled(true)
            val shareUserId = inMeetingService.activeShareUserID()
            val renderInfo1 = MobileRTCRenderInfo(0, 0, 100, 100)
            defaultVideoViewManager?.addShareVideoUnit(shareUserId, renderInfo1)
            updateAttendeeVideos(inMeetingService.inMeetingUserList, 0)
            customShareView.setMobileRTCVideoView(defaultVideoView)
            remoteControlHelper.refreshRemoteControlStatus()
        } else {
            defaultVideoView.visibility = View.VISIBLE
            defaultVideoView.setOnClickListener(null)
            defaultVideoView.setGestureDetectorEnabled(true)
            val shareUserId = inMeetingService.activeShareUserID()
            val renderInfo1 = MobileRTCRenderInfo(0, 0, 100, 100)
            defaultVideoViewManager?.addShareVideoUnit(shareUserId, renderInfo1)
        }
        attenderVideoAdapter.setUserList(null)
        attenderVideoAdapter.notifyDataSetChanged()
        videoListLayout.visibility = View.INVISIBLE
    }

    private val isMySelfWebinarAttendee: Boolean
        get() {
            val myUserInfo = inMeetingService.myUserInfo
            return if (myUserInfo != null && inMeetingService.isWebinarMeeting) {
                myUserInfo.inMeetingUserRole == InMeetingUserInfo.InMeetingUserRole.USERROLE_ATTENDEE
            } else false
        }
    private val isMySelfWebinarHostCoHost: Boolean
        get() {
            val myUserInfo = inMeetingService.myUserInfo
            return if (myUserInfo != null && inMeetingService.isWebinarMeeting) {
                (myUserInfo.inMeetingUserRole == InMeetingUserInfo.InMeetingUserRole.USERROLE_HOST
                        || myUserInfo.inMeetingUserRole == InMeetingUserInfo.InMeetingUserRole.USERROLE_COHOST)
            } else false
        }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        meetingVideoHelper.checkVideoRotation(this)
        val display = (getSystemService(WINDOW_SERVICE) as WindowManager).defaultDisplay
        val displayRotation = display.rotation
        sharingView.onMyVideoRotationChangedForShareCamera(displayRotation)
        updateVideoListMargin(!meetingOptionBar.isShowing)
    }

    override fun onResume() {
        super.onResume()
        MeetingWindowHelper.getInstance().hiddenMeetingWindow(false)
        checkShowVideoLayout(false)
        meetingVideoHelper.checkVideoRotation(this)
        defaultVideoView.onResume()
    }

    override fun onPause() {
        super.onPause()
        defaultVideoView.onPause()
    }

    override fun onStop() {
        super.onStop()
        clearSubscribe()
    }

    private fun clearSubscribe() {
        defaultVideoViewManager?.removeAllVideoUnits()
        val userList = inMeetingService.inMeetingUserList
        userList?.let {
            attenderVideoAdapter.removeUserList(it)
        }
        currentLayoutType = -1
    }

    override fun onDestroy() {
        super.onDestroy()
        remoteControlHelper.onDestroy()
        unRegisterListener()
        attenderVideoAdapter.clear()
    }

    private var callBack: MeetingOptionBarCallBack = object : MeetingOptionBarCallBack {
        override fun onClickBack() {
            onClickMiniWindow()
        }

        override fun onClickSwitchCamera() {
            meetingVideoHelper.switchCamera()
        }

        override fun onClickLeave() {
            showLeaveMeetingDialog()
        }

        override fun onClickAudio() {
            meetingAudioHelper.switchAudio()
        }

        override fun onClickVideo() {
            meetingVideoHelper.switchVideo()
        }

        override fun onClickShare() {
            meetingShareHelper.onClickShare()
        }

        override fun onClickChats() {
            inMeetingService.showZoomChatUI(this@MyMeetingActivity, REQUEST_CHAT_CODE)
        }

        override fun onClickPlist() {
            inMeetingService.showZoomParticipantsUI(this@MyMeetingActivity, REQUEST_PLIST)
        }

        override fun onClickDisconnectAudio() {
            meetingAudioHelper.disconnectAudio()
        }

        override fun onClickSwitchLoudSpeaker() {
            meetingAudioHelper.switchLoudSpeaker()
            meetingOptionBar.updateSpeakerButton()
        }

        override fun onClickAdminBo() {
            val intent = Intent(this@MyMeetingActivity, BreakoutRoomsAdminActivity::class.java)
            startActivity(intent)
        }

        override fun onClickLowerAllHands() {
            if (inMeetingService.lowerAllHands(false) == MobileRTCSDKError.SDKERR_SUCCESS) Toast.makeText(
                this@MyMeetingActivity,
                "Lower all hands successfully",
                Toast.LENGTH_SHORT
            ).show()
        }

        override fun onClickReclaimHost() {
            if (inMeetingService.reclaimHost() == MobileRTCSDKError.SDKERR_SUCCESS) Toast.makeText(
                this@MyMeetingActivity,
                "Reclaim host successfully",
                Toast.LENGTH_SHORT
            ).show()
        }

        override fun showMoreMenu(popupWindow: PopupWindow?) {
            popupWindow?.showAtLocation(
                meetingOptionBar.parent as View,
                Gravity.BOTTOM or Gravity.RIGHT,
                0,
                150
            )
        }

        override fun onHidden(hidden: Boolean) {
            updateVideoListMargin(hidden)
        }
    }

    private fun onClickMiniWindow() {
        if (meetingService?.meetingStatus == MeetingStatus.MEETING_STATUS_INMEETING) {
            //stop share
            if (currentLayoutType == LAYOUT_TYPE_VIEW_SHARE) {
                defaultVideoViewManager?.removeShareVideoUnit()
                currentLayoutType = -1
            }
            val userList = ZoomSDK.getInstance().inMeetingService.inMeetingUserList
            if (null == userList || userList.size < 2) {
                showLeaveMeetingDialog()
                return
            }
            Log.d("ZOOM", "onClickMiniWindow:canDrawOverlays=" + Settings.canDrawOverlays(this))
            if (!Settings.canDrawOverlays(this)) {
                val intent = Intent(
                    Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    Uri.parse("package:" + this.packageName)
                )
                startActivityForResult(intent, REQUEST_SYSTEM_ALERT_WINDOW_FOR_MINIWINDOW)
            } else {
                showMainActivity()
            }
        } else {
            showLeaveMeetingDialog()
        }
    }

    override fun onBackPressed() = showLeaveMeetingDialog()

    private fun updateVideoListMargin(hidden: Boolean) {
        val params = videoListLayout.layoutParams as RelativeLayout.LayoutParams
        params.bottomMargin = if (hidden) 0 else meetingOptionBar.bottomBarHeight
        if (Configuration.ORIENTATION_LANDSCAPE == resources.configuration.orientation) {
            params.bottomMargin = 0
        }
        videoListLayout.setLayoutParams(params)
        videoListLayout.bringToFront()
    }

    private fun showMainActivity() {
        var clz: Class<*> = LoginUserStartJoinMeetingActivity::class.java
        if (from == JOIN_FROM_UNLOGIN) {
            clz = InitAuthSDKActivity::class.java
        } else if (from == JOIN_FROM_APIUSER) {
            clz = APIUserStartJoinMeetingActivity::class.java
        }
        val intent = Intent(this, clz)
        intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)
        startActivity(intent)
        clearSubscribe()
    }

    private fun showPasswordDialog(
        needPassword: Boolean,
        needDisplayName: Boolean,
        handler: InMeetingEventHandler
    ) {

        builder?.dismiss()

        val builder = Dialog(this, R.style.DigiOS_Dialog)
        this.builder = builder

        builder.setTitle("Need password or displayName")
        builder.setContentView(R.layout.layout_input_password_name)
        val pwd = builder.findViewById<EditText>(R.id.edit_pwd)
        val name = builder.findViewById<EditText>(R.id.edit_name)
        pwd.onFocusChangeListener = focusChangeListener
        name.onFocusChangeListener = focusChangeListener
        builder.findViewById<View>(R.id.layout_pwd).visibility =
            if (needPassword) View.VISIBLE else View.GONE
        builder.findViewById<View>(R.id.layout_name).visibility =
            if (needDisplayName) View.VISIBLE else View.GONE
        builder.findViewById<View>(R.id.btn_leave).setOnClickListener { _: View? ->
            builder.dismiss()
            handler.setMeetingNamePassword("", "", true)
        }
        builder.findViewById<View>(R.id.btn_ok).setOnClickListener { _: View? ->
            val password = pwd.getText().toString()
            val userName = name.getText().toString()
            if (needPassword && TextUtils.isEmpty(password) ||
                needDisplayName && TextUtils.isEmpty(userName)
            ) {
                builder.dismiss()
                onMeetingNeedPasswordOrDisplayName(needPassword, needDisplayName, handler)
                return@setOnClickListener
            }
            builder.dismiss()
            handler.setMeetingNamePassword(password, userName, false)
        }
        builder.setCancelable(false)
        builder.setCanceledOnTouchOutside(false)
        builder.show()
        pwd.requestFocus()
    }

    private fun updateVideoView(userList: List<Long>, action: Int) {
        if (currentLayoutType == LAYOUT_TYPE_LIST_VIDEO || currentLayoutType == LAYOUT_TYPE_VIEW_SHARE) {
            if (videoListLayout.videoList.visibility == View.VISIBLE) {
                updateAttendeeVideos(userList, action)
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            REQUEST_SHARE_SCREEN_PERMISSION -> {
                if (resultCode != RESULT_OK) {
                    Log.d(TAG, "onActivityResult REQUEST_SHARE_SCREEN_PERMISSION no ok ")
                } else {
                    data?.let { startShareScreen(it) }
                }
            }

            REQUEST_SYSTEM_ALERT_WINDOW -> meetingShareHelper.startShareScreenSession(
                screenInfoData
            )

            REQUEST_SYSTEM_ALERT_WINDOW_FOR_MINIWINDOW -> {
                if (resultCode == RESULT_OK) {
                    showMainActivity()
                } else {
                    showLeaveMeetingDialog()
                }
            }

            MeetingShareHelper.REQUEST_CODE_OPEN_FILE_EXPLORER -> meetingShareHelper.onActivityResult(
                requestCode,
                resultCode,
                data
            )
        }
    }

    private var finished = false
    override fun finish() {
        finished = true
        super.finish()
    }

    override fun onSaveInstanceState(outState: Bundle, outPersistentState: PersistableBundle) {
        super.onSaveInstanceState(outState, outPersistentState)
        if (!finished) {
            showMainActivity()
        }
    }

    private fun showLeaveMeetingDialog() {
        val isBreakOut = inMeetingService.inMeetingBOController.isInBOMeeting
        val isConnected = inMeetingService.isMeetingConnected
        val isHost = inMeetingService.isMeetingHost
        val isHostConnected = isConnected && isHost

        builder?.dismiss()

        val builder = Dialog(this, R.style.DigiOS_Dialog)
        this.builder = builder

        val onDialogClickListener = OnClickListener {
            when (it.id) {
                R.id.positive -> leave(isHostConnected)
                R.id.neutral -> leave(false)
                R.id.negative -> if (isBreakOut) leaveBo()
            }
            builder.dismiss()
        }

        builder.setContentView(R.layout.dialog_default)

        with(builder.findViewById<TextView>(R.id.title)) {
            setText(
                if (isHostConnected) R.string.dialog_exit_host
                else R.string.dialog_exit_attendee
            )
        }

        with(builder.findViewById<Button>(R.id.positive)) {
            setOnClickListener(onDialogClickListener)
            setText(
                if (isHostConnected) R.string.dialog_action_end
                else R.string.leave_meeting_label
            )
        }

        with(builder.findViewById<Button>(R.id.neutral)) {
            setOnClickListener(onDialogClickListener)
            visibility = if (isHostConnected) View.VISIBLE else View.GONE
            setTitle(R.string.leave_meeting_label)
        }

        with(builder.findViewById<Button>(R.id.negative)) {
            setOnClickListener(onDialogClickListener)
            setText(
                if (isBreakOut) R.string.dialog_action_leave_breakout
                else android.R.string.cancel
            )
        }

        builder.setCancelable(false)
        builder.setCanceledOnTouchOutside(false)
        builder.show()
    }

    private fun leave(end: Boolean) {
        if (meetingShareHelper.isSharingOut) {
            meetingShareHelper.stopShare()
        }
        finish()
        inMeetingService.leaveCurrentMeeting(end)
    }

    private fun leaveBo() {
        val boController = inMeetingService.inMeetingBOController
        val iboAssistant = boController.boAssistantHelper
        if (iboAssistant != null) {
            iboAssistant.leaveBO()
        } else {
            val boAttendee = boController.boAttendeeHelper
            boAttendee?.leaveBo() ?: leave(false)
        }
    }

    private fun showJoinFailDialog(error: Int) {
        val dialog = AlertDialog.Builder(this)
            .setCancelable(false)
            .setTitle("Meeting Fail")
            .setMessage("Error:$error")
            .setPositiveButton("Ok") { _, _ -> finish() }.create()
        dialog.show()
    }

    private fun showWebinarNeedRegisterDialog(inMeetingEventHandler: InMeetingEventHandler?) {
        val dialog = AlertDialog.Builder(this)
            .setCancelable(false)
            .setTitle("Need register to join this webinar meeting ")
            .setNegativeButton("Cancel") { _, _ ->
                inMeetingService.leaveCurrentMeeting(
                    true
                )
            }
            .setPositiveButton("Ok") { _, _ ->
                if (null != inMeetingEventHandler) {
                    val time = System.currentTimeMillis()
                    inMeetingEventHandler.setRegisterWebinarInfo("test", "$time@example.com", false)
                }
            }.create()
        dialog.show()
    }

    private fun showEndOtherMeetingDialog(handler: InMeetingEventHandler) {
        val dialog = AlertDialog.Builder(this)
            .setCancelable(false)
            .setTitle("Meeting Alert")
            .setMessage("You have a meeting that is currently in-progress. Please end it to start a new meeting.")
            .setPositiveButton("End Other Meeting") { _, _ -> handler.endOtherMeeting() }
            .setNeutralButton("Leave") { _, _ ->
                finish()
                inMeetingService.leaveCurrentMeeting(true)
            }.create()
        dialog.show()
    }

    @SuppressLint("NewApi")
    protected fun startShareScreen(data: Intent) {
        if (!Settings.canDrawOverlays(this)) {
            val intent = Intent(
                Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                Uri.parse("package:$packageName")
            )
            screenInfoData = data
            startActivityForResult(intent, REQUEST_SYSTEM_ALERT_WINDOW)
        } else {
            meetingShareHelper.startShareScreenSession(data)
        }
    }

    override fun checkSelfPermission(permission: String): Int {
        return if (permission.isEmpty()) {
            PackageManager.PERMISSION_DENIED
        } else try {
            checkPermission(permission, Process.myPid(), Process.myUid())
        } catch (e: Throwable) {
            PackageManager.PERMISSION_DENIED
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        for (i in permissions.indices) {
            if (Manifest.permission.RECORD_AUDIO == permissions[i]) {
                if (grantResults[i] == PackageManager.PERMISSION_GRANTED) {
                    meetingAudioHelper.switchAudio()
                }
            } else if (Manifest.permission.CAMERA == permissions[i]) {
                if (grantResults[i] == PackageManager.PERMISSION_GRANTED) {
                    checkShowVideoLayout(false)
                }
            } else if (Manifest.permission.READ_EXTERNAL_STORAGE == permissions[i]) {
                if (grantResults[i] == PackageManager.PERMISSION_GRANTED) {
                    meetingShareHelper.openFileExplorer()
                }
            }
        }
        if (requestCode == REQUEST_PHONE_STATUS_BLUETOOTH) {
            ZoomSDK.getInstance().inMeetingService.updatePermissions(permissions, grantResults)
        }
    }

    override fun onUserAudioStatusChanged(userId: Long) {
        meetingAudioHelper.onUserAudioStatusChanged(userId)
    }

    override fun onUserAudioTypeChanged(userId: Long) {
        meetingAudioHelper.onUserAudioTypeChanged(userId)
    }

    override fun onMyAudioSourceTypeChanged(type: Int) {
        meetingAudioHelper.onMyAudioSourceTypeChanged(type)
    }

    override fun onPermissionRequested(permissions: Array<String>) {
        for (permission in permissions) {
            Log.d(TAG, "onPermissionRequested:$permission")
        }
        ActivityCompat.requestPermissions(this, permissions, REQUEST_PHONE_STATUS_BLUETOOTH)
    }

    override fun onUserVideoStatusChanged(userId: Long) {
        meetingOptionBar.updateVideoButton()
        meetingOptionBar.updateSwitchCameraButton()
    }

    override fun onShareActiveUser(userId: Long) {
        meetingShareHelper.onShareActiveUser(mCurShareUserId, userId)
        mCurShareUserId = userId
        meetingOptionBar.updateShareButton()
        checkShowVideoLayout(true)
    }

    override fun onSilentModeChanged(inSilentMode: Boolean) {
        if (inSilentMode) meetingShareHelper.stopShare()
    }

    override fun onShareUserReceivingStatus(userId: Long) {}
    override fun onShareSettingTypeChanged(type: ShareSettingType) {}
    override fun onMeetingUserJoin(userList: List<Long>) {
        checkShowVideoLayout(false)
        updateVideoView(userList, 1)
    }

    override fun onMeetingUserLeave(userList: List<Long>) {
        var forceRefresh = false
        if (userList.contains(currentPinUser)) {
            forceRefresh = true
        }
        if (currentLayoutType == LAYOUT_TYPE_SHARING_VIEW || currentLayoutType == LAYOUT_TYPE_VIEW_SHARE) {
            if (!userList.contains(mCurShareUserId)) {
                forceRefresh = false
            }
        }
        checkShowVideoLayout(forceRefresh)
        updateVideoView(userList, 2)
    }

    override fun onWebinarNeedRegister(registerUrl: String) {}
    override fun onMeetingFail(errorCode: Int, internalErrorCode: Int) {
        mMeetingFailed = true
        meetingVideoView.visibility = View.GONE
        connectingLabel.visibility = View.GONE
        showJoinFailDialog(errorCode)
    }

    override fun onMeetingLeaveComplete(ret: Long) {
        meetingShareHelper.stopShare()
        if (!mMeetingFailed) finish()
    }

    override fun onMeetingStatusChanged(
        meetingStatus: MeetingStatus,
        errorCode: Int,
        internalErrorCode: Int
    ) {
        checkShowVideoLayout(true)
        refreshToolbar()
        if (meetingStatus == MeetingStatus.MEETING_STATUS_RECONNECTING) {
            meetingShareHelper.stopShare()
            if (!mMeetingFailed) finish()
        }
    }

    override fun onMeetingNeedPasswordOrDisplayName(
        needPassword: Boolean,
        needDisplayName: Boolean,
        handler: InMeetingEventHandler
    ) {
        val password = getString(SysProperty.PROPERTY_ZOOM_PASSWORD, "")
        if (password.isNullOrEmpty()) {
            showPasswordDialog(needPassword, needDisplayName, handler)
        } else {
            var username = getUsername(prefs)
            if (username.isEmpty()) {
                username = getString(SysProperty.PROPERTY_ZOOM_USERNAME, "Endava Demo")!!
            }
            handler.setMeetingNamePassword(password, username, false)
        }
    }

    override fun onMeetingNeedColseOtherMeeting(inMeetingEventHandler: InMeetingEventHandler) {
        showEndOtherMeetingDialog(inMeetingEventHandler)
    }

    override fun onJoinWebinarNeedUserNameAndEmail(inMeetingEventHandler: InMeetingEventHandler) {
        val time = System.currentTimeMillis()
        showWebinarNeedRegisterDialog(inMeetingEventHandler)
        //        inMeetingEventHandler.setRegisterWebinarInfo("test", time+"@example.com", false);
    }

    override fun onFreeMeetingReminder(
        isOrignalHost: Boolean,
        canUpgrade: Boolean,
        isFirstGift: Boolean
    ) {
        Log.d(TAG, "onFreeMeetingReminder:$isOrignalHost $canUpgrade $isFirstGift")
    }

    override fun onNeedRealNameAuthMeetingNotification(
        supportCountryList: List<ZoomSDKCountryCode>,
        privacyUrl: String,
        handler: IZoomRetrieveSMSVerificationCodeHandler
    ) {
        Log.d(TAG, "onNeedRealNameAuthMeetingNotification:$privacyUrl")
        Log.d(
            TAG,
            "onNeedRealNameAuthMeetingNotification getRealNameAuthPrivacyURL:" + ZoomSDK.getInstance().smsService.realNameAuthPrivacyURL
        )
        RealNameAuthDialog.show(this, handler)
    }

    override fun onRetrieveSMSVerificationCodeResultNotification(
        result: MobileRTCSMSVerificationError,
        handler: IZoomVerifySMSVerificationCodeHandler
    ) {
        Log.d(TAG, "onRetrieveSMSVerificationCodeResultNotification:$result")
    }

    override fun onVerifySMSVerificationCodeResultNotification(result: MobileRTCSMSVerificationError) {
        Log.d(TAG, "onVerifySMSVerificationCodeResultNotification:$result")
    }

    override fun onHelpRequestReceived(strUserID: String) {
        val boController = inMeetingService.inMeetingBOController
        val iboAdmin = boController.boAdminHelper
        if (iboAdmin != null) {
            val boAndUser = getBoNameUserNameByUserId(boController, strUserID)
            if (boAndUser.size != 2) return
            AlertDialog.Builder(this@MyMeetingActivity)
                .setMessage(boAndUser[1] + " in " + boAndUser[0] + " asked for help.")
                .setCancelable(false)
                .setNegativeButton("Later") { _, _ ->
                    iboAdmin.ignoreUserHelpRequest(
                        strUserID
                    )
                }
                .setPositiveButton("Join Breakout Room") { _, _ ->
                    iboAdmin.joinBOByUserRequest(
                        strUserID
                    )
                }
                .create().show()
        }
    }

    override fun onStartBOError(error: BOControllerError) {
        Log.d(TAG, "onStartBOError:$error")
    }

    override fun onBOEndTimerUpdated(remaining: Int, isTimesUpNotice: Boolean) {
        Log.d(TAG, "onBOEndTimerUpdated: remaining: $remaining,isTimesUpNotice: $isTimesUpNotice")
    }

    private fun unRegisterListener() {
        try {
            MeetingAudioCallback.getInstance().removeListener(this)
            MeetingVideoCallback.getInstance().removeListener(this)
            MeetingShareCallback.getInstance().removeListener(this)
            MeetingUserCallback.getInstance().removeListener(this)
            MeetingCommonCallback.getInstance().removeListener(this)
            BOEventCallback.getInstance().removeEvent(this)
            EmojiReactionCallback.getInstance().removeListener(this)
            smsService.removeListener(this)
            zoomSDK.inMeetingService.inMeetingBOController.removeListener(mBOControllerListener)
            zoomSDK.inMeetingService.inMeetingLiveTranscriptionController.removeListener(mLiveTranscriptionListener
            )
        } catch (e: Exception) {
            Log.w(TAG, e)
        }
    }

    private fun registerZoomListener() {
        smsService = zoomSDK.smsService
        smsService.addListener(this)
        MeetingAudioCallback.getInstance().addListener(this)
        MeetingVideoCallback.getInstance().addListener(this)
        MeetingShareCallback.getInstance().addListener(this)
        MeetingUserCallback.getInstance().addListener(this)
        MeetingCommonCallback.getInstance().addListener(this)
        EmojiReactionCallback.getInstance().addListener(this)
        inMeetingService.inMeetingBOController.addListener(mBOControllerListener)
        inMeetingService.inMeetingInterpretationController.setEvent(event)
        inMeetingService.inMeetingLiveTranscriptionController.addListener(mLiveTranscriptionListener)
    }

    private val mBOControllerListener: SimpleInMeetingBOControllerListener =
        object : SimpleInMeetingBOControllerListener() {
            var dialog: AlertDialog? = null
            override fun onHasAttendeeRightsNotification(iboAttendee: IBOAttendee) {
                super.onHasAttendeeRightsNotification(iboAttendee)
                Log.d(TAG, "onHasAttendeeRightsNotification")
                iboAttendee.setEvent(iboAttendeeEvent)
                val boController = inMeetingService.inMeetingBOController
                if (boController.isInBOMeeting) {
                    joinBreakoutButton.visibility = View.GONE
                    requestHelpButton.visibility =
                        if (iboAttendee.isHostInThisBO) View.GONE else View.VISIBLE
                    meetingOptionBar.updateMeetingNumber(iboAttendee.boName)
                } else {
                    requestHelpButton.visibility = View.GONE
                    val builder = AlertDialog.Builder(this@MyMeetingActivity)
                        .setMessage("The host is inviting you to join Breakout Room: " + iboAttendee.boName)
                        .setNegativeButton("Later") { _, _ ->
                            joinBreakoutButton.visibility = View.VISIBLE
                        }
                        .setPositiveButton("Join") { _, _ -> iboAttendee.joinBo() }
                        .setCancelable(false)
                    dialog = builder.create()
                    dialog?.show()
                }
            }

            override fun onHasDataHelperRightsNotification(iboData: IBOData) {
                Log.d(TAG, "onHasDataHelperRightsNotification")
                iboData.setEvent(iboDataEvent)
            }

            override fun onLostAttendeeRightsNotification() {
                super.onLostAttendeeRightsNotification()
                Log.d(TAG, "onLostAttendeeRightsNotification")
                dialog?.dismiss()
                joinBreakoutButton.visibility = View.GONE
            }

            override fun onHasAdminRightsNotification(iboAdmin: IBOAdmin) {
                super.onHasAdminRightsNotification(iboAdmin)
                Log.d(TAG, "onHasAdminRightsNotification")
                BOEventCallback.getInstance().addEvent(this@MyMeetingActivity)
            }

            override fun onBOSwitchRequestReceived(strNewBOName: String, strNewBOID: String) {
                super.onBOSwitchRequestReceived(strNewBOName, strNewBOID)
                Log.d(TAG, "onBOSwitchRequestReceived: boName: $strNewBOName, boID: $strNewBOID")
            }
        }
    private val iboDataEvent: IBODataEvent = object : IBODataEvent {
        override fun onBOInfoUpdated(strBOID: String) {
            val boController = inMeetingService.inMeetingBOController
            val iboData = boController.boDataHelper
            if (iboData != null) {
                val boName = iboData.currentBoName
                if (!TextUtils.isEmpty(boName)) {
                    meetingOptionBar.updateMeetingNumber(boName)
                }
            }
        }

        override fun onBOListInfoUpdated() {
            val boController = inMeetingService.inMeetingBOController
            val iboData = boController.boDataHelper
            if (iboData != null) {
                val boName = iboData.currentBoName
                if (!TextUtils.isEmpty(boName)) {
                    meetingOptionBar.updateMeetingNumber(boName)
                }
            }
        }

        override fun onUnAssignedUserUpdated() {}
    }
    private val iboAttendeeEvent: IBOAttendeeEvent = object : IBOAttendeeEvent {
        override fun onHelpRequestHandleResultReceived(eResult: ATTENDEE_REQUEST_FOR_HELP_RESULT) {
            if (eResult == ATTENDEE_REQUEST_FOR_HELP_RESULT.RESULT_IGNORE) {
                AlertDialog.Builder(this@MyMeetingActivity)
                    .setMessage("The host is currently helping others. Please try again later.")
                    .setCancelable(false)
                    .setPositiveButton("OK") { _, _ -> }.create().show()
            }
        }

        override fun onHostJoinedThisBOMeeting() {
            requestHelpButton.visibility = View.GONE
        }

        override fun onHostLeaveThisBOMeeting() {
            requestHelpButton.visibility = View.VISIBLE
        }
    }

    private fun attendeeRequestHelp() {
        val boController = inMeetingService.inMeetingBOController
        val boAttendee = boController.boAttendeeHelper
        if (boAttendee != null) {
            AlertDialog.Builder(this)
                .setMessage("You can invite the host to this Breakout Room for assistance.")
                .setCancelable(false)
                .setNegativeButton("Cancel") { _, _ -> }
                .setPositiveButton("Ask for Help") { _, _ -> boAttendee.requestForHelp() }
                .create().show()
        }
    }

    private val event: IMeetingInterpretationControllerEvent =
        object : IMeetingInterpretationControllerEvent {
            override fun onInterpretationStart() {
                Log.d(TAG, "onInterpretationStart:")
                updateLanguage()
            }

            override fun onInterpretationStop() {
                Log.d(TAG, "onInterpretationStop:")
                updateLanguage()
            }

            override fun onInterpreterListChanged() {
                Log.d(TAG, "onInterpreterListChanged:")
            }

            override fun onInterpreterRoleChanged(userID: Int, isInterpreter: Boolean) {
                Log.d(TAG, "onInterpreterRoleChanged:$userID:$isInterpreter")
                val isMyself = ZoomSDK.getInstance().inMeetingService.isMyself(userID.toLong())
                if (isMyself) {
                    if (isInterpreter) {
                        Toast.makeText(baseContext, R.string.zm_msg_interpreter, Toast.LENGTH_SHORT)
                            .show()
                    }
                    updateLanguage()
                }
            }

            private fun updateLanguage() {
                val controller = zoomSDK.inMeetingService.inMeetingInterpretationController
                // TODO: Not needed for demo
                //  if (controller.isInterpretationEnabled && controller.isInterpretationStarted && controller.isInterpreter) {
                //      layout_lans.visibility = View.VISIBLE
                //  } else {
                //      layout_lans.visibility = View.GONE
                //      return
                //  }
                val button1 = langLayout.findViewById<TextView>(R.id.btn_lan1)
                val button2 = langLayout.findViewById<TextView>(R.id.btn_lan2)
                val list = controller.interpreterLans
                val lanId = controller.interpreterActiveLan
                if (null != list && list.size >= 2) {
                    controller.getInterpretationLanguageByID(list[0])?.let { language1 ->
                        button1.text = language1.languageName
                    }
                    controller.getInterpretationLanguageByID(list[1])?.let { language2 ->
                        button2.text = language2.languageName
                    }
                    when (lanId) {
                        list[0] -> {
                            button1.setSelected(true)
                            button2.setSelected(false)
                        }

                        list[1] -> {
                            button2.setSelected(true)
                            button1.setSelected(false)
                        }

                        else -> {
                            button2.setSelected(false)
                            button1.setSelected(false)
                        }
                    }
                }
                button1.setOnClickListener {
                    val lans = controller.interpreterLans
                    if (null != lans && lans.size >= 2) {
                        controller.setInterpreterActiveLan(lans[0])
                    }
                    button2.setSelected(false)
                    button1.setSelected(true)
                }
                button2.setOnClickListener {
                    val lans = controller.interpreterLans
                    if (null != lans && lans.size >= 2) {
                        controller.setInterpreterActiveLan(lans[1])
                    }
                    button1.setSelected(false)
                    button2.setSelected(true)
                }
            }

            override fun onInterpreterActiveLanguageChanged(userID: Int, activeLanID: Int) {
                Log.d(TAG, "onInterpreterActiveLanguageChanged:$userID:$activeLanID")
                updateLanguage()
            }

            override fun onInterpreterLanguageChanged(lanID1: Int, lanID2: Int) {
                Log.d(TAG, "onInterpreterLanguageChanged:$lanID1:$lanID2")
                updateLanguage()
            }

            override fun onAvailableLanguageListUpdated(pAvailableLanguageList: List<IInterpretationLanguage>) {
                Log.d(TAG, "onAvailableLanguageListUpdated:$pAvailableLanguageList")
                updateLanguage()
            }

            override fun onInterpreterLanguagesUpdated(pInterpreterAvailableListenLanList: List<IInterpretationLanguage>) {
                val lanList = StringBuilder()
                for (language in pInterpreterAvailableListenLanList) {
                    lanList.append(language.languageID).append(", ")
                        .append(language.languageAbbreviations).append(", ")
                        .append(language.languageName).append("\n")
                }
                Log.d(TAG, "onInterpreterLanguagesUpdated:$lanList")
                updateLanguage()
            }
        }
    private val mLiveTranscriptionListener: InMeetingLiveTranscriptionListener =
        object : InMeetingLiveTranscriptionListener {
            override fun onLiveTranscriptionStatus(status: MobileRTCLiveTranscriptionStatus) {
                Log.d(TAG, "onLiveTranscriptionStatus: $status")
            }

            override fun onLiveTranscriptionMsgReceived(
                msg: String,
                speakerId: Long,
                type: MobileRTCLiveTranscriptionOperationType
            ) {
                Log.d(
                    TAG,
                    "onLiveTranscriptionMsgReceived: $msg, operation type: $type speakerId:$speakerId"
                )
            }

            override fun onLiveTranscriptionMsgReceived(messageInfo: ILiveTranscriptionMessageInfo) {
                Log.d(
                    TAG,
                    "onLiveTranscriptionMsgReceived messageInfo: " + messageInfo.messageContent + ", operation type: " +
                            messageInfo.messageOperationType + " speakerId:" + messageInfo.speakerID
                )
            }

            override fun onOriginalLanguageMsgReceived(messageInfo: ILiveTranscriptionMessageInfo) {
                Log.d(
                    TAG,
                    "onOriginalLanguageMsgReceived messageInfo: " + messageInfo.messageContent + ", operation type: " +
                            messageInfo.messageOperationType + " speakerId:" + messageInfo.speakerID
                )
            }

            override fun onRequestForLiveTranscriptReceived(
                requesterUserId: Long,
                bAnonymous: Boolean
            ) {
                Log.d(
                    TAG,
                    "onRequestForLiveTranscriptReceived from: $requesterUserId, bAnonymous: $bAnonymous"
                )
                var userName: String? = null
                if (!bAnonymous) {
                    val userInfo = inMeetingService.getUserInfoById(requesterUserId)
                    userName = userInfo.userName
                }
                LiveTranscriptionRequestHandleDialog.show(this@MyMeetingActivity, userName)
            }

            override fun onRequestLiveTranscriptionStatusChange(enabled: Boolean) {
                Log.d(TAG, "onRequestLiveTranscriptionStatusChange: $enabled")
            }

            override fun onLiveTranscriptionMsgError(
                speakLanguage: InMeetingLiveTranscriptionLanguage,
                transcriptLanguage: InMeetingLiveTranscriptionLanguage
            ) {
                Log.d(
                    TAG,
                    "onLiveTranscriptionMsgError speakLanguage: " + speakLanguage.lttLanguageName + ", transcriptLanguage: " + transcriptLanguage.lttLanguageName
                )
            }

            override fun onCaptionStatusChanged(enabled: Boolean) {
                Log.d(TAG, "onCaptionStatusChanged: $enabled")
            }
        }

    companion object {
        private val TAG = MyMeetingActivity::class.java.getSimpleName()
        const val REQUEST_CHAT_CODE = 1000
        const val REQUEST_PLIST = 1001
        const val REQUEST_CAMERA_CODE = 1010
        const val REQUEST_AUDIO_CODE = 1011
        const val REQUEST_STORAGE_CODE = 1012
        const val REQUEST_SHARE_SCREEN_PERMISSION = 1001
        protected const val REQUEST_SYSTEM_ALERT_WINDOW = 1002
        protected const val REQUEST_SYSTEM_ALERT_WINDOW_FOR_MINIWINDOW = 1003
        protected const val REQUEST_PHONE_STATUS_BLUETOOTH = 1004

        private const val LAYOUT_TYPE_PREVIEW = 0
        private const val LAYOUT_TYPE_WAITHOST = 1
        private const val LAYOUT_TYPE_IN_WAIT_ROOM = 2
        private const val LAYOUT_TYPE_ONLY_MYSELF = 3
        private const val LAYOUT_TYPE_ONETOONE = 4
        private const val LAYOUT_TYPE_LIST_VIDEO = 5
        private const val LAYOUT_TYPE_VIEW_SHARE = 6
        private const val LAYOUT_TYPE_SHARING_VIEW = 7

        @JvmField
        var mCurShareUserId: Long = -1
        const val JOIN_FROM_UNLOGIN = 1
        const val JOIN_FROM_APIUSER = 2
        const val JOIN_FROM_LOGIN = 3
        private const val ENABLE_SHOW_LOCAL_SHARE_CONTENT = false
    }
}

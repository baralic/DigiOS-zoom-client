package us.zoom.sdksample.inmeetingfunction.customizedmeetingui.view

import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView.OnItemClickListener
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.ListView
import android.widget.PopupWindow
import android.widget.TextView
import androidx.core.content.res.ResourcesCompat
import us.zoom.sdk.InMeetingAnnotationController
import us.zoom.sdk.InMeetingAudioController
import us.zoom.sdk.InMeetingChatController
import us.zoom.sdk.InMeetingInterpretationController
import us.zoom.sdk.InMeetingLiveTranscriptionController
import us.zoom.sdk.InMeetingService
import us.zoom.sdk.InMeetingShareController
import us.zoom.sdk.InMeetingUserInfo.InMeetingUserRole
import us.zoom.sdk.InMeetingVideoController
import us.zoom.sdk.InMeetingWebinarController
import us.zoom.sdk.ZoomSDK
import us.zoom.sdksample.BuildConfig
import us.zoom.sdksample.R
import us.zoom.sdksample.inmeetingfunction.customizedmeetingui.LegalNoticeDialogUtil
import us.zoom.sdksample.inmeetingfunction.customizedmeetingui.MyMeetingActivity
import us.zoom.sdksample.inmeetingfunction.customizedmeetingui.rawdata.VirtualVideoSource
import us.zoom.sdksample.inmeetingfunction.customizedmeetingui.rawdata.WaterMarkData
import us.zoom.sdksample.inmeetingfunction.customizedmeetingui.rawdata.YUVConvert
import us.zoom.sdksample.inmeetingfunction.customizedmeetingui.view.adapter.SimpleMenuAdapter
import us.zoom.sdksample.inmeetingfunction.customizedmeetingui.view.adapter.SimpleMenuItem
import us.zoom.sdksample.inmeetingfunction.livetranscription.RequestLiveTranscriptionDialog
import us.zoom.sdksample.ui.QAActivity

class MeetingOptionBar : FrameLayout, View.OnClickListener {

    private var callback: MeetingOptionBarCallBack? = null
    private var mContentView: View? = null
    private lateinit var bottomBar: View
    private lateinit var topBar: View
    private lateinit var leaveButton: View
    private lateinit var audioButton: View
    private lateinit var videoButton: View
    private lateinit var shareButton: View
    lateinit var switchCameraView: View
        private set

    private var mAudioStatusImg: ImageView? = null
    private var mVideoStatusImg: ImageView? = null
    private var mShareStatusImg: ImageView? = null
    private var mMeetingNumberText: TextView? = null
    private var mMeetingPasswordText: TextView? = null
    private var mMeetingAudioText: TextView? = null
    private var mMeetingVideoText: TextView? = null
    private var mMeetingShareText: TextView? = null

    private var inMeetingService: InMeetingService? = null
    private var chatController: InMeetingChatController? = null
    private var shareController: InMeetingShareController? = null
    private var videoController: InMeetingVideoController? = null
    private var audioController: InMeetingAudioController? = null
    private var webinarController: InMeetingWebinarController? = null
    private var annotationController: InMeetingAnnotationController? = null
    private var interpretationController: InMeetingInterpretationController? = null

    private var autoHidden = Runnable { hideOrShowToolbar(true) }

    private var virtualVideoSource: VirtualVideoSource? = null

    interface MeetingOptionBarCallBack {
        fun onClickBack()
        fun onClickSwitchCamera()
        fun onClickLeave()
        fun onClickAudio()
        fun onClickVideo()
        fun onClickShare()
        fun onClickChats()
        fun onClickPlist()
        fun onClickDisconnectAudio()
        fun onClickSwitchLoudSpeaker()
        fun onClickAdminBo()
        fun onClickLowerAllHands()
        fun onClickReclaimHost()
        fun showMoreMenu(popupWindow: PopupWindow?)
        fun onHidden(hidden: Boolean)
    }

    constructor(context: Context) : super(context) {
        initView(context)
    }

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        initView(context)
    }

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ) {
        initView(context)
    }

    fun setCallBack(callBack: MeetingOptionBarCallBack?) {
        callback = callBack
    }

    fun initView(context: Context) {
        mContentView = LayoutInflater.from(context)
            .inflate(R.layout.layout_meeting_option, this, false)
        addView(mContentView)
        inMeetingService = ZoomSDK.getInstance().inMeetingService
        shareController = inMeetingService?.inMeetingShareController
        videoController = inMeetingService?.inMeetingVideoController
        audioController = inMeetingService?.inMeetingAudioController
        webinarController = inMeetingService?.inMeetingWebinarController
        annotationController = inMeetingService?.inMeetingAnnotationController
        chatController = inMeetingService?.inMeetingChatController
        interpretationController = inMeetingService?.inMeetingInterpretationController

//        mContentView.setOnClickListener(this);
        bottomBar = findViewById(R.id.bottom_bar)
        topBar = findViewById(R.id.top_bar)
        leaveButton = findViewById(R.id.btnLeaveZoomMeeting)
        leaveButton.setOnClickListener(this)
        shareButton = findViewById(R.id.btnShare)
        shareButton.setOnClickListener(this)
        videoButton = findViewById(R.id.btnCamera)
        videoButton.setOnClickListener(this)
        audioButton = findViewById(R.id.btnAudio)
        audioButton.setOnClickListener(this)
        findViewById<View>(R.id.btnChats).setOnClickListener(this)
        mAudioStatusImg = findViewById(R.id.audioStatusImage)
        mVideoStatusImg = findViewById(R.id.videotatusImage)
        mShareStatusImg = findViewById(R.id.shareStatusImage)
        mMeetingAudioText = findViewById(R.id.text_audio)
        mMeetingVideoText = findViewById(R.id.text_video)
        mMeetingShareText = findViewById(R.id.text_share)
        findViewById<View>(R.id.moreActionImg).setOnClickListener(this)
        switchCameraView = findViewById(R.id.btnSwitchCamera)
        switchCameraView.setOnClickListener(this)
        mMeetingNumberText = findViewById(R.id.meetingNumber)
        mMeetingPasswordText = findViewById(R.id.txtPassword)
        findViewById<View>(R.id.btnBack).setOnClickListener(this)
    }

    fun hideOrShowToolbar(hidden: Boolean) {
        removeCallbacks(autoHidden)
        if (hidden) {
            visibility = INVISIBLE
        } else {
            postDelayed(autoHidden, 3000)
            visibility = VISIBLE
            bringToFront()
        }
        callback?.onHidden(hidden)
    }

    val bottomBarHeight: Int
        get() = bottomBar.measuredHeight
    val bottomBarBottom: Int
        get() = bottomBar.bottom
    val bottomBarTop: Int
        get() = bottomBar.top
    val topBarHeight: Int
        get() = topBar.measuredHeight

    fun updateMeetingNumber(text: String?) {
        mMeetingNumberText?.text = text
    }

    fun updateMeetingPassword(text: String?) = mMeetingPasswordText?.let {
        if (text?.isNotEmpty() == true) {
            mMeetingPasswordText?.visibility = VISIBLE
            mMeetingPasswordText?.text = text
        } else {
            mMeetingPasswordText?.visibility = GONE
        }
    }

    fun refreshToolbar() {
        updateAudioButton()
        updateVideoButton()
        updateShareButton()
        updateSwitchCameraButton()
    }

    fun updateAudioButton() {
        if (audioController?.isAudioConnected == true) {
            if (audioController?.isMyAudioMuted == true) {
                mAudioStatusImg?.setImageResource(R.drawable.icon_meeting_audio_mute)
            } else {
                mAudioStatusImg?.setImageResource(R.drawable.icon_meeting_audio)
            }
        } else {
            mAudioStatusImg?.setImageResource(R.drawable.icon_meeting_noaudio)
        }
    }

    private val isMySelfWebinarAttendee: Boolean
        get() {
            return inMeetingService?.myUserInfo?.let {
                if (inMeetingService?.isWebinarMeeting == true) {
                    it.inMeetingUserRole == InMeetingUserRole.USERROLE_ATTENDEE
                } else false
            } ?: false
        }

    fun updateShareButton() {
        if (isMySelfWebinarAttendee) {
            shareButton.visibility = GONE
        } else {
            shareButton.visibility = VISIBLE
            if (shareController?.isSharingOut == true) {
                mMeetingShareText?.text = "Stop share"
                mShareStatusImg?.setImageResource(R.drawable.icon_share_pause)
            } else {
                mMeetingShareText?.text = "Share"
                mShareStatusImg?.setImageResource(R.drawable.icon_share_resume)
            }
        }
    }

    fun updateVideoButton() {
        if (videoController?.isMyVideoMuted == true) {
            mVideoStatusImg?.setImageResource(R.drawable.icon_meeting_video_mute)
        } else {
            mVideoStatusImg?.setImageResource(R.drawable.icon_meeting_video)
        }
    }

    fun updateSwitchCameraButton() {
        if (videoController?.isMyVideoMuted == true) {
            switchCameraView.visibility = GONE
        } else {
            switchCameraView.visibility = VISIBLE
        }
    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.btnBack -> callback?.onClickBack()

            R.id.btnLeaveZoomMeeting -> callback?.onClickLeave()

            R.id.btnShare -> callback?.onClickShare()

            R.id.btnCamera -> callback?.onClickVideo()

            R.id.btnAudio -> callback?.onClickAudio()

            R.id.btnSwitchCamera -> callback?.onClickSwitchCamera()

            R.id.moreActionImg -> showMoreMenuPopupWindow()

            R.id.btnChats -> callback?.onClickChats()

            else -> visibility = INVISIBLE
        }
    }

    private val isMySelfWebinarHostCoHost: Boolean
        get() {
            return inMeetingService?.myUserInfo?.let {
                if (inMeetingService!!.isWebinarMeeting) {
                    it.inMeetingUserRole == InMeetingUserRole.USERROLE_HOST
                            || it.inMeetingUserRole == InMeetingUserRole.USERROLE_COHOST
                } else false
            } ?: false
        }

    private val isMySelfMeetingHostBoModerator: Boolean
        get() {
            return inMeetingService!!.myUserInfo?.let {
                if (inMeetingService?.inMeetingBOController?.isInBOMeeting == false
                    && inMeetingService?.inMeetingBOController?.boCreatorHelper != null
                ) {
                    val role = it.inMeetingUserRole
                    role == InMeetingUserRole.USERROLE_HOST ||
                            role == InMeetingUserRole.USERROLE_BREAKOUTROOM_MODERATOR
                } else false
            } ?: false
        }
    private val isShowLiveTranscriptionItem: Boolean
        get() {
            val isInBoMeeting = inMeetingService?.inMeetingBOController?.isInBOMeeting ?: false
            val inMeetingLiveTranscriptionController =
                inMeetingService?.inMeetingLiveTranscriptionController
            return !isInBoMeeting
                    && !isMySelfHost
                    && inMeetingService?.isWebinarMeeting == false
                    && inMeetingLiveTranscriptionController?.isLiveTranscriptionFeatureEnabled == true
                    && inMeetingLiveTranscriptionController.isRequestToStartLiveTranscriptionEnabled
                    && inMeetingLiveTranscriptionController.liveTranscriptionStatus != InMeetingLiveTranscriptionController.MobileRTCLiveTranscriptionStatus.MobileRTC_LiveTranscription_Status_Start
        }
    private val isShowStopTranscriptionItem: Boolean
        get() = isMySelfHost &&
                (inMeetingService?.inMeetingLiveTranscriptionController?.liveTranscriptionStatus
                        == InMeetingLiveTranscriptionController.MobileRTCLiveTranscriptionStatus.MobileRTC_LiveTranscription_Status_Start)
    private val isMySelfHost: Boolean
        get() {
            return inMeetingService?.myUserInfo?.let {
                it.inMeetingUserRole == InMeetingUserRole.USERROLE_HOST
            } ?: false
        }

    private val isMySelfHostCoHost: Boolean
        get() {
            return inMeetingService?.myUserInfo?.let {
                it.inMeetingUserRole == InMeetingUserRole.USERROLE_HOST
                        || it.inMeetingUserRole == InMeetingUserRole.USERROLE_COHOST
            } ?: false
        }

    private fun showMoreMenuPopupWindow() {
        val menuAdapter = SimpleMenuAdapter(context)
        if (audioController?.isAudioConnected == true) {
            menuAdapter.addItem(SimpleMenuItem(MENU_DISCONNECT_AUDIO, "Disconnect Audio"))
        }
        if (audioController?.canSwitchAudioOutput() == true) {
            if (audioController?.loudSpeakerStatus == true) {
                menuAdapter.addItem(SimpleMenuItem(MENU_SPEAKER_OFF, "Speak Off"))
            } else {
                menuAdapter.addItem(SimpleMenuItem(MENU_SPEAKER_ON, "Speak On"))
            }
        }
        if (!isMySelfWebinarAttendee) menuAdapter.addItem(
            SimpleMenuItem(MENU_SHOW_PLIST, "Paticipants")
        )
        if (annotationController?.canDisableViewerAnnotation() == true) {
            if (annotationController?.isViewerAnnotationDisabled == false) {
                menuAdapter.addItem(SimpleMenuItem(MENU_ANNOTATION_OFF, "Disable Annotation"))
            } else {
                menuAdapter.addItem(SimpleMenuItem(MENU_ANNOTATION_ON, "Enable Annotation"))
            }
        }
        if (isMySelfWebinarHostCoHost) {
            if (webinarController!!.isAllowPanellistStartVideo) {
                menuAdapter.addItem(
                    SimpleMenuItem(
                        MENU_DISALLOW_PANELIST_START_VIDEO,
                        "Disallow panelist start video"
                    )
                )
            } else {
                menuAdapter.addItem(
                    SimpleMenuItem(MENU_ALLOW_PANELIST_START_VIDEO, "Allow panelist start video")
                )
            }
            if (webinarController?.isAllowAttendeeChat == true) {
                menuAdapter.addItem(
                    SimpleMenuItem(MENU_DISALLOW_ATTENDEE_CHAT, "Disallow attendee chat")
                )
            } else {
                menuAdapter.addItem(
                    SimpleMenuItem(MENU_ALLOW_ATTENDEE_CHAT, "Allow attendee chat")
                )
            }
        }
        if (BuildConfig.DEBUG) {
            inMeetingService?.myUserInfo?.let {
                if (inMeetingService?.isWebinarMeeting == true) {
                    if (inMeetingService?.inMeetingQAController?.isQAEnabled == true) {
                        menuAdapter.addItem(SimpleMenuItem(MENU_ANNOTATION_QA, "QA"))
                    }
                }
            }
        }
        if (BuildConfig.DEBUG) {
            if (interpretationController?.isInterpretationEnabled == true
                && interpretationController?.isInterpreter == false
                && interpretationController?.isInterpretationStarted == true
            ) {
                menuAdapter.addItem(SimpleMenuItem(MENU_INTERPRETATION, "Language Interpretation"))
            }
        }

        if (BuildConfig.DEBUG) {
            if (interpretationController?.isInterpretationEnabled == true && isMySelfHost) {
                menuAdapter.addItem(SimpleMenuItem(MENU_INTERPRETATION_ADMIN, "Interpretation"))
            }
        }
        if (isMySelfMeetingHostBoModerator) {
            if (inMeetingService?.inMeetingBOController?.isBOEnabled == true) {
                menuAdapter.addItem(SimpleMenuItem(MENU_CREATE_BO, "Breakout Rooms"))
            }
        }

        // Add request live transcription button for non-host , support co-host
        if (isShowLiveTranscriptionItem) {
            menuAdapter.addItem(
                SimpleMenuItem(MENU_LIVE_TRANSCRIPTION_REQUEST, "Request Live Transcription")
            )
        }

        if (isShowStopTranscriptionItem) {
            menuAdapter.addItem(
                SimpleMenuItem(MENU_LIVE_TRANSCRIPTION_STOP, "STOP Live Transcription")
            )
        }

        if (isMySelfHostCoHost) {
            menuAdapter.addItem(SimpleMenuItem(MENU_LOWER_ALL_HANDS, "Lower All Hands"))
        }

        if (inMeetingService?.canReclaimHost() == true) {
            menuAdapter.addItem(SimpleMenuItem(MENU_RECLAIM_HOST, "Reclaim Host"))
        }

        menuAdapter.addItem(SimpleMenuItem(MENU_SHOW_CHAT_LEGAL_NOTICE, "Show Chat Legal Notice"))

        // menuAdapter.addItem((new SimpleMenuItem(MENU_VIRTUAL_SOURCE, "Virtual source")));
        // menuAdapter.addItem((new SimpleMenuItem(MENU_INTERNAL_SOURCE, "Internal source")));


        val popupWindowLayout = LayoutInflater.from(context).inflate(R.layout.popupwindow, null)
        val shareActions = popupWindowLayout.findViewById<View>(R.id.actionListView) as ListView
        val window = PopupWindow(
            popupWindowLayout,
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        window.setBackgroundDrawable(
            ResourcesCompat.getDrawable(resources, R.drawable.bg_transparent, context.theme)
        )
        shareActions.setAdapter(menuAdapter)
        shareActions.onItemClickListener = OnItemClickListener { _, _, position, _ ->
            val item = menuAdapter.getItem(position) as SimpleMenuItem
            when (item.action) {

                MENU_ALLOW_ATTENDEE_CHAT -> chatController?.allowAttendeeChat(
                    InMeetingChatController.MobileRTCWebinarChatPriviledge.All_Panelists_And_Attendees
                )

                MENU_DISALLOW_ATTENDEE_CHAT -> chatController?.allowAttendeeChat(
                    InMeetingChatController.MobileRTCWebinarChatPriviledge.All_Panelists
                )

                MENU_ALLOW_PANELIST_START_VIDEO -> webinarController?.allowPanelistStartVideo()

                MENU_DISALLOW_PANELIST_START_VIDEO -> webinarController!!.disallowPanelistStartVideo()

                MENU_ANNOTATION_ON -> annotationController?.disableViewerAnnotation(false)

                MENU_ANNOTATION_OFF -> annotationController?.disableViewerAnnotation(true)

                MENU_ANNOTATION_QA -> context.startActivity(Intent(context, QAActivity::class.java))

                MENU_SWITCH_DOMAIN -> ZoomSDK.getInstance().switchDomain("zoom.us", true)

                MENU_CREATE_BO -> callback?.onClickAdminBo()

                MENU_LOWER_ALL_HANDS -> callback?.onClickLowerAllHands()

                MENU_RECLAIM_HOST -> callback?.onClickReclaimHost()

                MENU_DISCONNECT_AUDIO -> callback?.onClickDisconnectAudio()

                MENU_SHOW_PLIST -> callback?.onClickPlist()

                MENU_SPEAKER_OFF, MENU_SPEAKER_ON -> callback?.onClickSwitchLoudSpeaker()

                MENU_VIRTUAL_SOURCE -> ZoomSDK.getInstance().videoSourceHelper?.let {
                    if (null == virtualVideoSource) {
                        virtualVideoSource = VirtualVideoSource(context)
                    }
                    it.setExternalVideoSource(virtualVideoSource)
                }

                MENU_INTERNAL_SOURCE -> {
                    val sourceHelper = ZoomSDK.getInstance().videoSourceHelper
                    val waterMark =
                        BitmapFactory.decodeResource(resources, R.drawable.zm_watermark_sdk)
                    val yuv = YUVConvert.convertBitmapToYuv(waterMark)
                    val data = WaterMarkData(waterMark.getWidth(), waterMark.getHeight(), yuv)
                    sourceHelper.setPreProcessor { rawData ->
                        YUVConvert.addWaterMark(
                            rawData,
                            data,
                            140,
                            120,
                            true
                        )
                    }
                }

                MENU_INTERPRETATION -> interpretationController?.let {
                    if (it.isInterpretationStarted && !it.isInterpreter) {
                        MeetingInterpretationDialog.show(context)
                    }
                }

                MENU_INTERPRETATION_ADMIN ->
                    MeetingInterpretationAdminDialog.show(context)

                MENU_LIVE_TRANSCRIPTION_REQUEST ->
                    RequestLiveTranscriptionDialog.show(context as MyMeetingActivity?)

                MENU_LIVE_TRANSCRIPTION_STOP ->
                    inMeetingService?.inMeetingLiveTranscriptionController?.stopLiveTranscription()


                MENU_SHOW_CHAT_LEGAL_NOTICE ->
                    LegalNoticeDialogUtil.showChatLegalNoticeDialog(context)
            }
            window.dismiss()
        }
        window.isFocusable = true
        window.isOutsideTouchable = true
        window.update()
        if (null != callback) {
            callback!!.showMoreMenu(window)
        }
    }

    val isShowing: Boolean
        get() = visibility == VISIBLE

    companion object {
        private const val TAG = "MeetingOptionBar"
        private const val MENU_DISCONNECT_AUDIO = 0
        private const val MENU_SHOW_PLIST = 4

        //webinar host & co host
        private const val MENU_ALLOW_PANELIST_START_VIDEO = 5
        private const val MENU_ALLOW_ATTENDEE_CHAT = 6
        private const val MENU_DISALLOW_PANELIST_START_VIDEO = 7
        private const val MENU_DISALLOW_ATTENDEE_CHAT = 8
        private const val MENU_SPEAKER_ON = 9
        private const val MENU_SPEAKER_OFF = 10
        private const val MENU_ANNOTATION_OFF = 11
        private const val MENU_ANNOTATION_ON = 12
        private const val MENU_ANNOTATION_QA = 13
        private const val MENU_SWITCH_DOMAIN = 14
        private const val MENU_CREATE_BO = 15
        private const val MENU_LOWER_ALL_HANDS = 16
        private const val MENU_RECLAIM_HOST = 17
        private const val MENU_VIRTUAL_SOURCE = 18
        private const val MENU_INTERNAL_SOURCE = 19
        private const val MENU_INTERPRETATION = 20
        private const val MENU_INTERPRETATION_ADMIN = 21
        private const val MENU_LIVE_TRANSCRIPTION_REQUEST = 22
        private const val MENU_LIVE_TRANSCRIPTION_STOP = 23
        private const val MENU_SHOW_CHAT_LEGAL_NOTICE = 24
    }
}

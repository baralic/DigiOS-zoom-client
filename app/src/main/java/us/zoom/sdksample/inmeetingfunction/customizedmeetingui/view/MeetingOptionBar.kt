package us.zoom.sdksample.inmeetingfunction.customizedmeetingui.view

import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.util.AttributeSet
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView.OnItemClickListener
import android.widget.FrameLayout
import android.widget.ListView
import android.widget.PopupWindow
import android.widget.TextView
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.FragmentManager
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
import us.zoom.sdksample.util.Constants
import us.zoom.sdksample.util.isHold
import us.zoom.sdksample.util.mapTo

class MeetingOptionBar : FrameLayout, View.OnClickListener, Constants.SysProperty {

    private var callback: MeetingOptionBarCallBack? = null

    private lateinit var root: View

    private var topBar: View? = null
    private var meetingNumberText: TextView? = null
    private var meetingPasswordText: TextView? = null
    private lateinit var leaveButton: View
    private lateinit var backButton: View
    lateinit var switchCameraView: View
        private set

    private lateinit var bottomBar: View

    private lateinit var audioButton: TextView
    private lateinit var cameraButton: TextView
    private lateinit var speakerButton: TextView
    private lateinit var shareButton: TextView
    private lateinit var chatButton: TextView
    private lateinit var moreButton: TextView

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

    val isShowing: Boolean
        get() = visibility == VISIBLE

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

    private fun initView(context: Context) {
        root = LayoutInflater.from(context).inflate(R.layout.layout_meeting_option, this, false)
        addView(root)

        // topBar = findViewById(R.id.top_bar)
        meetingNumberText = topBar?.findViewById(R.id.meetingNumber)
        meetingPasswordText = topBar?.findViewById(R.id.meetingPassword)
        topBar?.visibility = GONE

        bottomBar = findViewById(R.id.bottom_bar)
        backButton = bottomBar.findViewById(R.id.backButton)
        leaveButton = bottomBar.findViewById(R.id.leaveButton)
        switchCameraView = bottomBar.findViewById(R.id.switchCameraView)
        audioButton = bottomBar.findViewById(R.id.audioButton)
        speakerButton = bottomBar.findViewById(R.id.speakerButton)
        cameraButton = bottomBar.findViewById(R.id.cameraButton)
        shareButton = bottomBar.findViewById(R.id.shareButton)
        chatButton = bottomBar.findViewById(R.id.chatButton)
        moreButton = bottomBar.findViewById(R.id.moreActionButton)

        addListeners()

        inMeetingService = ZoomSDK.getInstance().inMeetingService
        shareController = inMeetingService?.inMeetingShareController
        videoController = inMeetingService?.inMeetingVideoController
        audioController = inMeetingService?.inMeetingAudioController
        webinarController = inMeetingService?.inMeetingWebinarController
        annotationController = inMeetingService?.inMeetingAnnotationController
        chatController = inMeetingService?.inMeetingChatController
        interpretationController = inMeetingService?.inMeetingInterpretationController
    }

    private fun addListeners() {
        backButton.setOnClickListener(this)
        leaveButton.setOnClickListener(this)
        switchCameraView.setOnClickListener(this)
        shareButton.setOnClickListener(this)
        cameraButton.setOnClickListener(this)
        audioButton.setOnClickListener(this)
        chatButton.setOnClickListener(this)
        moreButton.setOnClickListener(this)
        speakerButton.setOnClickListener(this)
    }

    fun mapKeyDown(keyCode: Int, event: KeyEvent): KeyEvent? =
        if (this.hasFocus() && keyCode.isHold) {
            callback?.onClickDisconnectAudio()
            event.mapTo(keyCode)
        } else null

    fun mapKeyUp(keyCode: Int, event: KeyEvent): KeyEvent? =
        if (this.hasFocus() && keyCode.isHold) event.mapTo(keyCode)
        else null

    fun hideOrShowToolbar(hidden: Boolean) {
        visibility = VISIBLE
        bringToFront()
        removeCallbacks(autoHidden)
        if (hidden) {
            visibility = INVISIBLE
        } else {
            postDelayed(autoHidden, getInt(Constants.KEY_AUTO_JOIN, 5000).toLong())
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
        get() = topBar?.measuredHeight ?: 0

    fun updateMeetingNumber(text: String?) {
        meetingNumberText?.text = text
    }

    fun updateMeetingPassword(text: String?) {
        if (text?.isNotEmpty() == true) {
            meetingPasswordText?.visibility = VISIBLE
            meetingPasswordText?.text = text
        } else {
            meetingPasswordText?.visibility = GONE
        }
    }

    fun refreshToolbar() {
        updateAudioButton()
        updateSpeakerButton()
        updateVideoButton()
        updateShareButton()
        updateChatButton()
        updateMoreActionsButton()
        updateSwitchCameraButton()
    }

    fun updateAudioButton() {
        val drawableId = if (audioController?.isAudioConnected == true) {
            if (audioController?.isMyAudioMuted == true) R.drawable.icon_meeting_audio_mute
            else R.drawable.icon_meeting_audio
        } else R.drawable.icon_meeting_noaudio
        val drawable = ResourcesCompat.getDrawable(resources, drawableId, context.theme)
        audioButton.setCompoundDrawablesWithIntrinsicBounds(null, drawable, null, null)
    }

    fun updateSpeakerButton() = with(speakerButton) {
        visibility = audioController?.let {
            if (it.canSwitchAudioOutput()) {
                val drawableId =
                    if (it.loudSpeakerStatus) R.drawable.speaker_icon
                    else R.drawable.speaker_icon_mute
                val drawable = ResourcesCompat.getDrawable(resources, drawableId, context.theme)
                setCompoundDrawablesWithIntrinsicBounds(null, drawable, null, null)
                VISIBLE
            } else GONE
        } ?: GONE
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
        shareButton.visibility = shareController?.let {
            if (hasShareFeature().not()) GONE
            else if (isMySelfWebinarAttendee) GONE
            else {
                val drawableId: Int
                val stringId: Int
                if (it.isSharingOut) {
                    stringId = R.string.meeting_stop_share
                    drawableId = R.drawable.icon_share_pause
                } else {
                    stringId = R.string.meeting_start_share
                    drawableId = R.drawable.icon_share_resume
                }
                shareButton.setText(stringId)
                val drawable = ResourcesCompat.getDrawable(resources, drawableId, context.theme)
                shareButton.setCompoundDrawablesWithIntrinsicBounds(null, drawable, null, null)
                VISIBLE
            }
        } ?: GONE
    }

    private fun updateChatButton() {
        chatButton.visibility = if (hasChatFeature()) VISIBLE else GONE
    }

    private fun updateMoreActionsButton() {
        moreButton.visibility = if (hasMoreActionsFeature()) VISIBLE else GONE
    }

    fun updateVideoButton() {
        val drawableId =
            if (videoController?.isMyVideoMuted == true) R.drawable.icon_meeting_video_mute
            else R.drawable.icon_meeting_video
        val drawable = ResourcesCompat.getDrawable(resources, drawableId, context.theme)
        cameraButton.setCompoundDrawablesWithIntrinsicBounds(null, drawable, null, null)
    }

    fun updateSwitchCameraButton() {
        // TODO: ARGO has only back camera
        //  if (videoController?.isMyVideoMuted == true) {
        //      switchCameraView.visibility = GONE
        //  } else {
        //      switchCameraView.visibility = VISIBLE
        //  }
    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.backButton -> callback?.onClickBack()

            R.id.leaveButton -> callback?.onClickLeave()

            R.id.shareButton -> callback?.onClickShare()

            R.id.cameraButton -> callback?.onClickVideo()

            R.id.audioButton -> callback?.onClickAudio()

            R.id.speakerButton -> callback?.onClickSwitchLoudSpeaker()

            R.id.switchCameraView -> callback?.onClickSwitchCamera()

            R.id.moreActionButton -> showMoreMenuPopupWindow()

            R.id.chatButton -> callback?.onClickChats()

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
        // TODO: Handled by [speakerButton]
        //  if (audioController?.canSwitchAudioOutput() == true) {
        //      if (audioController?.loudSpeakerStatus == true) {
        //          menuAdapter.addItem(SimpleMenuItem(MENU_SPEAKER_OFF, "Speak Off"))
        //      } else {
        //          menuAdapter.addItem(SimpleMenuItem(MENU_SPEAKER_ON, "Speak On"))
        //      }
        //  }

        // TODO: Not compliant with ARGO
        //  if (!isMySelfWebinarAttendee) menuAdapter.addItem(
        //      SimpleMenuItem(MENU_SHOW_PLIST, "Paticipants")
        //  )

        if (annotationController?.canDisableViewerAnnotation() == true) {
            if (annotationController?.isViewerAnnotationDisabled == false) {
                menuAdapter.addItem(SimpleMenuItem(MENU_ANNOTATION_OFF, "Disable Annotation"))
            } else {
                menuAdapter.addItem(SimpleMenuItem(MENU_ANNOTATION_ON, "Enable Annotation"))
            }
        }
        if (isMySelfWebinarHostCoHost) {
            var item =
                if (webinarController?.isAllowPanellistStartVideo == true) {
                    SimpleMenuItem(
                        MENU_DISALLOW_PANELIST_START_VIDEO,
                        "Disallow panelist start video"
                    )
                } else {
                    SimpleMenuItem(MENU_ALLOW_PANELIST_START_VIDEO, "Allow panelist start video")
                }
            menuAdapter.addItem(item)

            item = if (webinarController?.isAllowAttendeeChat == true) {
                SimpleMenuItem(MENU_DISALLOW_ATTENDEE_CHAT, "Disallow attendee chat")
            } else {
                SimpleMenuItem(MENU_ALLOW_ATTENDEE_CHAT, "Allow attendee chat")
            }
            menuAdapter.addItem(item)
        }
        if (BuildConfig.DEBUG) {
            inMeetingService?.myUserInfo?.let {
                if (inMeetingService?.isWebinarMeeting == true) {
                    if (inMeetingService?.inMeetingQAController?.isQAEnabled == true) {
                        menuAdapter.addItem(SimpleMenuItem(MENU_ANNOTATION_QA, "QA"))
                    }
                }
            }

            if (interpretationController?.isInterpretationEnabled == true
                && interpretationController?.isInterpreter == false
                && interpretationController?.isInterpretationStarted == true
            ) {
                menuAdapter.addItem(SimpleMenuItem(MENU_INTERPRETATION, "Language Interpretation"))
            }

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

        // TODO: Not compliant with ARGO
        // menuAdapter.addItem(SimpleMenuItem(MENU_SHOW_CHAT_LEGAL_NOTICE, "Show Chat Legal Notice"))

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

                MENU_DISALLOW_PANELIST_START_VIDEO -> webinarController?.disallowPanelistStartVideo()

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

    private fun hasShareFeature() = getBoolean(Constants.SysProperty.PROPERTY_USE_SHARE)
    private fun hasChatFeature() = getBoolean(Constants.SysProperty.PROPERTY_USE_CHAT)
    private fun hasCaptionsFeature() = getBoolean(Constants.SysProperty.PROPERTY_USE_CAPTIONS)
    private fun hasWhiteboardFeature() = getBoolean(Constants.SysProperty.PROPERTY_USE_WHITEBOARD)
    private fun hasParticipantsFeature() = getBoolean(Constants.SysProperty.PROPERTY_USE_PARTICIPANTS)
    private fun hasReactionsFeature() = getBoolean(Constants.SysProperty.PROPERTY_USE_REACTIONS)
    private fun hasRecordingFeature() = getBoolean(Constants.SysProperty.PROPERTY_USE_RECORDING)
    private fun hasMoreActionsFeature() = getBoolean(Constants.SysProperty.PROPERTY_USE_MORE)


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

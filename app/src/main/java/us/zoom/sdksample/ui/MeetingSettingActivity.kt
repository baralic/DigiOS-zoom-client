package us.zoom.sdksample.ui

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.CompoundButton
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Switch
import androidx.fragment.app.FragmentActivity
import us.zoom.sdk.InviteOptions
import us.zoom.sdk.MeetingViewsOptions
import us.zoom.sdk.ZoomSDK
import us.zoom.sdk.ZoomSDKInitParams
import us.zoom.sdk.ZoomSDKInitializeListener
import us.zoom.sdk.ZoomSDKRawDataMemoryMode
import us.zoom.sdksample.R
import us.zoom.sdksample.inmeetingfunction.zoommeetingui.ZoomMeetingUISettingHelper

class MeetingSettingActivity : FragmentActivity(), CompoundButton.OnCheckedChangeListener {

    private lateinit var settingContain: LinearLayout
    private lateinit var rawDataSettingContain: LinearLayout
    private lateinit var editParticipantId: EditText
    private lateinit var webinarToken: EditText
    private lateinit var editCustomMeetingId: EditText
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_meeting_setting)
        val btnCustomUI = findViewById<View>(R.id.btn_custom_ui) as Switch
        val isCustomUI = ZoomSDK.getInstance().meetingSettingsHelper.isCustomizedMeetingUIEnabled
        btnCustomUI.setChecked(isCustomUI)
        settingContain = findViewById(R.id.settings_contain)
        settingContain.visibility = if (isCustomUI) View.GONE else View.VISIBLE
        val hasRawDataLicense = ZoomSDK.getInstance().hasRawDataLicense()
        val btnVideoSource = findViewById<View>(R.id.btn_external_video_source) as Switch
        btnVideoSource.visibility = if (hasRawDataLicense) View.VISIBLE else View.GONE
        btnVideoSource.setChecked(ZoomMeetingUISettingHelper.useExternalVideoSource)
        btnVideoSource.setOnCheckedChangeListener { _, isChecked ->
            ZoomMeetingUISettingHelper.changeVideoSource(
                isChecked,
                this@MeetingSettingActivity
            )
        }
        val hasLicense = ZoomSDK.getInstance().hasRawDataLicense()
        rawDataSettingContain = findViewById(R.id.rawdata_settings_contain)
        rawDataSettingContain.setVisibility(if (isCustomUI && hasLicense) View.VISIBLE else View.GONE)
        run {
            var i = 0
            val count = settingContain.childCount
            while (i < count) {
                val child = settingContain.getChildAt(i)
                if (null != child && child is Switch) {
                    child.setOnCheckedChangeListener(this)
                    initCheck(child)
                }
                i++
            }
        }
        initInvite()
        val options = ZoomMeetingUISettingHelper.getMeetingOptions()
        editParticipantId = findViewById(R.id.edit_participant_id)
        webinarToken = findViewById(R.id.webinar_token)
        editCustomMeetingId = findViewById(R.id.edit_custom_meeting_id)
        editParticipantId.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable) {
                options.customer_key = editParticipantId.getText().toString()
            }
        })
        webinarToken.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable) {
                options.webinar_token = webinarToken.getText().toString()
            }
        })
        editCustomMeetingId.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable) {
                options.custom_meeting_id = editCustomMeetingId.getText().toString()
            }
        })
        editParticipantId.setText(options.customer_key)
        webinarToken.setText(options.webinar_token)
        editCustomMeetingId.setText(options.custom_meeting_id)
        (findViewById<View>(R.id.btn_switch_domain) as Switch).setOnCheckedChangeListener(this)
        val btnRawData = findViewById<Switch>(R.id.btn_raw_data)
        val sharedPreferences = getSharedPreferences("UI_Setting", MODE_PRIVATE)
        val enable = sharedPreferences.getBoolean("enable_rawdata", false)
        btnRawData.setChecked(enable)
        btnRawData.setOnCheckedChangeListener(this)
        btnCustomUI.setOnCheckedChangeListener(this)
        val linearLayout = findViewById<LinearLayout>(R.id.view_option_contain)
        var index = 0
        val count = linearLayout.childCount
        while (index < count) {
            val layout = linearLayout.getChildAt(index) as LinearLayout
            initMeetingViewOption(layout)
            index++
        }
    }

    fun initMeetingViewOption(contain: ViewGroup) {
        val options = ZoomMeetingUISettingHelper.getMeetingOptions()
        var index = 0
        val count = contain.childCount
        while (index < count) {
            val checkBox = contain.getChildAt(index) as CheckBox
            when (checkBox.id) {
                R.id.no_btn_video -> {
                    checkBox.setChecked(options.meeting_views_options and MeetingViewsOptions.NO_BUTTON_VIDEO != 0)
                }

                R.id.no_btn_audio -> {
                    checkBox.setChecked(options.meeting_views_options and MeetingViewsOptions.NO_BUTTON_AUDIO != 0)
                }

                R.id.no_btn_share -> {
                    checkBox.setChecked(options.meeting_views_options and MeetingViewsOptions.NO_BUTTON_SHARE != 0)
                }

                R.id.no_btn_participants -> {
                    checkBox.setChecked(options.meeting_views_options and MeetingViewsOptions.NO_BUTTON_PARTICIPANTS != 0)
                }

                R.id.no_btn_more -> {
                    checkBox.setChecked(options.meeting_views_options and MeetingViewsOptions.NO_BUTTON_MORE != 0)
                }

                R.id.no_text_meeting_id -> {
                    checkBox.setChecked(options.meeting_views_options and MeetingViewsOptions.NO_TEXT_MEETING_ID != 0)
                }

                R.id.no_text_password -> {
                    checkBox.setChecked(options.meeting_views_options and MeetingViewsOptions.NO_TEXT_PASSWORD != 0)
                }

                R.id.no_btn_leave -> {
                    checkBox.setChecked(options.meeting_views_options and MeetingViewsOptions.NO_BUTTON_LEAVE != 0)
                }

                R.id.no_btn_switch_camera -> {
                    checkBox.setChecked(options.meeting_views_options and MeetingViewsOptions.NO_BUTTON_SWITCH_CAMERA != 0)
                }

                R.id.no_btn_switch_audio_source -> {
                    checkBox.setChecked(options.meeting_views_options and MeetingViewsOptions.NO_BUTTON_SWITCH_AUDIO_SOURCE != 0)
                }
            }
            setMeetingViewOption(checkBox)
            index++
        }
    }

    fun initInvite() {
        val options = ZoomMeetingUISettingHelper.getMeetingOptions()
        val listener = CompoundButton.OnCheckedChangeListener { buttonView, isChecked ->
            when (buttonView.id) {
                R.id.invite_disable_all -> {
                    options.invite_options =
                        if (isChecked) options.invite_options and InviteOptions.INVITE_DISABLE_ALL else options.invite_options or InviteOptions.INVITE_ENABLE_ALL
                }

                R.id.invite_enable_all -> {
                    options.invite_options =
                        if (isChecked) options.invite_options or InviteOptions.INVITE_ENABLE_ALL else options.invite_options and InviteOptions.INVITE_DISABLE_ALL
                }

                R.id.invite_via_sms -> {
                    options.invite_options =
                        options.invite_options and InviteOptions.INVITE_VIA_SMS.inv()
                    options.invite_options =
                        if (isChecked) options.invite_options or InviteOptions.INVITE_VIA_SMS else options.invite_options xor InviteOptions.INVITE_VIA_SMS
                }

                R.id.invite_via_email -> {
                    options.invite_options =
                        options.invite_options and InviteOptions.INVITE_VIA_EMAIL.inv()
                    options.invite_options =
                        if (isChecked) options.invite_options or InviteOptions.INVITE_VIA_EMAIL else options.invite_options xor InviteOptions.INVITE_VIA_EMAIL
                }

                R.id.invite_copy_url -> {
                    options.invite_options =
                        options.invite_options and InviteOptions.INVITE_COPY_URL.inv()
                    options.invite_options =
                        if (isChecked) options.invite_options or InviteOptions.INVITE_COPY_URL else options.invite_options xor InviteOptions.INVITE_COPY_URL
                }
            }
            initInvite()
        }
        var checkBox = findViewById<CheckBox>(R.id.invite_disable_all)
        checkBox.setOnCheckedChangeListener(null)
        checkBox.setChecked(options.invite_options or InviteOptions.INVITE_DISABLE_ALL == 0)
        checkBox.setOnCheckedChangeListener(listener)
        checkBox = findViewById(R.id.invite_enable_all)
        checkBox.setOnCheckedChangeListener(null)
        checkBox.setChecked(options.invite_options and InviteOptions.INVITE_ENABLE_ALL == InviteOptions.INVITE_ENABLE_ALL)
        checkBox.setOnCheckedChangeListener(listener)
        checkBox = findViewById(R.id.invite_via_sms)
        checkBox.setOnCheckedChangeListener(null)
        checkBox.setChecked(options.invite_options and InviteOptions.INVITE_VIA_SMS != 0)
        checkBox.setOnCheckedChangeListener(listener)
        checkBox = findViewById(R.id.invite_via_email)
        checkBox.setOnCheckedChangeListener(null)
        checkBox.setChecked(options.invite_options and InviteOptions.INVITE_VIA_EMAIL != 0)
        checkBox.setOnCheckedChangeListener(listener)
        checkBox = findViewById(R.id.invite_copy_url)
        checkBox.setOnCheckedChangeListener(null)
        checkBox.setChecked(options.invite_options and InviteOptions.INVITE_COPY_URL != 0)
        checkBox.setOnCheckedChangeListener(listener)
    }

    fun setMeetingViewOption(checkBox: CheckBox) {
        val options = ZoomMeetingUISettingHelper.getMeetingOptions()
        checkBox.setOnCheckedChangeListener { buttonView, isChecked ->
            when (buttonView.id) {
                R.id.no_btn_video -> {
                    options.meeting_views_options =
                        if (isChecked) options.meeting_views_options or MeetingViewsOptions.NO_BUTTON_VIDEO else options.meeting_views_options xor MeetingViewsOptions.NO_BUTTON_VIDEO
                }

                R.id.no_btn_audio -> {
                    options.meeting_views_options =
                        if (isChecked) options.meeting_views_options or MeetingViewsOptions.NO_BUTTON_AUDIO else options.meeting_views_options xor MeetingViewsOptions.NO_BUTTON_AUDIO
                }

                R.id.no_btn_share -> {
                    options.meeting_views_options =
                        if (isChecked) options.meeting_views_options or MeetingViewsOptions.NO_BUTTON_SHARE else options.meeting_views_options xor MeetingViewsOptions.NO_BUTTON_SHARE
                }

                R.id.no_btn_participants -> {
                    options.meeting_views_options =
                        if (isChecked) options.meeting_views_options or MeetingViewsOptions.NO_BUTTON_PARTICIPANTS else options.meeting_views_options xor MeetingViewsOptions.NO_BUTTON_PARTICIPANTS
                }

                R.id.no_btn_more -> {
                    options.meeting_views_options =
                        if (isChecked) options.meeting_views_options or MeetingViewsOptions.NO_BUTTON_MORE else options.meeting_views_options xor MeetingViewsOptions.NO_BUTTON_MORE
                }

                R.id.no_text_meeting_id -> {
                    options.meeting_views_options =
                        if (isChecked) options.meeting_views_options or MeetingViewsOptions.NO_TEXT_MEETING_ID else options.meeting_views_options xor MeetingViewsOptions.NO_TEXT_MEETING_ID
                }

                R.id.no_text_password -> {
                    options.meeting_views_options =
                        if (isChecked) options.meeting_views_options or MeetingViewsOptions.NO_TEXT_PASSWORD else options.meeting_views_options xor MeetingViewsOptions.NO_TEXT_PASSWORD
                }

                R.id.no_btn_leave -> {
                    options.meeting_views_options =
                        if (isChecked) options.meeting_views_options or MeetingViewsOptions.NO_BUTTON_LEAVE else options.meeting_views_options xor MeetingViewsOptions.NO_BUTTON_LEAVE
                }

                R.id.no_btn_switch_camera -> {
                    options.meeting_views_options =
                        if (isChecked) options.meeting_views_options or MeetingViewsOptions.NO_BUTTON_SWITCH_CAMERA else options.meeting_views_options xor MeetingViewsOptions.NO_BUTTON_SWITCH_CAMERA
                }

                R.id.no_btn_switch_audio_source -> {
                    options.meeting_views_options =
                        if (isChecked) options.meeting_views_options or MeetingViewsOptions.NO_BUTTON_SWITCH_AUDIO_SOURCE else options.meeting_views_options xor MeetingViewsOptions.NO_BUTTON_SWITCH_AUDIO_SOURCE
                }
            }
        }
    }

    private fun initCheck(view: Switch) {
        when (view.id) {
            R.id.btn_auto_connect_audio -> {
                view.setChecked(ZoomSDK.getInstance().meetingSettingsHelper.isAutoConnectVoIPWhenJoinMeetingEnabled)
            }

            R.id.btn_mute_my_mic -> {
                view.setChecked(ZoomSDK.getInstance().meetingSettingsHelper.isMuteMyMicrophoneWhenJoinMeetingEnabled)
            }

            R.id.btn_turn_off_video -> {
                view.setChecked(ZoomSDK.getInstance().meetingSettingsHelper.isTurnOffMyVideoWhenJoinMeetingEnabled)
            }

            R.id.btn_hide_no_video_user -> {
                view.setChecked(ZoomSDK.getInstance().meetingSettingsHelper.isHideNoVideoUsersEnabled)
            }

            R.id.btn_auto_switch_video -> {
                view.setChecked(ZoomSDK.getInstance().meetingSettingsHelper.isSwitchVideoLayoutAccordingToUserCountEnabled)
            }

            R.id.btn_gallery_video -> {
                view.setChecked(ZoomSDK.getInstance().meetingSettingsHelper.isGalleryVideoViewDisabled)
            }

            R.id.btn_show_tool_bar -> {
                view.setChecked(ZoomSDK.getInstance().meetingSettingsHelper.isAlwaysShowMeetingToolbarEnabled)
            }

            R.id.btn_no_video_title_share -> {
                view.setChecked(ZoomSDK.getInstance().meetingSettingsHelper.isNoVideoTileOnShareScreenEnabled)
            }

            R.id.btn_no_leave_btn -> {
                view.setChecked(ZoomSDK.getInstance().meetingSettingsHelper.isNoLeaveMeetingButtonForHostEnabled)
            }

            R.id.btn_no_tips_user_event -> {
                view.setChecked(ZoomSDK.getInstance().meetingSettingsHelper.isNoUserJoinOrLeaveTipEnabled)
            }

            R.id.btn_no_drive_mode -> {
                view.setChecked(ZoomMeetingUISettingHelper.getMeetingOptions().no_driving_mode)
            }

            R.id.btn_no_end_dialog -> {
                view.setChecked(ZoomMeetingUISettingHelper.getMeetingOptions().no_meeting_end_message)
            }

            R.id.btn_hidden_title_bar -> {
                view.setChecked(ZoomMeetingUISettingHelper.getMeetingOptions().no_titlebar)
            }

            R.id.btn_hidden_invite -> {
                view.setChecked(ZoomMeetingUISettingHelper.getMeetingOptions().no_invite)
            }

            R.id.btn_hidden_bottom_bar -> {
                view.setChecked(ZoomMeetingUISettingHelper.getMeetingOptions().no_bottom_toolbar)
            }

            R.id.btn_hidden_dial_in_via_phone -> {
                view.setChecked(ZoomMeetingUISettingHelper.getMeetingOptions().no_dial_in_via_phone)
            }

            R.id.btn_hidden_dial_out_via_phone -> {
                view.setChecked(ZoomMeetingUISettingHelper.getMeetingOptions().no_dial_out_to_phone)
            }

            R.id.btn_no_share -> {
                view.setChecked(ZoomMeetingUISettingHelper.getMeetingOptions().no_share)
            }

            R.id.btn_no_video -> {
                view.setChecked(ZoomMeetingUISettingHelper.getMeetingOptions().no_video)
            }

            R.id.btn_no_audio -> {
                view.setChecked(ZoomMeetingUISettingHelper.getMeetingOptions().no_audio)
            }

            R.id.btn_no_meeting_error_message -> {
                view.setChecked(ZoomMeetingUISettingHelper.getMeetingOptions().no_meeting_error_message)
            }

            R.id.btn_hide_screen_share_toolbar_annotation -> view.setChecked(ZoomSDK.getInstance().meetingSettingsHelper.isHideAnnotationInScreenShareToolbar)
            R.id.btn_hide_screen_share_toolbar_stopshare -> view.setChecked(ZoomSDK.getInstance().meetingSettingsHelper.isHideStopShareInScreenShareToolbar)
            R.id.btn_hidden_disconnect_audio -> {
                view.setChecked(ZoomMeetingUISettingHelper.getMeetingOptions().no_disconnect_audio)
            }

            R.id.btn_hidden_record -> {
                view.setChecked(ZoomMeetingUISettingHelper.getMeetingOptions().no_record)
            }

            R.id.no_unmute_confirm_dialog -> {
                view.setChecked(ZoomMeetingUISettingHelper.getMeetingOptions().no_unmute_confirm_dialog)
            }

            R.id.no_webinar_register_dialog -> {
                view.setChecked(ZoomMeetingUISettingHelper.getMeetingOptions().no_webinar_register_dialog)
            }

            R.id.no_chat_msg_toast -> {
                view.setChecked(ZoomMeetingUISettingHelper.getMeetingOptions().no_chat_msg_toast)
            }
        }
    }

    override fun onCheckedChanged(buttonView: CompoundButton, isChecked: Boolean) {
        when (buttonView.id) {
            R.id.btn_custom_ui -> {
                ZoomSDK.getInstance().meetingSettingsHelper.isCustomizedMeetingUIEnabled = isChecked
                settingContain.visibility = if (isChecked) View.GONE else View.VISIBLE
                val hasLicense = ZoomSDK.getInstance().hasRawDataLicense()
                rawDataSettingContain.visibility =
                    if (isChecked && hasLicense) View.VISIBLE else View.GONE
            }

            R.id.btn_auto_connect_audio -> {
                ZoomSDK.getInstance().meetingSettingsHelper.setAutoConnectVoIPWhenJoinMeeting(
                    isChecked
                )
            }

            R.id.btn_mute_my_mic -> {
                ZoomSDK.getInstance().meetingSettingsHelper.setMuteMyMicrophoneWhenJoinMeeting(
                    isChecked
                )
            }

            R.id.btn_turn_off_video -> {
                ZoomSDK.getInstance().meetingSettingsHelper.setTurnOffMyVideoWhenJoinMeeting(
                    isChecked
                )
            }

            R.id.btn_hide_no_video_user -> {
                ZoomSDK.getInstance().meetingSettingsHelper.isHideNoVideoUsersEnabled = isChecked
            }

            R.id.btn_auto_switch_video -> {
                ZoomSDK.getInstance().meetingSettingsHelper.isSwitchVideoLayoutAccordingToUserCountEnabled =
                    isChecked
            }

            R.id.btn_gallery_video -> {
                ZoomSDK.getInstance().meetingSettingsHelper.isGalleryVideoViewDisabled = !isChecked
            }

            R.id.btn_show_tool_bar -> {
                ZoomSDK.getInstance().meetingSettingsHelper.isAlwaysShowMeetingToolbarEnabled =
                    isChecked
            }

            R.id.btn_no_video_title_share -> {
                ZoomSDK.getInstance().meetingSettingsHelper.isNoVideoTileOnShareScreenEnabled =
                    isChecked
            }

            R.id.btn_no_leave_btn -> {
                ZoomSDK.getInstance().meetingSettingsHelper.isNoLeaveMeetingButtonForHostEnabled =
                    isChecked
            }

            R.id.btn_no_tips_user_event -> {
                ZoomSDK.getInstance().meetingSettingsHelper.isNoUserJoinOrLeaveTipEnabled =
                    isChecked
            }

            R.id.btn_no_drive_mode -> {
                ZoomMeetingUISettingHelper.getMeetingOptions().no_driving_mode = isChecked
            }

            R.id.btn_no_end_dialog -> {
                ZoomMeetingUISettingHelper.getMeetingOptions().no_meeting_end_message = isChecked
            }

            R.id.btn_hidden_title_bar -> {
                ZoomMeetingUISettingHelper.getMeetingOptions().no_titlebar = isChecked
            }

            R.id.btn_hidden_invite -> {
                ZoomMeetingUISettingHelper.getMeetingOptions().no_invite = isChecked
            }

            R.id.btn_hidden_bottom_bar -> {
                ZoomMeetingUISettingHelper.getMeetingOptions().no_bottom_toolbar = isChecked
            }

            R.id.btn_hidden_dial_in_via_phone -> {
                ZoomMeetingUISettingHelper.getMeetingOptions().no_dial_in_via_phone = isChecked
            }

            R.id.btn_hidden_dial_out_via_phone -> {
                ZoomMeetingUISettingHelper.getMeetingOptions().no_dial_out_to_phone = isChecked
            }

            R.id.btn_hidden_disconnect_audio -> {
                ZoomMeetingUISettingHelper.getMeetingOptions().no_disconnect_audio = isChecked
            }

            R.id.btn_hidden_record -> {
                ZoomMeetingUISettingHelper.getMeetingOptions().no_record = isChecked
            }

            R.id.btn_no_share -> {
                ZoomMeetingUISettingHelper.getMeetingOptions().no_share = isChecked
            }

            R.id.btn_no_video -> {
                ZoomMeetingUISettingHelper.getMeetingOptions().no_video = isChecked
            }

            R.id.btn_no_audio -> {
                ZoomMeetingUISettingHelper.getMeetingOptions().no_audio = isChecked
            }

            R.id.btn_no_meeting_error_message -> {
                ZoomMeetingUISettingHelper.getMeetingOptions().no_meeting_error_message = isChecked
            }

            R.id.no_unmute_confirm_dialog -> {
                ZoomMeetingUISettingHelper.getMeetingOptions().no_unmute_confirm_dialog = isChecked
            }

            R.id.no_webinar_register_dialog -> {
                ZoomMeetingUISettingHelper.getMeetingOptions().no_webinar_register_dialog =
                    isChecked
            }

            R.id.no_chat_msg_toast -> ZoomMeetingUISettingHelper.getMeetingOptions().no_chat_msg_toast =
                isChecked

            R.id.btn_force_start_video -> {
                ZoomSDK.getInstance().meetingSettingsHelper.enableForceAutoStartMyVideoWhenJoinMeeting(
                    isChecked
                )
            }

            R.id.btn_force_stop_video -> {
                ZoomSDK.getInstance().meetingSettingsHelper.enableForceAutoStopMyVideoWhenJoinMeeting(
                    isChecked
                )
            }

            R.id.btn_show_audio_select_dialog -> {
                ZoomSDK.getInstance().meetingSettingsHelper.disableAutoShowSelectJoinAudioDlgWhenJoinMeeting(
                    isChecked
                )
            }

            R.id.btn_raw_data -> {
                val sharedPreferences = getSharedPreferences("UI_Setting", MODE_PRIVATE)
                sharedPreferences.edit().putBoolean("enable_rawdata", isChecked).commit()
            }

            R.id.btn_hide_screen_share_toolbar_annotation -> ZoomSDK.getInstance().meetingSettingsHelper.hideAnnotationInScreenShareToolbar(
                isChecked
            )

            R.id.btn_hide_screen_share_toolbar_stopshare -> ZoomSDK.getInstance().meetingSettingsHelper.hideStopShareInScreenShareToolbar(
                isChecked
            )

            R.id.btn_switch_domain -> {
                val success = ZoomSDK.getInstance().switchDomain("https://www.zoomus.cn", true)
                Log.d(TAG, "switchDomain:$success")
                if (success) {
                    val initParams = ZoomSDKInitParams()
                    initParams.enableLog = true
                    initParams.logSize = 50
                    initParams.domain = "https://www.zoomus.cn"
                    initParams.videoRawDataMemoryMode =
                        ZoomSDKRawDataMemoryMode.ZoomSDKRawDataMemoryModeStack
                    ZoomSDK.getInstance().initialize(this, object : ZoomSDKInitializeListener {
                        override fun onZoomSDKInitializeResult(
                            errorCode: Int,
                            internalErrorCode: Int
                        ) {
                            Log.d(TAG, "onZoomSDKInitializeResult:$errorCode:$internalErrorCode")
                        }

                        override fun onZoomAuthIdentityExpired() {
                            Log.d(TAG, "onZoomAuthIdentityExpired:")
                        }
                    }, initParams)
                }
            }
        }
    }

    companion object {
        private val TAG = MeetingSettingActivity::class.java.getSimpleName()
    }
}
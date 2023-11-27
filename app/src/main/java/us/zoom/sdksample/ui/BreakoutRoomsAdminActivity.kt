package us.zoom.sdksample.ui

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView.OnItemClickListener
import android.widget.BaseAdapter
import android.widget.Button
import android.widget.ListView
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.FragmentActivity
import us.zoom.sdk.BOOption
import us.zoom.sdk.BOStatus
import us.zoom.sdk.BOStopCountdown
import us.zoom.sdk.IBOAdmin
import us.zoom.sdk.IBOAssistant
import us.zoom.sdk.IBOAttendee
import us.zoom.sdk.IBOAttendeeEvent
import us.zoom.sdk.IBOAttendeeEvent.ATTENDEE_REQUEST_FOR_HELP_RESULT
import us.zoom.sdk.IBOCreator
import us.zoom.sdk.IBOCreatorEvent
import us.zoom.sdk.IBOData
import us.zoom.sdk.IBODataEvent
import us.zoom.sdk.IBOMeeting
import us.zoom.sdk.InMeetingBOController
import us.zoom.sdk.InMeetingBOControllerListener
import us.zoom.sdk.MobileRTCSDKError
import us.zoom.sdk.PreAssignBODataStatus
import us.zoom.sdk.ReturnToMainSessionHandler
import us.zoom.sdk.ZoomSDK
import us.zoom.sdksample.R
import us.zoom.sdksample.ui.dialog.AssignNewUserToRunningBODialog
import us.zoom.sdksample.ui.dialog.AssignUserToBODialog

class BreakoutRoomsAdminActivity : FragmentActivity(), InMeetingBOControllerListener {

    private val TAG = BreakoutRoomsAdminActivity::class.java.getSimpleName()
    private lateinit var mBoController: InMeetingBOController

    private val mBoCreatorEvent: IBOCreatorEvent = object : IBOCreatorEvent {
        override fun onBOCreateSuccess(strBOID: String) {
            Log.i(TAG, "onBOCreateSuccess: boId: $strBOID")
        }

        override fun onWebPreAssignBODataDownloadStatusChanged(status: PreAssignBODataStatus) {
            Log.i(TAG, "onWebPreAssignBODataDownloadStatusChanged: status: $status")
            if (status == PreAssignBODataStatus.PreAssignBODataStatus_download_fail) {
                if (!this@BreakoutRoomsAdminActivity::mBoController.isInitialized) {
                    mBoController.boCreatorHelper?.requestAndUseWebPreAssignBOList()
                }
            }
        }
    }
    private lateinit var mBoLv: ListView
    private lateinit var mUnassignedUsersLv: ListView
    private lateinit var mBtnOpenBo: Button
    private lateinit var mBtnAddBo: Button
    private lateinit var mAdapter: BoListAdapter
    private lateinit var mNewUsersAdapter: BoNewUserListAdapter

    private var mBoCount = 0
    private var mList: MutableList<IBOMeeting> = ArrayList()
    private var mNewUserList: MutableList<String> = ArrayList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_bo_admin)
        mBoLv = findViewById<View>(R.id.lv_bo) as ListView
        mUnassignedUsersLv = findViewById<View>(R.id.lv_unassigned_users) as ListView
        mBtnOpenBo = findViewById<View>(R.id.btn_open_bo) as Button
        mBtnAddBo = findViewById<View>(R.id.btn_add_bo) as Button
        mBoController = ZoomSDK.getInstance().inMeetingService.inMeetingBOController
        mBoController.addListener(this)
        mAdapter = BoListAdapter(this, mList, mBoController)
        mNewUsersAdapter = BoNewUserListAdapter(this, mNewUserList, mBoController)
        val iboData = mBoController.boDataHelper
        if (iboData != null) {
            val bIds = iboData.boMeetingIDList
            if (bIds == null || bIds.size == 0) {
                Log.d(TAG, "first create break room")
                createBO()
            } else {
                Log.d(TAG, "break rooms already exists")
                mBoCount = bIds.size
                for (id in bIds) {
                    val iboMeeting = iboData.getBOMeetingByID(id)
                    if (null != iboMeeting) {
                        mList.add(iboMeeting)
                    }
                }
            }
        }
        mBoLv.setAdapter(mAdapter)
        mUnassignedUsersLv.setAdapter(mNewUsersAdapter)
        mBoLv.onItemClickListener = OnItemClickListener { _, _, position, _ ->
            val intent = Intent(this@BreakoutRoomsAdminActivity, BoEditActivity::class.java)
            intent.putExtra(BoEditActivity.ARG_BO_ID, mList[position].boId)
            startActivityForResult(intent, REQUEST_ID_BO_EDIT)
        }
        val isBOStarted = mBoController.isBOStarted
        mBtnOpenBo.text = if (isBOStarted) "Close All BO" else "Open All BO"
        mBtnAddBo.setEnabled(!isBOStarted)
    }

    override fun onResume() {
        super.onResume()
        registBoDataEvent()
    }

    override fun onPause() {
        super.onPause()
        unRegistBoDataEvent()
    }

    override fun onDestroy() {
        super.onDestroy()
        mBoController.removeListener(this)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            REQUEST_ID_BO_EDIT -> {
                val iboData = mBoController.boDataHelper
                if (iboData != null) {
                    mList.clear()
                    val bIds = iboData.boMeetingIDList
                    if (bIds != null && bIds.size > 0) {
                        mBoCount = bIds.size
                        for (id in bIds) {
                            val iboMeeting = iboData.getBOMeetingByID(id)
                            if (null != iboMeeting) {
                                mList.add(iboMeeting)
                            }
                        }
                    }
                    mAdapter.notifyDataSetChanged()
                    mBtnOpenBo.setEnabled(mList.size > 0)
                }
            }
        }
    }

    private fun createBO() {
        val iboCreator = mBoController.boCreatorHelper
        if (iboCreator != null) {
            mBoCount++
            iboCreator.setEvent(mBoCreatorEvent)
            val option = BOOption()
            option.countdown = BOStopCountdown.COUNTDOWN_SECONDS_15
            option.timerDuration = 200
            option.isBOTimerEnabled = true
            option.isTimerAutoStopBOEnabled = true
            option.isParticipantCanChooseBO = true
            option.isParticipantCanReturnToMainSessionAtAnyTime = false
            val setBOOptionRet = iboCreator.setBOOption(option)
            Log.i(TAG, "createBO setBOOption: $setBOOptionRet")
            val bId = iboCreator.createBO("Breakout Room $mBoCount")
            val iboData = mBoController.boDataHelper
            if (iboData != null) {
                val iboMeeting = iboData.getBOMeetingByID(bId)
                if (null != iboMeeting) {
                    mList.add(iboMeeting)
                }
            }
            mAdapter.notifyDataSetChanged()
            mBtnOpenBo.setEnabled(mList.size > 0)
        }
    }

    private fun batchCreateBO() {
        val iboCreator = mBoController.boCreatorHelper
        if (iboCreator != null) {
            iboCreator.setEvent(mBoCreatorEvent)
            val option = BOOption()
            option.countdown = BOStopCountdown.COUNTDOWN_SECONDS_15
            val setBOOptionRet = iboCreator.setBOOption(option)
            Log.i(TAG, "batchCreateBO setBOOption: $setBOOptionRet")
            val boNames: MutableList<String> = ArrayList()
            var boName: String
            for (i in 0..4) {
                mBoCount++
                boName = "Breakout Room $mBoCount"
                boNames.add(boName)
            }
            var error = MobileRTCSDKError.SDKERR_OTHER_ERROR
            if (ZoomSDK.getInstance().inMeetingService.isWebinarMeeting) {
                val ret = iboCreator.createWebinarBo(boNames)
                if (ret) {
                    error = MobileRTCSDKError.SDKERR_SUCCESS
                }
            } else {
                error = iboCreator.createGroupBO(boNames)
            }
            if (error != MobileRTCSDKError.SDKERR_SUCCESS) {
                return
            }
            val iboData = mBoController.boDataHelper ?: return
            val boIds = iboData.boMeetingIDList
            if (boIds == null || boIds.isEmpty()) {
                return
            }
            mList.clear()
            for (boId in boIds) {
                val iboMeeting = iboData.getBOMeetingByID(boId)
                if (iboMeeting != null) {
                    mList.add(iboMeeting)
                }
            }
            mAdapter.notifyDataSetChanged()
            mBtnOpenBo.setEnabled(mList.size > 0)
        }
    }

    fun onClose(view: View?) {
        finish()
    }

    fun onClickAddBO(view: View?) {
        createBO()
    }

    fun onClickBatchAddBO(view: View?) {
        batchCreateBO()
    }

    fun onClickStartBO(view: View?) {
        val boAdmin = mBoController.boAdminHelper
        if (boAdmin != null) {
            if (mBoController.isBOStarted) {
                if (boAdmin.stopBO()) {
                    mBtnOpenBo.text = "Open All BO"
                    mBtnAddBo.setEnabled(true)
                    mAdapter.notifyDataSetChanged()
                }
            } else {
                if (boAdmin.startBO()) {
                    mBtnOpenBo.text = "Close All BO"
                    mBtnAddBo.setEnabled(false)
                    mAdapter.notifyDataSetChanged()
                }
            }
            refreshUnassignedNewUsersList()
        }
    }

    private fun refreshUnassignedNewUsersList() {
        mNewUserList.clear()
        if (!mBoController.isBOStarted) {
            mUnassignedUsersLv.visibility = View.GONE
            mNewUsersAdapter.notifyDataSetChanged()
            return
        }
        val boData = mBoController.boDataHelper
        val list = boData?.unassginedUserList
        if (list != null && list.size > 0) {
            mUnassignedUsersLv.visibility = View.VISIBLE
            mNewUserList.addAll(list)
        } else {
            mUnassignedUsersLv.visibility = View.GONE
        }
        mNewUsersAdapter.notifyDataSetChanged()
    }

    override fun onHasCreatorRightsNotification(iboCreator: IBOCreator) {
        Log.d(TAG, "onHasCreatorRightsNotification")
        mList.clear()
        mAdapter.notifyDataSetChanged()
    }

    override fun onHasAdminRightsNotification(iboAdmin: IBOAdmin) {
        Log.d(TAG, "onHasAdminRightsNotification")
    }

    override fun onHasAssistantRightsNotification(iboAssistant: IBOAssistant) {
        Log.d(TAG, "onHasAssistantRightsNotification")
    }

    override fun onHasAttendeeRightsNotification(iboAttendee: IBOAttendee) {
        Log.d(TAG, "onHasAttendeeRightsNotification")
        iboAttendee.setEvent(object : IBOAttendeeEvent {
            override fun onHelpRequestHandleResultReceived(eResult: ATTENDEE_REQUEST_FOR_HELP_RESULT) {
                Log.d(TAG, "onHelpRequestHandleResultReceived:$eResult")
            }

            override fun onHostJoinedThisBOMeeting() {
                Log.d(TAG, "onHostJoinedThisBOMeeting:")
            }

            override fun onHostLeaveThisBOMeeting() {
                Log.d(TAG, "onHostLeaveThisBOMeeting:")
            }
        })
    }

    override fun onHasDataHelperRightsNotification(iboData: IBOData) {
        Log.d(TAG, "onHasDataHelperRightsNotification")
    }

    override fun onLostCreatorRightsNotification() {
        Log.d(TAG, "onLostCreatorRightsNotification")
    }

    override fun onLostAdminRightsNotification() {
        Log.d(TAG, "onLostAdminRightsNotification")
    }

    override fun onLostAssistantRightsNotification() {
        Log.d(TAG, "onLostAssistantRightsNotification")
    }

    override fun onLostAttendeeRightsNotification() {
        Log.d(TAG, "onLostAttendeeRightsNotification")
    }

    override fun onLostDataHelperRightsNotification() {
        Log.d(TAG, "onLostDataHelperRightsNotification")
    }

    override fun onNewBroadcastMessageReceived(message: String) {
        Log.d(TAG, "onNewBroadcastMessageReceived:$message")
    }

    override fun onBOStopCountDown(seconds: Int) {
        Log.d(TAG, "onBOStopCountDown seconds: $seconds")
    }

    override fun onHostInviteReturnToMainSession(
        name: String,
        handler: ReturnToMainSessionHandler
    ) {
        Log.d(TAG, "onHostInviteReturnToMainSession name: $name")
    }

    override fun onBOStatusChanged(status: BOStatus) {
        Log.d(TAG, "onBOStatusChanged status: $status")
    }

    class BoListAdapter(
        private val context: Context,
        list: List<IBOMeeting>,
        boController: InMeetingBOController
    ) : BaseAdapter() {
        private val list: List<IBOMeeting>
        private val boController: InMeetingBOController

        init {
            this.list = list
            this.boController = boController
        }

        override fun getCount(): Int = list.size

        override fun getItem(position: Int): Any? =
            if (position >= 0) list[position] else null


        override fun getItemId(position: Int): Long {
            return position.toLong()
        }

        override fun getView(position: Int, convertView: View, parent: ViewGroup): View {
            val tag = "boListItem"
            val view: View
            if (tag == convertView.tag) {
                view = convertView
            } else {
                view = LayoutInflater.from(context).inflate(R.layout.bo_list_item, parent, false)
                view.tag = tag
            }
            val boMeeting = list[position]
            val tvBoName = view.findViewById<View>(R.id.tv_bo_name) as TextView
            val btnAssign = view.findViewById<View>(R.id.btn_assign) as Button
            tvBoName.text = boMeeting.boName
            if (boController.isBOStarted) {
                btnAssign.text = "Join"
            } else {
                btnAssign.text = "Assign"
            }
            btnAssign.setOnClickListener {
                if (boController.isBOStarted) {
                    val iboAssistant = boController.boAssistantHelper
                    if (iboAssistant != null) {
                        val success = iboAssistant.joinBO(boMeeting.boId)
                        Toast.makeText(
                            context,
                            if (success) "Join successfully" else "Join failed",
                            Toast.LENGTH_SHORT
                        ).show()
                        if (success) {
                            if (context is BreakoutRoomsAdminActivity) {
                                context.finish()
                            }
                        }
                    }
                } else {
                    val boData = boController.boDataHelper
                    if (boData != null) {
                        val unassignedUsers = boData.unassginedUserList
                        if (unassignedUsers == null || unassignedUsers.size == 0) {
                            Toast.makeText(
                                context,
                                "All participants have been assigned to Breakout Rooms.",
                                Toast.LENGTH_SHORT
                            ).show()
                        } else {
                            if (context is FragmentActivity) {
                                val fm = context.supportFragmentManager
                                AssignUserToBODialog.show(fm, boMeeting.boId, null)
                            }
                        }
                    }
                }
            }
            return view
        }
    }

    class BoNewUserListAdapter(
        private val context: Context,
        list: List<String>,
        boController: InMeetingBOController
    ) : BaseAdapter() {
        private val list: List<String>
        private val boController: InMeetingBOController

        init {
            this.list = list
            this.boController = boController
        }

        override fun getCount(): Int = list.size

        override fun getItem(position: Int): Any? =
            if (position >= 0) list[position] else null

        override fun getItemId(position: Int): Long {
            return position.toLong()
        }

        override fun getView(position: Int, convertView: View, parent: ViewGroup): View {
            val tag = "boNewUserListItem"
            val view: View
            if (tag == convertView.tag) {
                view = convertView
            } else {
                view = LayoutInflater.from(context)
                    .inflate(R.layout.bo_new_user_list_item, parent, false)
                view.tag = tag
            }
            val tvUserName = view.findViewById<View>(R.id.tv_user_name) as TextView
            val btnAssignTo = view.findViewById<View>(R.id.btn_assign_to) as Button
            val iboData = boController.boDataHelper
            if (iboData != null) tvUserName.text = iboData.getBOUserName(list[position])
            btnAssignTo.setOnClickListener {
                if (context is FragmentActivity) {
                    val fm = context.supportFragmentManager
                    AssignNewUserToRunningBODialog.show(fm, list[position], null)
                }
            }
            return view
        }
    }

    private fun registBoDataEvent() {
        val iboData = mBoController.boDataHelper
        iboData?.setEvent(iboDataEvent)
    }

    private fun unRegistBoDataEvent() {
        val iboData = mBoController.boDataHelper
        iboData?.setEvent(null)
    }

    private val iboDataEvent: IBODataEvent = object : IBODataEvent {
        override fun onBOInfoUpdated(strBOID: String) {
            refreshUnassignedNewUsersList()
        }

        override fun onBOListInfoUpdated() {
            refreshUnassignedNewUsersList()
        }

        override fun onUnAssignedUserUpdated() {
            refreshUnassignedNewUsersList()
        }
    }

    override fun onBOSwitchRequestReceived(strNewBOName: String, strNewBOID: String) {
        Log.d(TAG, "onBOSwitchRequestReceived: boName: $strNewBOName, boID: $strNewBOID")
    }

    override fun onBroadcastBOVoiceStatus(start: Boolean) {}

    companion object {
        const val REQUEST_ID_BO_EDIT = 1
    }
}

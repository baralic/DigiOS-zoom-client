package us.zoom.sdksample.ui

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ListView
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.FragmentActivity
import us.zoom.sdk.IBODataEvent
import us.zoom.sdk.InMeetingBOController
import us.zoom.sdk.ZoomSDK
import us.zoom.sdksample.R
import us.zoom.sdksample.ui.dialog.SwitchAssignedUserToRunningBODialog

class BoEditActivity : FragmentActivity() {

    private lateinit var mBoController: InMeetingBOController
    private lateinit var mEdtxBoName: EditText
    private lateinit var mBoUserLv: ListView
    private lateinit var mAdapter: BoUserListAdapter
    private lateinit var mBtnSaveBoName: Button
    private lateinit var mBtnDeleteBo: Button

    private var mList: MutableList<String> = ArrayList()
    private var mBoId: String? = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_bo_edit)
        mEdtxBoName = findViewById<View>(R.id.edtx_bo_name) as EditText
        mBtnDeleteBo = findViewById<View>(R.id.btn_delete_bo) as Button
        mBtnSaveBoName = findViewById<View>(R.id.btn_save_bo_name) as Button
        mBoUserLv = findViewById<View>(R.id.lv_bo_users) as ListView

        mBoId = intent.getStringExtra(ARG_BO_ID)
        mBoController = ZoomSDK.getInstance().inMeetingService.inMeetingBOController

        val iboData = mBoController.boDataHelper
        if (iboData != null) {
            mEdtxBoName.setText(iboData.getBOMeetingByID(mBoId).boName)
            val bIds = iboData.boMeetingIDList
            mAdapter =
                BoUserListAdapter(this, mList, mBoController, mBoId, bIds != null && bIds.size > 1)
        }
        mBoUserLv.setAdapter(mAdapter)
        refreshBoUserList()
        val isBOStarted = mBoController.isBOStarted()
        mBtnDeleteBo.setEnabled(!isBOStarted)
        mBtnSaveBoName.setEnabled(!isBOStarted)
    }

    override fun onResume() {
        super.onResume()
        registBoDataEvent()
    }

    override fun onPause() {
        super.onPause()
        unRegistBoDataEvent()
    }

    fun onClose(view: View?) {
        finishWithResult()
    }

    fun onClickDeleteBO(view: View?) {
        val iboCreator = mBoController.boCreatorHelper
        if (iboCreator != null && iboCreator.removeBO(mBoId)) {
            finishWithResult()
        }
    }

    fun onClickSaveBoName(view: View?) {
        val iboCreator = mBoController.boCreatorHelper
        if (iboCreator != null && iboCreator.updateBOName(
                mBoId,
                mEdtxBoName.getText().toString()
            )
        ) Toast.makeText(this, "success", Toast.LENGTH_SHORT).show() else Toast.makeText(
            this,
            "fail",
            Toast.LENGTH_SHORT
        ).show()
    }

    private fun finishWithResult() {
        val intent = Intent(this, BreakoutRoomsAdminActivity::class.java)
        setResult(RESULT_OK, intent)
        finish()
    }

    class BoUserListAdapter(
        private val context: Context,
        list: List<String>,
        boController: InMeetingBOController,
        boId: String?,
        canMove: Boolean
    ) : BaseAdapter() {

        private val list: List<String>
        private val boController: InMeetingBOController
        private val canMove: Boolean
        private val boId: String?

        init {
            this.list = list
            this.boController = boController
            this.boId = boId
            this.canMove = canMove
        }

        override fun getCount(): Int = list.size

        override fun getItem(position: Int) = if (position >= 0) list[position] else null

        override fun getItemId(position: Int): Long = position.toLong()

        override fun getView(position: Int, convertView: View, parent: ViewGroup): View {
            val tag = "boUserListItem"
            val view: View
            if (tag == convertView.tag) {
                view = convertView
            } else {
                view =
                    LayoutInflater.from(context).inflate(R.layout.bo_user_list_item, parent, false)
                view.tag = tag
            }
            val tvUserName = view.findViewById<View>(R.id.tv_user_name) as TextView
            val btnMoveTo = view.findViewById<View>(R.id.btn_move_to) as Button
            btnMoveTo.visibility = if (canMove) View.VISIBLE else View.GONE
            val iboData = boController.boDataHelper
            if (iboData != null) tvUserName.text = iboData.getBOUserName(list[position])
            btnMoveTo.setOnClickListener {
                if (context is FragmentActivity) {
                    val fm = context.supportFragmentManager
                    SwitchAssignedUserToRunningBODialog.show(fm, list[position], boId, null)
                }
            }
            return view
        }
    }

    private fun refreshBoUserList() {
        val iboData = mBoController.boDataHelper
        mList.clear()
        if (iboData != null) {
            val users = iboData.getBOMeetingByID(mBoId).boUserList
            if (users != null && users.size > 0) mList.addAll(users)
        }
        mAdapter.notifyDataSetChanged()
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
            if (mBoId.equals(strBOID, ignoreCase = true)) {
                refreshBoUserList()
            }
        }

        override fun onBOListInfoUpdated() {
            refreshBoUserList()
        }

        override fun onUnAssignedUserUpdated() {}
    }

    companion object {
        const val ARG_BO_ID = "ARG_BO_ID"
    }
}

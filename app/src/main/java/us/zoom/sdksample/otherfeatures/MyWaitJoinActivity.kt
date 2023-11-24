package us.zoom.sdksample.otherfeatures

import android.app.Activity
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import us.zoom.sdk.ZoomSDK
import us.zoom.sdksample.R

class MyWaitJoinActivity : Activity(), View.OnClickListener {

    private lateinit var mLeave: Button
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.wait_join_activity)

        val topic = intent.getStringExtra(EXTRA_TOPIC)
        val meetingId = intent.getLongExtra(EXTRA_MEETING_ID, 0)
        val isRepeat = intent.getBooleanExtra(EXTRA_IS_REPEAT, false)
        val date = intent.getStringExtra(EXTRA_DATE)
        val time = intent.getStringExtra(EXTRA_TIME)

        mLeave = findViewById<View>(R.id.btnLeave) as Button
        val txtTime = findViewById<View>(R.id.txtTime) as TextView
        val txtDate = findViewById<View>(R.id.txtDate) as TextView
        val txtTopic = findViewById<View>(R.id.txtTopic) as TextView
        val txtMeetingId = findViewById<View>(R.id.txtMeetingId) as TextView
        val txtIsRepeat = findViewById<View>(R.id.txtIsRepeat) as TextView

        txtIsRepeat.text = "Is Repeat Meeting: $isRepeat"
        if (topic != null) txtTopic.text = "Topic: $topic"
        if (meetingId > 0) txtMeetingId.text = "Meeting ID: $meetingId"
        if (time != null) txtTime.text = "Time: $time"
        if (date != null) txtDate.text = "Date: $date"

        mLeave.setOnClickListener(this)

        ZoomSDK.getInstance().inMeetingService
    }

    override fun onClick(v: View) {
        if (v.id == R.id.btnLeave) {
            onClickLeave()
        }
    }

    private fun onClickLeave() {
        val zoomSDK = ZoomSDK.getInstance()
        val meetingService = zoomSDK.meetingService
        meetingService?.leaveCurrentMeeting(false)
        finish()
    }

    override fun onBackPressed() {
        onClickLeave()
    }

    companion object {
        var EXTRA_TOPIC = "meetingTopic"
        var EXTRA_IS_REPEAT = "meetingIsRepeat"
        var EXTRA_DATE = "meetingDate"
        var EXTRA_TIME = "meetingTime"
        var EXTRA_MEETING_ID = "meetingId"
    }
}

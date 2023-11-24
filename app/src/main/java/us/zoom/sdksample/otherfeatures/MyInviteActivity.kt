package us.zoom.sdksample.otherfeatures

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.TextView
import us.zoom.sdksample.R

class MyInviteActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.invite_activity)

        val txtUrl = findViewById<View>(R.id.txtUrl) as TextView
        val txtSubject = findViewById<View>(R.id.txtSubject) as TextView
        val txtMeetingId = findViewById<View>(R.id.txtMeetingId) as TextView
        val txtPassword = findViewById<View>(R.id.txtPassword) as TextView
        val txtRawPassword = findViewById<View>(R.id.txtRawPassword) as TextView
        val edtText = findViewById<View>(R.id.edtText) as EditText

        val uri = intent.data
        val text = intent.getStringExtra(EXTRA_TEXT)
        val subject = intent.getStringExtra(EXTRA_SUBJECT)
        val meetingId = intent.getLongExtra(EXTRA_MEETING_ID, 0)
        val meetingPassword = intent.getStringExtra(EXTRA_MEETING_PSW)
        val meetingRawPassword = intent.getStringExtra(EXTRA_MEETING_RAW_PSW)

        if (uri != null) txtUrl.text = "URL:$uri"
        if (subject != null) txtSubject.text = "Subject: $subject"
        if (meetingId > 0) txtMeetingId.text = "Meeting ID: $meetingId"
        if (meetingPassword != null) txtPassword.text = "Password: $meetingPassword"
        if (meetingRawPassword != null) txtRawPassword.text = "Raw Password: $meetingRawPassword"
        if (text != null) edtText.setText(text)
    }

    companion object {
        var EXTRA_SUBJECT = Intent.EXTRA_SUBJECT
        var EXTRA_MEETING_ID = "meetingId"
        var EXTRA_MEETING_PSW = "meetingPassword"
        var EXTRA_MEETING_RAW_PSW = "meetingRawPassword"
        var EXTRA_TEXT = Intent.EXTRA_TEXT
    }
}

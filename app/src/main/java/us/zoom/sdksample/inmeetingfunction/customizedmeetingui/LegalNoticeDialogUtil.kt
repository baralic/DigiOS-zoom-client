package us.zoom.sdksample.inmeetingfunction.customizedmeetingui

import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.widget.Toast
import us.zoom.sdk.ZoomSDK

object LegalNoticeDialogUtil {
    fun showChatLegalNoticeDialog(context: Context?) {
        val chatController = ZoomSDK.getInstance().inMeetingService?.inMeetingChatController
        if (chatController?.isMeetingChatLegalNoticeAvailable == false) {
            Toast.makeText(context, "Chat legal notice is not available", Toast.LENGTH_LONG).show()
            return
        }
        val dialog = AlertDialog.Builder(context)
            .setCancelable(false)
            .setTitle(chatController?.chatLegalNoticesPrompt)
            .setMessage(ZoomSDK.getInstance().inMeetingService.inMeetingChatController.chatLegalNoticesExplained)
            .setPositiveButton("Ok") { dialog1: DialogInterface, _: Int -> dialog1.dismiss() }
            .create()
        dialog.show()
    }
}

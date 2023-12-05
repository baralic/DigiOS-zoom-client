package us.zoom.sdksample.inmeetingfunction.customizedmeetingui.view.adapter

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import us.zoom.sdk.InMeetingUserInfo
import us.zoom.sdk.MobileRTCVideoUnitAspectMode
import us.zoom.sdk.MobileRTCVideoUnitRenderInfo
import us.zoom.sdk.MobileRTCVideoView
import us.zoom.sdk.SDKEmojiReactionType
import us.zoom.sdk.ZoomSDK
import us.zoom.sdksample.R
import us.zoom.sdksample.inmeetingfunction.customizedmeetingui.emoji.EmojiReactionHelper
import us.zoom.sdksample.inmeetingfunction.customizedmeetingui.view.adapter.AttenderVideoAdapter.ViewHold

class AttenderVideoAdapter(
    var context: Context,
    viewWidth: Int,
    private val listener: ItemClickListener
) : RecyclerView.Adapter<ViewHold>() {

    fun interface ItemClickListener {
        fun onItemClick(view: View?, position: Int, userId: Long)
    }

    private val userList: MutableList<Long> = ArrayList()
    private val emojiUsers: MutableMap<Long, EmojiParams> = HashMap()
    private var itemSize = 200
    private var selectedPosition = -1
    private var selectedView: View? = null
    private val handler = Handler(Looper.getMainLooper())

    class EmojiParams(var userId: Long, var reactionType: SDKEmojiReactionType) {
        var runnable: Runnable? = null
    }

    fun updateSize(size: Int) {
        itemSize = size
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHold {
        val view = LayoutInflater.from(context).inflate(R.layout.item_attend, parent, false)
        val params = view.layoutParams as RecyclerView.LayoutParams
        params.width = itemSize
        params.height = itemSize
        view.setLayoutParams(params)
        view.setOnClickListener(onClickListener)
        return ViewHold(view)
    }

    private val onClickListener: View.OnClickListener = object : View.OnClickListener {
        override fun onClick(view: View) {
            val userId = view.tag as Long
            if (userId == selectedUserId) {
                return
            }
            val position = userList.indexOf(userId)
            if (position >= userList.size) {
                return
            }
            listener.onItemClick(view, position, userId)
            selectedView?.setBackgroundResource(R.drawable.selector_checkbox)
            view.setBackgroundResource(R.drawable.video_bg)
            selectedView = view
            selectedPosition = position

        }
    }

    init {
        if (viewWidth > 0) {
            itemSize = (viewWidth - 40) / 4
        }
    }

    fun setUserList(userList: List<Long>?) {
        this.userList.clear()
        userList?.let {
            val retUserList: MutableList<Long> = ArrayList()
            for (userId in it) {
                if (!isWebinarAttendee(userId)) {
                    retUserList.add(userId)
                }
            }
            if (retUserList.isNotEmpty()) {
                this.userList.addAll(retUserList)
            }
        }
    }

    fun onSpeakerChanged(userId: Long, list: RecyclerView) {
        val position = userList.indexOf(userId)
        val holder = list.findViewHolderForAdapterPosition(position)
        if (userId != selectedUserId) {
            holder?.itemView?.let { view ->
                if (position < userList.size) {
                    listener.onItemClick(view, position, userId)
                    selectedView?.setBackgroundResource(R.drawable.selector_checkbox)
                    view.setBackgroundResource(R.drawable.video_bg)
                    selectedView = view
                    selectedPosition = position
                }
            }
        }
    }

    override fun onBindViewHolder(holder: ViewHold, position: Int) {
        val userId = userList[position]
        holder.videoView.videoViewManager.removeAllAttendeeVideoUnit()
        holder.videoView.videoViewManager.addAttendeeVideoUnit(userId, holder.renderInfo)
        holder.root.tag = userId
        holder.videoView.tag = position
        val emojiParams = emojiUsers[userId]
        if (emojiParams != null) {
            val drawableId =
                EmojiReactionHelper.getEmojiReactionDrawableId(emojiParams.reactionType)
            if (drawableId == 0) {
                holder.ivEmoji.setVisibility(View.GONE)
                return
            }
            holder.ivEmoji.setVisibility(View.VISIBLE)
            holder.ivEmoji.setImageDrawable(context.resources.getDrawable(drawableId, null))
        } else {
            holder.ivEmoji.setVisibility(View.GONE)
        }
        if (position == selectedPosition) {
            if (null != selectedView) {
                selectedView!!.setBackgroundResource(R.drawable.selector_checkbox)
            }
            holder.root.setBackgroundResource(R.drawable.video_bg)
            selectedView = holder.root
        } else {
            holder.root.setBackgroundResource(R.drawable.selector_checkbox)
        }
    }

    fun addUserList(list: List<Long>) {
        for (userId in list) {
            if (!userList.contains(userId) && !isWebinarAttendee(userId)) {
                userList.add(userId)
                notifyItemInserted(userList.size)
            }
        }
    }

    fun setEmojiUser(emojiParams: EmojiParams) {
        val existedEmojiParams = emojiUsers.put(emojiParams.userId, emojiParams)
        if (existedEmojiParams?.runnable != null) {
            handler.removeCallbacks(existedEmojiParams.runnable!!)
        }
        val index = userList.indexOf(emojiParams.userId)
        notifyItemChanged(index)
        emojiParams.runnable = Runnable {
            emojiUsers.remove(emojiParams.userId)
            val pos = userList.indexOf(emojiParams.userId)
            notifyItemChanged(pos)
        }
        handler.postDelayed(emojiParams.runnable!!, REACTION_DURATION.toLong())
    }

    val selectedUserId: Long
        get() = if (selectedPosition >= 0 && selectedPosition < userList.size) {
            userList[selectedPosition]
        } else -1

    fun removeUserList(list: List<Long>?) {
        list?.let {
            for (userId in it) {
                if (userList.contains(userId)) {
                    val index = userList.indexOf(userId)
                    userList.removeAt(index)
                    if (index == selectedPosition) {
                        selectedPosition = 0
                        notifyItemChanged(selectedPosition)
                    }
                    notifyItemRemoved(index)
                }
            }
        }
    }

    override fun getItemCount(): Int = userList.size

    fun clear() {
        handler.removeCallbacksAndMessages(null)
    }

    fun isWebinarAttendee(userId: Long): Boolean {
        val inMeetingService = ZoomSDK.getInstance().inMeetingService ?: return false
        if (!inMeetingService.isWebinarMeeting) {
            return false
        }
        val userInfo = inMeetingService.getUserInfoById(userId)
        return userInfo != null && userInfo.inMeetingUserRole == InMeetingUserInfo.InMeetingUserRole.USERROLE_ATTENDEE
    }

    class ViewHold(var root: View) : RecyclerView.ViewHolder(root) {

        val videoView: MobileRTCVideoView = root.findViewById(R.id.item_videoView)
        val renderInfo = MobileRTCVideoUnitRenderInfo(0, 0, 100, 100)
        val ivEmoji: ImageView = root.findViewById(R.id.iv_emoji)

        init {
            videoView.setZOrderMediaOverlay(true)
            renderInfo.aspect_mode = MobileRTCVideoUnitAspectMode.VIDEO_ASPECT_PAN_AND_SCAN
            renderInfo.is_show_audio_off = true
            renderInfo.is_username_visible = true
            renderInfo.is_border_visible = true
            // renderInfo.aspect_mode = MobileRTCVideoUnitAspectMode.VIDEO_ASPECT_FULL_FILLED;
        }
    }

    companion object {
        const val REACTION_DURATION = 5000
    }
}

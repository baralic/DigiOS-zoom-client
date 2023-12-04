package us.zoom.sdksample.inmeetingfunction.customizedmeetingui.view

import android.app.Service
import android.content.Context
import android.content.res.Configuration
import android.util.AttributeSet
import android.util.Log
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import android.widget.LinearLayout
import android.widget.RelativeLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import us.zoom.sdk.MobileRTCVideoView
import us.zoom.sdksample.R
import us.zoom.sdksample.inmeetingfunction.customizedmeetingui.view.adapter.AttenderVideoAdapter
import us.zoom.sdksample.util.isLeft
import us.zoom.sdksample.util.isRight
import us.zoom.sdksample.util.mapTo

class VideoListLayout : LinearLayout, View.OnClickListener {

    private lateinit var indicator: View
    lateinit var videoList: RecyclerView

    constructor(context: Context?) : super(context) {
        init()
    }

    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs) {
        init()
    }

    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ) {
        init()
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        updateArrow()
        updateOrientation(resources.configuration.orientation)
    }

    private fun init() {
        LayoutInflater.from(context).inflate(R.layout.layout_video_list_indicator, this, true)
        indicator = findViewById(R.id.videoList_indicator)
        videoList = findViewById(R.id.videoList)
        indicator.setOnClickListener(this)
        videoList.addOnScrollListener(onScrollListener)
        updateArrow()
        updateOrientation(resources.configuration.orientation)
    }

    fun mapKeyDown(keyCode: Int, event: KeyEvent): KeyEvent? =
        if (videoList.hasFocus() && (keyCode.isLeft || keyCode.isRight))
            event.mapTo(
                if (keyCode.isRight) KeyEvent.KEYCODE_DPAD_DOWN
                else KeyEvent.KEYCODE_DPAD_UP
            )
        else null

    fun mapKeyUp(keyCode: Int, event: KeyEvent): KeyEvent? =
        if (videoList.hasFocus() && (keyCode.isLeft || keyCode.isRight))
            event.mapTo(
                if (keyCode.isRight) KeyEvent.KEYCODE_DPAD_DOWN
                else KeyEvent.KEYCODE_DPAD_UP
            )
        else null

    fun setAdapter(adapter: AttenderVideoAdapter) {
        videoList.adapter = adapter
    }

    override fun onClick(v: View) {
        if (v === indicator) {
            if (indexOfChild(videoList) < 0) {
                addView(videoList)
            } else {
                removeView(videoList)
            }
            updateArrow()
        }
    }

    override fun setVisibility(visibility: Int) {
        super.setVisibility(visibility)
        if (visibility == VISIBLE) {
            if (indexOfChild(videoList) < 0) {
                addView(videoList)
            }
            updateArrow()
        } else {
            removeView(videoList)
        }
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        val orientation = newConfig.orientation
        updateArrow()
        updateOrientation(orientation)
    }

    private fun updateOrientation(orientation: Int) = layoutParams?.let {
        val params = it as RelativeLayout.LayoutParams
        val windowManager = context.getSystemService(Service.WINDOW_SERVICE) as WindowManager
        if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
            params.removeRule(RelativeLayout.ALIGN_PARENT_BOTTOM)
            val size = windowManager.defaultDisplay.height / 4
            params.addRule(RelativeLayout.ALIGN_PARENT_TOP, RelativeLayout.TRUE)
            params.addRule(RelativeLayout.ALIGN_PARENT_RIGHT, RelativeLayout.TRUE)
            params.width = RelativeLayout.LayoutParams.WRAP_CONTENT
            params.height = RelativeLayout.LayoutParams.MATCH_PARENT
            setOrientation(HORIZONTAL)
            setLayoutParams(params)
            videoList.setLayoutManager(
                LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
            )
            val adapter = videoList.adapter
            if (adapter is AttenderVideoAdapter) {
                adapter.updateSize(size)
            }
        } else {
            params.removeRule(RelativeLayout.ALIGN_PARENT_TOP)
            params.removeRule(RelativeLayout.ALIGN_PARENT_RIGHT)
            params.width = RelativeLayout.LayoutParams.MATCH_PARENT
            params.height = RelativeLayout.LayoutParams.WRAP_CONTENT
            // params.addRule(RelativeLayout.ABOVE, R.id.view_option_bottombar);
            setOrientation(VERTICAL)
            setLayoutParams(params)
            videoList.setLayoutManager(FocusedLayoutManager(context))
            val size = windowManager.defaultDisplay.width / 4
            val adapter = videoList.adapter
            if (adapter is AttenderVideoAdapter) {
                adapter.updateSize(size)
            }
        }
    }

    inner class FocusedLayoutManager(context: Context) : LinearLayoutManager(context, VERTICAL,false) {

        override fun onInterceptFocusSearch(focused: View, direction: Int): View? {
            val v: MobileRTCVideoView = focused.findViewById(R.id.item_videoView)
            Log.d("OBR", ">>>onInterceptFocusSearch $direction")
            val dir = if (direction == View.FOCUS_LEFT) View.FOCUS_UP
            else if (direction == View.FOCUS_RIGHT) View.FOCUS_DOWN
            else direction
            return super.onInterceptFocusSearch(focused, dir)
        }
    }

    private val onScrollListener: RecyclerView.OnScrollListener =
        object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    val linearLayoutManager = recyclerView.layoutManager as LinearLayoutManager
                    val first = linearLayoutManager.findFirstVisibleItemPosition()
                    val firstView = linearLayoutManager.findViewByPosition(first)
                    if (resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT) {
                        firstView?.left?.let { left ->
                            if (left < 0) {
                                val width = firstView.measuredWidth
                                if (-left >= width / 2) {
                                    videoList.smoothScrollBy(width + left, 0)
                                } else {
                                    videoList.smoothScrollBy(left, 0)
                                }
                            }
                        }
                    } else {
                        firstView?.top?.let { top ->
                            if (top < 0) {
                                val width = firstView.measuredWidth
                                if (-top >= width / 2) {
                                    videoList.smoothScrollBy(0, width + top)
                                } else {
                                    videoList.smoothScrollBy(0, top)
                                }
                            }
                        }
                    }
                }
            }
        }

    private fun updateArrow() {
        val visible = indexOfChild(videoList) > 0
        indicator.rotation =
            if (resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT) {
                if (visible) 0 else 180
            } else {
                if (visible) 270 else 90
            }.toFloat()
    }
}

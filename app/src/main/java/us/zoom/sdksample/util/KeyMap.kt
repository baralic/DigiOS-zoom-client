package us.zoom.sdksample.util

import android.view.KeyEvent

class KeyMap {

    companion object {
        const val KEYCODE_NOP = KeyEvent.KEYCODE_UNKNOWN
        const val KEYCODE_LEFT = KeyEvent.KEYCODE_DPAD_LEFT
        const val KEYCODE_RIGHT = KeyEvent.KEYCODE_DPAD_RIGHT
        const val KEYCODE_MENU = KeyEvent.KEYCODE_MENU
        const val KEYCODE_TEST_MENU = KeyEvent.KEYCODE_DPAD_UP
        const val KEYCODE_BACK = KeyEvent.KEYCODE_BACK
        const val KEYCODE_TEST_BACK = KeyEvent.KEYCODE_DPAD_DOWN
        const val KEYCODE_ENTER = KeyEvent.KEYCODE_ENTER
    }
}

val Int.isHold: Boolean
    get() = (this == KeyMap.KEYCODE_MENU || this == KeyMap.KEYCODE_TEST_MENU)

val Int.isDouble: Boolean
    get() = (this == KeyMap.KEYCODE_BACK || this == KeyMap.KEYCODE_TEST_BACK)

val Int.isEnter: Boolean
    get() = (this == KeyMap.KEYCODE_ENTER)

val Int.isLeft: Boolean
    get() = (this == KeyMap.KEYCODE_LEFT)

val Int.isRight: Boolean
    get() = (this == KeyMap.KEYCODE_RIGHT)

val Int.isNOP: Boolean
    get() = (this == KeyMap.KEYCODE_NOP)

fun KeyEvent.mapTo(keyCode: Int): KeyEvent = KeyEvent(
    downTime, eventTime, action, keyCode, repeatCount, metaState, deviceId, scanCode, flags, source
)

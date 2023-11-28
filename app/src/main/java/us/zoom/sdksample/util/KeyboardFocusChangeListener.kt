package us.zoom.sdksample.util

import android.app.Activity
import android.view.View
import android.view.inputmethod.InputMethodManager


class KeyboardFocusChangeListener(private val ids: Array<Int>) : View.OnFocusChangeListener {

    override fun onFocusChange(view: View?, hasFocus: Boolean) {
        view?.context?.let { context ->
            val imm = context.getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
            if (view.id in ids) {
                imm.showSoftInput(view, 0)
            } else {
                imm.hideSoftInputFromWindow(view.windowToken, 0)
            }
        }
    }
}

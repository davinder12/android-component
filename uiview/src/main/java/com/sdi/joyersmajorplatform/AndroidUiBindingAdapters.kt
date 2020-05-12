@file:Suppress("unused", "UNCHECKED_CAST")

package com.sdi.joyersmajorplatform

import android.view.View
import androidx.databinding.BindingAdapter

class AndroidUiBindingAdapters


@BindingAdapter("visibleGone")
fun showHide(view: View, show: Boolean) {
    view.visibility = if (show) View.VISIBLE else View.GONE
}

@BindingAdapter("goneVisible")
fun hideShow(view: View, show: Boolean) {
    view.visibility = if (show) View.GONE else View.VISIBLE
}

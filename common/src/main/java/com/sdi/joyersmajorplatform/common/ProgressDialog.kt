package com.sdi.joyersmajorplatform.common

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import androidx.annotation.StringRes
import androidx.fragment.app.Fragment
import dmax.dialog.SpotsDialog

fun progressDialog(context: Context, @StringRes text: Int): AlertDialog {
    return SpotsDialog.Builder().setContext(context).setCancelable(false).setMessage(text)
        .build()
}

fun Activity.progressDialog(@StringRes text: Int) =
    progressDialog(this, text)
fun Fragment.progressDialog(@StringRes text: Int) =
    progressDialog(requireContext(), text)

package com.sdi.joyersmajorplatform.uiview

import android.content.Context
import androidx.annotation.StringRes
import com.sdi.joyersmajorplatform.common.getStringOrDefault


@Suppress("DataClassPrivateConstructor")
data class NetworkState private constructor(
    val status: Status,
    val msg: String? = null,
    @StringRes val stringRes: Int? = null
) {

    fun getErrorMessage(context: Context): String? {
        return stringRes?.let { context.getStringOrDefault(it, msg) } ?: msg
    }

    companion object {
        val success =
            NetworkState(Status.SUCCESS)
        val loading =
            NetworkState(Status.RUNNING)

        fun error(msg: String?) =
            NetworkState(
                Status.FAILED,
                msg = msg
            )

        fun success(msg: String?, @StringRes stringRes: Int?) =
            NetworkState(
                Status.SUCCESS,
                msg = msg,
                stringRes = stringRes

            )

        fun error(@StringRes stringRes: Int?) =
            NetworkState(
                Status.FAILED,
                stringRes = stringRes
            )

        fun error(msg: String?, @StringRes stringRes: Int?) =
            NetworkState(
                Status.FAILED,
                msg = msg,
                stringRes = stringRes

            )
    }

    enum class Status {
        RUNNING,
        SUCCESS,
        FAILED
    }
}
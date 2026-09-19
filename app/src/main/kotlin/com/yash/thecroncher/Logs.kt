package com.yash.thecroncher

import android.util.Log

/**
 * Logcat, gated so a release build says nothing routine. Informational chatter
 * (frame rate, surface size, which pad connected) is only for a debug build;
 * genuine failures are always reported, because that is what a bug report needs.
 */
object Logs {
    const val TAG = "CroncherTV"

    fun info(message: () -> String) {
        if (BuildConfig.DEBUG) Log.i(TAG, message())
    }

    fun warn(message: String, error: Throwable? = null) {
        Log.w(TAG, message, error)
    }
}

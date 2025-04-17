package com.example.mapapplication.utils.extension

import android.content.res.Resources

fun Int.dpToPx(): Int {
    return (this * Resources.getSystem().displayMetrics.density + 0.5f).toInt()
}
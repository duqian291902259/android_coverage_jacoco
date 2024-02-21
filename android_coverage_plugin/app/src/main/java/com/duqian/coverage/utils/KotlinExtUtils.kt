package com.duqian.coverage.utils

import android.content.res.Resources
import android.view.View

/**
 * description:Kotlin扩展系统函数
 * @author 杜小菜 Created on 6/20/21 - 10:02 AM.
 * E-mail:duqian2010@gmail.com
 */
// View Extensions
var View.isVisible
    get() = visibility == View.VISIBLE
    set(value) {
        visibility = if (value) View.VISIBLE else View.GONE
    }

fun View.setViewVisible(isVisible: Boolean) {
    visibility = if (isVisible) View.VISIBLE else View.GONE
}

fun Float.dp2px(): Float {
    return (0.5f + this * Resources.getSystem().displayMetrics.density)
}

fun Int.dp2px(): Float {
    return this * 1.0f.dp2px()
}

val Float.dp: Float
    get() = android.util.TypedValue.applyDimension(
        android.util.TypedValue.COMPLEX_UNIT_DIP, this, Resources.getSystem().displayMetrics
    )

val Int.dp: Float
    get() = this * 1.0f.dp


val Float.sp: Float
    get() = android.util.TypedValue.applyDimension(
        android.util.TypedValue.COMPLEX_UNIT_SP, this, Resources.getSystem().displayMetrics
    )

val Int.sp: Float
    get() = this * 1.0f.sp

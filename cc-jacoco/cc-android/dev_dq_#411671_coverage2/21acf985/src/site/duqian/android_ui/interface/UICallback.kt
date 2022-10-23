package site.duqian.android_ui.`interface`

import android.graphics.Bitmap
import android.graphics.Rect

/**
 * Description:
 * @author 杜小菜,Created on 7/3/21 - 12:56 PM.
 * E-mail:duqian2010@gmail.com
 */
interface OnDialogCallback {
    fun onCancel()

    fun onDismiss()

    fun onBitmapDraw(bitmap: Bitmap?, rect: Rect)
}

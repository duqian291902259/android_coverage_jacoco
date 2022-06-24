package site.duqian.android_ui.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.PixelFormat
import android.graphics.Point
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.graphics.drawable.NinePatchDrawable
import android.view.Display
import android.view.WindowManager


/**
 * Description:UI工具类
 * @author 杜小菜,Created on 6/14/21 - 1:53 PM.
 * E-mail:duqian2010@gmail.com
 */
object UIUtils {

    fun drawable2Bitmap(drawable: Drawable?): Bitmap? {
        return if (drawable is BitmapDrawable) {
            drawable.bitmap
        } else if (drawable is NinePatchDrawable) {
            val bitmap = Bitmap
                .createBitmap(
                    drawable.getIntrinsicWidth(),
                    drawable.getIntrinsicHeight(),
                    if (drawable.getOpacity() != PixelFormat.OPAQUE) Bitmap.Config.ARGB_8888 else Bitmap.Config.RGB_565
                )
            val canvas = Canvas(bitmap)
            drawable.setBounds(0, 0, drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight())
            drawable.draw(canvas)
            bitmap
        } else {
            null
        }
    }

    /**
     * 根据手机的分辨率从 dp 的单位 转成为 px(像素)
     */
    fun dp2px(context: Context?, dpValue: Float): Int {
        val scale = context?.resources?.displayMetrics?.density ?: 1.0f
        return (dpValue * scale + 0.5f).toInt()
    }


    /**
     * 根据手机的分辨率从 px(像素) 的单位 转成为 dp
     */
    fun px2dp(context: Context?, pxValue: Float): Int {
        val scale = context?.resources?.displayMetrics?.density ?: 1.0f
        return (pxValue / scale + 0.5f).toInt()
    }

    private fun getDisplay(context: Context?): Display? {
        val windowManager =
            context?.applicationContext?.getSystemService(Context.WINDOW_SERVICE) as WindowManager?
        return windowManager?.defaultDisplay
    }

    fun getScreenWidth(context: Context?): Int {
        return getDisplay(context)?.width ?: 0
    }

    fun getScreenHeight(context: Context?): Int {
        val point = Point()
        getDisplay(context)?.getRealSize(point)
        //return point.x
        return point.y
    }
}
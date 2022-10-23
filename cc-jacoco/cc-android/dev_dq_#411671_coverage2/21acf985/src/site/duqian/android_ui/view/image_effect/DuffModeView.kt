package site.duqian.android_ui.view.image_effect

import android.content.Context
import android.graphics.*
import android.util.AttributeSet

/**
 * Description:两图融合
 *
 * 源图像和目标图像的交集区域，只会显示出目标图像的效果，但是其透明度会受到源图像的透明度的影响。
 *
 * DST_IN 模式，它是根据 [Sa * Da, Sa * Dc] 算法来进行绘制
 * 交集区域的透明度 = 源图像的透明度 * 目标图像的透明度
 * 交集区域的色值 = 源图像的透明度 * 目标图像的色值
 *
 * @author 杜小菜, Created on 6/13/21 - 5:45 PM.
 * E-mail:duqian2010@gmail.com
 */
open class DuffModeView @JvmOverloads constructor(
    context: Context?,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : BaseDuffModeView(context, attrs, defStyleAttr) {

    override fun createDstBitmap(width: Int, height: Int): Bitmap {
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        val dstPaint = Paint(Paint.ANTI_ALIAS_FLAG)
        dstPaint.color = Color.parseColor("#00b7ee")
        canvas.drawCircle(
            (width / 3).toFloat(),
            (height / 3).toFloat(),
            (width / 3).toFloat(),
            dstPaint
        )
        return bitmap
    }

    override fun createSrcBitmap(width: Int, height: Int): Bitmap {
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        val scrPaint = Paint(Paint.ANTI_ALIAS_FLAG)
        scrPaint.color = Color.parseColor("#ec6941")
        canvas.drawRect(Rect(width / 3, height / 3, width, height), scrPaint)
        return bitmap
    }

}
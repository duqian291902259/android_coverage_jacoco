package site.duqian.android_ui.view.image_effect

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.*
import android.graphics.drawable.Drawable
import android.text.TextUtils
import android.util.AttributeSet
import android.util.Log
import android.view.View
import site.duqian.android_ui.R
import site.duqian.android_ui.utils.UIUtils

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
abstract class BaseDuffModeView @JvmOverloads constructor(
    context: Context?,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    var mRectBorder: Rect? = null
    var mPaint: Paint? = null
    var mWidth = 0
    var mHeight = 0
    var mMode: Int = 0
    var mSrcImageResId: Int = R.mipmap.ic_avatar_duqian //外部传入融合的图像res id
    var mPorterDuffMode: PorterDuff.Mode = PorterDuff.Mode.SRC_IN
    var mSrcBitmap: Bitmap? = null
    var mDstBitmap: Bitmap? = null
    var mBorderBitmap: Bitmap? = null
    var mIsLeft = false

    private var mSrcId: Drawable? = null
    private var mDstId: Drawable? = null
    private var mBorderId: Drawable? = null

    init {
        val obtainStyledAttributes =
            context?.obtainStyledAttributes(attrs, R.styleable.BaseDuffModeView)
        mMode = obtainStyledAttributes?.getInt(R.styleable.BaseDuffModeView_mode, 0) ?: 0
        mSrcId = obtainStyledAttributes?.getDrawable(R.styleable.BaseDuffModeView_src)
        mDstId = obtainStyledAttributes?.getDrawable(R.styleable.BaseDuffModeView_dst)
        mBorderId = obtainStyledAttributes?.getDrawable(R.styleable.BaseDuffModeView_border)
        mIsLeft =
            obtainStyledAttributes?.getBoolean(R.styleable.BaseDuffModeView_isLeft, false) ?: false
        obtainStyledAttributes?.recycle()

        mSrcBitmap = UIUtils.drawable2Bitmap(mSrcId)
        mDstBitmap = UIUtils.drawable2Bitmap(mDstId)
        mBorderBitmap = UIUtils.drawable2Bitmap(mBorderId)

        //禁用硬件加速
        this.setLayerType(LAYER_TYPE_SOFTWARE, null)
        //初始化画笔
        mPaint = Paint(Paint.ANTI_ALIAS_FLAG)

        updatePorterDuffMode()
    }

    fun updatePorterDuffMode() {
        for (mode in PorterDuff.Mode.values()) {
            if (mMode == mode.ordinal) {
                mPorterDuffMode = mode
                Log.d("dq-android-ui", "name=${mode.name},ordinal=${mode.ordinal}")
                break
            }
        }
    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        super.onLayout(changed, left, top, right, bottom)
        mWidth = right - left
        mHeight = bottom - top
        if (mRectBorder == null && mWidth > 0 && mHeight > 0) {
            mRectBorder = Rect(0, 0, mWidth, mHeight)
        }
        mDstBitmap = createDstBitmap(mWidth, mHeight)
        //获取src图片
        mSrcBitmap = createSrcBitmap(mWidth, mHeight)
    }

    @SuppressLint("DrawAllocation")
    override fun onDraw(canvas: Canvas) {
        try {
            super.onDraw(canvas)

            if (mDstBitmap != null) {
                canvas.drawBitmap(mDstBitmap!!, 0f, 0f, mPaint)
            }
            if (mMode > 0) {
                mPaint?.xfermode = PorterDuffXfermode(mPorterDuffMode)
            }
            if (mSrcBitmap != null) {
                canvas.drawBitmap(mSrcBitmap!!, 0f, 0f, mPaint)
            }
            mPaint?.xfermode = null

            // TODO: 6/15/21 画梯形

            drawText(canvas)

            if (mBorderBitmap != null && mRectBorder != null) {
                canvas.drawBitmap(mBorderBitmap!!, null, mRectBorder!!, mPaint)
            }
        } catch (e: Exception) {
            Log.d("dq-ui", "onDraw error $e")
        }
    }

    private fun drawText(canvas: Canvas) {
        mPaint?.apply {
            val name = mPorterDuffMode.name
            if (!TextUtils.isEmpty(name)) {
                this.style = Paint.Style.FILL
                this.strokeWidth = 30f
                this.textSize = 30f
                this.setShadowLayer(30f, 5f, 2f, Color.YELLOW)
                this.color = Color.parseColor("#00FFFF")
                canvas.drawText(name, (mWidth-this.measureText(name))/2, mHeight - this.textSize, this)
            }
        }
    }

    abstract fun createDstBitmap(width: Int, height: Int): Bitmap?

    abstract fun createSrcBitmap(width: Int, height: Int): Bitmap?

    fun updateSrcImage(srcId: Int) {
        mSrcImageResId = srcId
        requestLayout()
        invalidate()
    }
}
package site.duqian.android_ui.view

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.util.Log
import android.view.View
import androidx.core.util.Pair
import site.duqian.android_ui.R
import site.duqian.android_ui.utils.UIUtils
import kotlin.math.min

/**
 * Description:圆环进度条，中间有间隔，画扇形，画进度文本
 *
 * @author 杜小菜 on 2021/3/23 - 11:11 .
 * E-mail: duqian2010@gmail.com
 */
class CircleGapProgressView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {
    private var centerPadding = 0
    private var mTextSize = 15f
    private var mTextColor = -1
    private var progressWidth = 0f
    private var progressNum = 0
    private var gapSize = 0f
    private var bgColor = 0
    private var progressColor = 0
    private val bgPaint: Paint
    private var mTextPaint: Paint? = null
    private val progressPaint: Paint

    //进度扇形所在的圆范围
    private val rectF = RectF()

    //进度条划过的角度
    private var progressDegree = 0f

    //扇形 + 之间的空隙的角度
    private val gapProgressDegree: Float
    private var center: Pair<Float?, Float?>? = null
    private var maxProgress = 100
    private var currentProgress = 0

    //中间的text文本要真实，但是进度条不一定
    private var currentProgressText = 0
    private var isShowProgressText = false
    private val mTextBound = Rect()
    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val measuredWidth = MeasureSpec.getSize(widthMeasureSpec)
        val measuredHeight = MeasureSpec.getSize(heightMeasureSpec)
        Log.i("dq-ui", "measuredWidth=$measuredWidth,measuredHeight=$measuredHeight")
        val min = min(measuredWidth, measuredHeight)
        setMeasuredDimension(min, min)

        rectF[centerPadding.toFloat(), centerPadding.toFloat(), measuredWidth.toFloat() - centerPadding] =
            measuredHeight.toFloat() - centerPadding
        //每个一个扇形进度 / 周长 * 360
        progressDegree =
            (3.14f * measuredWidth / progressNum - gapSize) / (3.14f * measuredWidth) * 360f
        center = Pair.create(measuredWidth / 2f, measuredHeight / 2f)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        try {
            if (center!!.first == null || center!!.second == null) {
                return
            }
            //绘制背景圆环,相当于每次都是绘制了一次进度100的扇形
            if (bgPaint.color != 0) {
                var startDegree = 0f
                while (startDegree < 360) {
                    canvas.drawArc(
                        rectF,
                        startDegree + ORIGIN_DEGREE,
                        progressDegree,
                        false,
                        bgPaint
                    )
                    startDegree += gapProgressDegree
                }
            }
            //绘制进度
            var startDegree = calculateStartDegree()
            while (startDegree < 360) {
                canvas.drawArc(
                    rectF,
                    startDegree + ORIGIN_DEGREE,
                    progressDegree,
                    false,
                    progressPaint
                )
                startDegree += gapProgressDegree
            }
            setProgressText(canvas)
        } catch (e: Exception) {
        }
    }

    private fun setProgressText(canvas: Canvas) {
        if (isShowProgressText) {
            val text = "$currentProgressText%"
            mTextPaint!!.getTextBounds(text, 0, text.length, mTextBound)
            canvas.drawText(
                text, width / 2.0f - mTextBound.width() / 2.0f,
                height / 2.0f + mTextBound.height() / 2.0f, mTextPaint!!
            )
        }
    }

    fun setProgressColor(color: Int) {
        progressColor = color
        progressPaint.color = progressColor
        invalidate()
    }

    fun setMaxProgress(maxProgress: Int) {
        this.maxProgress = maxProgress
        invalidate()
    }

    fun setCurrentProgress(currentProgress: Int) {
        var currentProgress = currentProgress
        if (currentProgress > maxProgress) {
            currentProgress = maxProgress
        }
        this.currentProgress = currentProgress
        currentProgressText = currentProgress
        if (currentProgress in 1..10) {
            //防止看不到进度?
            this.currentProgress = 10
        }
        invalidate()
    }

    private fun calculateStartDegree(): Float {
        //计算当前的进度位置，第几个扇形
        val startProgress =
            Math.ceil(progressNum.toDouble() * (maxProgress - currentProgress) / maxProgress)
                .toInt()
        //计算这个扇形的开始角度 = 每个进度的角度 * 已过去的进度数
        return gapProgressDegree * (progressNum - startProgress)
    }

    companion object {
        private const val ORIGIN_DEGREE = -90f
    }

    init {
        val a = context.theme.obtainStyledAttributes(attrs, R.styleable.CircleGapProgressView, 0, 0)
        try {
            progressWidth = a.getDimension(
                R.styleable.CircleGapProgressView_progress_width,
                UIUtils.dp2px(context, 3f).toFloat()
            )
            progressNum = a.getInt(R.styleable.CircleGapProgressView_progress_num, 10)
            gapSize = a.getDimension(
                R.styleable.CircleGapProgressView_gap_size,
                UIUtils.dp2px(context, 1f).toFloat()
            )
            bgColor = a.getColor(R.styleable.CircleGapProgressView_bg_color, Color.WHITE)
            progressColor = a.getColor(R.styleable.CircleGapProgressView_progress_color, 0xffffff)
            currentProgress = a.getInt(R.styleable.CircleGapProgressView_current_progress, 0)
            maxProgress = a.getColor(R.styleable.CircleGapProgressView_max_progress, 100)
            isShowProgressText =
                a.getBoolean(R.styleable.CircleGapProgressView_showProgressText, false)
            mTextColor = a.getColor(R.styleable.CircleGapProgressView_text_color, 0xffffff)
            mTextSize = a.getDimension(R.styleable.CircleGapProgressView_text_size, 15f)

            centerPadding = progressWidth.toInt()
        } finally {
            a.recycle()
        }

        //底色
        bgPaint = Paint()
        bgPaint.style = Paint.Style.STROKE
        bgPaint.isAntiAlias = true
        bgPaint.color = bgColor
        bgPaint.strokeWidth = progressWidth

        //进度画笔
        progressPaint = Paint()
        progressPaint.style = Paint.Style.STROKE
        progressPaint.isAntiAlias = true
        progressPaint.color = progressColor
        progressPaint.strokeWidth = progressWidth
        gapProgressDegree = 360f / progressNum
        if (isShowProgressText) {
            mTextPaint = Paint()
            mTextPaint?.apply {
                this.strokeWidth = 4f
                this.textSize = mTextSize
                this.isAntiAlias = true
                this.color = mTextColor
                this.textAlign = Paint.Align.LEFT
            }
        }

        setCurrentProgress(currentProgress)
    }
}
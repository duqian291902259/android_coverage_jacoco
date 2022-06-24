package site.duqian.android_ui.fragment

import android.app.Dialog
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Rect
import android.os.Bundle
import android.util.Log
import android.view.*
import android.view.animation.*
import site.duqian.android_ui.*
import site.duqian.android_ui.`interface`.OnDialogCallback


/**
 * description:自定义dialog,做动画
 * @author 杜小菜 Created on 6/30/21 - 10:02 PM.
 * E-mail:duqian2010@gmail.com
 */
class CommonDialogFragment : BaseDialogFragment() {

    private lateinit var wrapDialogContent: View
    private lateinit var tvTitle: View
    private var mWidth = 0
    private var mHeight = 0

    companion object {
        @JvmOverloads
        fun newInstance(
            cancelable: Boolean = true,
            cancelListener: OnDialogCallback? = null
        ): BaseDialogFragment {
            val instance = CommonDialogFragment()
            instance.isCancelable = cancelable
            instance.mDialogCallback = cancelListener
            return instance
        }
    }

    override fun getDialog(context: Context?): Dialog? {
        return null
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val rootView = inflater.inflate(R.layout.fragment_common_dialog, null)
        wrapDialogContent = rootView.findViewById(R.id.wrap_dialog_content)
        tvTitle = rootView.findViewById(R.id.tv_title)
        return rootView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        view.post {
            mWidth = view.measuredWidth
            mHeight = view.measuredHeight

            val bitmap = copyBitmapByCanvas(wrapDialogContent)
            val rect = Rect()
            wrapDialogContent.getGlobalVisibleRect(rect)
            Log.d("dq-dialog", "dialog rect=$rect")

            mDialogCallback?.onBitmapDraw(bitmap,rect)

        }
        wrapDialogContent.setOnClickListener {
            closeDialog()
            //startAnimation()
        }
    }

    private fun startAnimation() {
        val mAnimatorSet = AnimationSet(true)
        val animation = TranslateAnimation(0f, 300f, 500f, 500f)
        animation.duration = 200//设置动画变化的持续时间
        animation.isFillEnabled = true//使其可以填充效果从而不回到原地
        animation.fillAfter = true//不回到起始位置
        mAnimatorSet.addAnimation(animation)
        tvTitle.startAnimation(mAnimatorSet)


        mAnimatorSet.setAnimationListener(object : Animation.AnimationListener {
            override fun onAnimationStart(animation: Animation?) {

            }

            override fun onAnimationEnd(animation: Animation?) {
                //doScaleAnimation()
                closeDialog()
            }

            override fun onAnimationRepeat(animation: Animation?) {

            }
        })
    }

    /**
     * 通过canvas复制view的bitmap
     */
    private fun copyBitmapByCanvas(view: View?): Bitmap? {
        val width = view?.measuredWidth ?: 0
        val height = view?.measuredHeight ?: 0
        if (width <= 0 || height <= 0) return null
        Log.d("dq-dialog", "copyByCanvas: width=$width,height=$height")
        val bp = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bp)
        view?.draw(canvas)
        canvas.save()
        return bp
    }

    /**
     * 无效，有bug
     */
    private fun updateDialogLayout(value: Float) {
        val window: Window? = dialog?.window
        val windowParams: WindowManager.LayoutParams? = window?.attributes
        windowParams?.width = 1000 * value.toInt()
        windowParams?.height = 1000 * value.toInt()
        window?.clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND)
        window?.setGravity(Gravity.START or Gravity.TOP)
        windowParams?.x = 1500 * value.toInt()
        windowParams?.y = 500 * value.toInt()
        window?.attributes = windowParams
        Log.d("dq-dialog", "window=$window,value=$value.windowParams")
    }

    private fun closeDialog() {
        dismissAllowingStateLoss()
    }
}
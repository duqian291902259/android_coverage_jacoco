package site.duqian.android_ui.fragment

import android.animation.ObjectAnimator
import android.animation.PropertyValuesHolder
import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.graphics.Rect
import android.util.DebugUtils
import android.util.Log
import android.view.View
import android.view.animation.*
import android.widget.ImageView
import android.widget.RelativeLayout
import site.duqian.android_ui.R
import site.duqian.android_ui.`interface`.OnDialogCallback
import site.duqian.android_ui.utils.UIUtils

/**
 * description:各种各样的dialog
 * @author 杜小菜 Created on 6/13/21 - 11:19 AM.
 * E-mail:duqian2010@gmail.com
 */
class MainDialogFragment : BaseFragment() {

    private lateinit var wrapImageBody: View
    private lateinit var ivTestImage: ImageView
    private lateinit var tvTitle: View

    companion object {
        fun newInstance() = MainDialogFragment()
        private const val ANIM_DURATION = 2000L
    }

    override fun getLayoutId(): Int {
        return R.layout.fragment_main_dialog
    }

    override fun initView(view: View) {
        wrapImageBody = view.findViewById(R.id.iv_test_image)
        ivTestImage = view.findViewById(R.id.iv_test_image)
        tvTitle = view.findViewById(R.id.tv_title)

        tvTitle.setOnClickListener {
            showDialog()
        }
    }

    override fun initData() {
        showDialog()
    }

    private var dialogRect = Rect()
    private fun showDialog() {
        val dialog = CommonDialogFragment.newInstance(true, object : OnDialogCallback {
            override fun onCancel() {

            }

            override fun onDismiss() {
                handleAnimation()
            }

            override fun onBitmapDraw(bitmap: Bitmap?, rect: Rect) {
                //显示并用于做动画
                ivTestImage.setImageBitmap(bitmap)
                ivTestImage.visibility = View.GONE
                dialogRect = rect
            }
        })
        if (fragmentManager != null) {
            dialog.show(fragmentManager!!, "dialog")
        }
    }

    @SuppressLint("ObjectAnimatorBinding")
    private fun handleAnimation() {
        ivTestImage.visibility = View.VISIBLE

        //startAnimation()

        //rect1=Rect(686, 324 - 1104, 381) rect=Rect(48, 48 - 768, 1071)
        val rect1 = Rect()
        tvTitle.getGlobalVisibleRect(rect1)
        Log.d("dq-dialog", "rect1=$rect1")
        //relayout(rect1)

        //startAnimation(wrapImageBody)
        startTranslationAnimation(wrapImageBody)

        doDialogAnimation(wrapImageBody)
    }

    private fun doDialogAnimation(view: View) {
        val valueAnimator = ValueAnimator.ofFloat(1f, 0f)
        val marginStart = UIUtils.dp2px(context, 200f)
        val marginTop = UIUtils.dp2px(context, 400f)

        val width = dialogRect.width()//UIUtils.getScreenWidth(context)
        val height = dialogRect.height()//UIUtils.dp2px(context, 400f)
        valueAnimator.addUpdateListener {
            val value = it.animatedValue as Float
            Log.d("dq-dialog", "value=$value")

            //wrapImageBody.alpha = value

            val layoutParams = view.layoutParams as RelativeLayout.LayoutParams
            layoutParams.width = (width * value).toInt()
            layoutParams.height = (height * value).toInt()
            view.layoutParams = layoutParams
        }
        valueAnimator.duration = ANIM_DURATION
        valueAnimator.interpolator = LinearInterpolator()
        valueAnimator.start()
    }

    private fun startAnimation(view: View) {
        val scaleX = PropertyValuesHolder.ofFloat("scaleX", 1f, 0f)
        val scaleY = PropertyValuesHolder.ofFloat("scaleY", 1f, 0f)
        val alpha = PropertyValuesHolder.ofFloat("alpha", 1f, 0.2f)
        val animator =
            ObjectAnimator.ofPropertyValuesHolder(view, alpha, scaleX, scaleY)
        animator.duration = ANIM_DURATION
        animator.interpolator = LinearInterpolator()
        animator.start()
    }

    private fun startTranslationAnimation(view: View) {
        val rect1 = Rect()
        tvTitle.getGlobalVisibleRect(rect1)

        //左移负数，上移负数
        val screenWidth = UIUtils.getScreenWidth(context).toFloat()
        val screenHeight = UIUtils.getScreenHeight(context).toFloat()
        //val dx = -(screenWidth / 2 - (rect1.left + (rect1.right - rect1.left) / 2))
        //val dy = -(screenHeight / 2 - (rect1.top + (rect1.bottom - rect1.top) / 2))

        val dp2px = UIUtils.dp2px(context, 30f)
        var dxx = 1
        if (screenWidth < rect1.left) {
            dxx = -1
        }
        var dyy = -1
        if (screenHeight < rect1.top) {
            dyy = 1
        }

        val dx = -(screenWidth / 2 - (rect1.left + dp2px)) + dxx * dp2px
        val dy = -(screenHeight / 2 - (rect1.top + dp2px)) + dyy *  UIUtils.dp2px(context, 60f)

        //val dx = -(dialogRect.centerX() - rect1.centerX()).toFloat()
        //val dy = -(dialogRect.centerY() - rect1.centerY()).toFloat()
        Log.d("dq-dialog", "dx=$dx,dy=$dy")
        val mAnimatorSet = AnimationSet(true)

        //dx=110.0,dy=-864.0
        val translateAnimation = TranslateAnimation(0f, dx, 0f, dy)
        //val translateAnimation = TranslateAnimation(0f, dx, 0f, dy)

        val scaleAnimation = ScaleAnimation(1.0f, 0.0f, 1f, 0.0f)
        val alphaAnimation = AlphaAnimation(1.0f, 0.2f)
        mAnimatorSet.addAnimation(alphaAnimation)
        //mAnimatorSet.addAnimation(scaleAnimation)
        mAnimatorSet.addAnimation(translateAnimation)

        mAnimatorSet.duration = ANIM_DURATION//设置动画变化的持续时间
        view.startAnimation(mAnimatorSet)
        mAnimatorSet.setAnimationListener(object : Animation.AnimationListener {
            override fun onAnimationStart(animation: Animation?) {

            }

            override fun onAnimationEnd(animation: Animation?) {
                view.visibility = View.GONE
                view.clearAnimation()
                mAnimatorSet.cancel()
            }

            override fun onAnimationRepeat(animation: Animation?) {

            }
        })
    }
}
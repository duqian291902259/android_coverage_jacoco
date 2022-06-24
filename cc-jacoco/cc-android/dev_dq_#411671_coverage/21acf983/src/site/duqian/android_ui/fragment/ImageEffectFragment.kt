package site.duqian.android_ui.fragment

import android.view.View
import site.duqian.android_ui.R
import site.duqian.android_ui.view.image_effect.DuffModeImageView

/**
 * description:图片融合效果，比如梯形图片，圆角图片
 * @author 杜小菜 Created on 6/13/21 - 11:20 AM.
 * E-mail:duqian2010@gmail.com
 */
class ImageEffectFragment : BaseFragment() {

    companion object {
        fun newInstance() = ImageEffectFragment()
    }

    override fun getLayoutId(): Int {
        return R.layout.fragment_image_effect
    }

    override fun initView(view: View) {

        val imageView: DuffModeImageView? = view.findViewById(R.id.iv_test_avatar)

        imageView?.postDelayed({
            imageView.updateSrcImage(R.mipmap.ic_avatar_duqian)
        }, 3000L)

        imageView?.setOnClickListener {
            imageView.updateSrcImage(R.mipmap.ic_avatar_duqian)
        }

        imageView?.setOnLongClickListener {
            imageView.updateSrcImage(R.mipmap.ic_qrcode)
            true
        }


        /*val rootView: LinearLayout = view.findViewById(R.id.wrap_iv_mode)
        for (mode in PorterDuff.Mode.values()) {
            Log.d("dq-android-ui", "name=${mode.name},ordinal=${mode.ordinal}")
            val imageView = DuffModeImageView(context)
            val lp = LinearLayout.LayoutParams(400, 300)
            lp.topMargin = 20
            imageView.layoutParams = lp
            imageView.setPorterDuffMode(mode)
            rootView.addView(imageView)
        }
        rootView.requestLayout()*/
    }

    override fun initData() {

    }
}
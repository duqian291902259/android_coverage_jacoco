package site.duqian.android_ui.fragment

import android.view.View
import site.duqian.android_ui.R

/**
 * description:各种各样的进度条
 * @author 杜小菜 Created on 6/13/21 - 11:19 AM.
 * E-mail:duqian2010@gmail.com
 */
class ProgressFragment : BaseFragment() {

    companion object {
        fun newInstance() = ProgressFragment()
    }

    override fun getLayoutId(): Int {
        return R.layout.fragment_progress
    }

    override fun initView(view: View) {

    }

    override fun initData() {

    }
}
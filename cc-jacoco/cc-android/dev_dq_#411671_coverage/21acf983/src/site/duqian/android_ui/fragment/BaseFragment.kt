package site.duqian.android_ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import site.duqian.android_ui.R

/**
 * description:fragment基类
 * @author 杜小菜 Created on 6/13/21 - 11:00 AM.
 * E-mail:duqian2010@gmail.com
 */
abstract class BaseFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val layoutId = getLayoutId()
        return inflater.inflate(layoutId, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView(view)

        initData()
    }

    protected abstract fun getLayoutId(): Int

    protected abstract fun initView(view: View)

    protected abstract fun initData()

}